/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.util.HexDump;
import org.vesalainen.vfs.CompressorFactory;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import org.vesalainen.vfs.arch.FileFormat;
import org.vesalainen.vfs.arch.Header;
import org.vesalainen.vfs.arch.tar.TARHeader;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.pm.Condition;
import org.vesalainen.vfs.pm.DependencyCondition;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEBFileSystem extends ArchiveFileSystem implements PackageManagerAttributeView
{
    private static final byte[] AR_MAGIC = "!<arch>\n".getBytes(US_ASCII);
    private static final byte[] DEB_VERSION = "2.0\n".getBytes(US_ASCII);
    private static final String INTERPRETER = "/bin/sh";
    private final Root root;
    private final Root controlRoot;
    private Control control;
    private Conffiles conffiles;
    private Docs docs;
    private ChangeLog changeLog;
    private Copyright copyright;
    private MD5Sums md5Sums;

    public DEBFileSystem(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        this(provider, path, TARHeader::new, getOpenOptions(path, env), getFileAttributes(env), getFileFormat(path, env));
    }

    private DEBFileSystem(
            VirtualFileSystemProvider provider, 
            Path path, 
            Supplier<Header> headerSupplier, 
            Set<? extends OpenOption> openOptions, 
            FileAttribute<?>[] fileAttributes, 
            FileFormat format) throws IOException
    {
        super(provider, path, headerSupplier, openOptions, fileAttributes, format, openChannel(path, openOptions, fileAttributes));
        checkFormat(format);
        VirtualFileStore defStore = new VirtualFileStore(this, UNIX_VIEW, USER_VIEW);
        defStore.addFileStoreAttributeView(this);
        root = addFileStore("/",defStore , true);
        VirtualFileStore controlStore = new VirtualFileStore(this, UNIX_VIEW);
        controlRoot = addFileStore("/control",controlStore , false);
        if (isReadOnly())
        {
            loadDEB();
        }
        else
        {
        }
    }
    private void loadDEB() throws IOException
    {
        long pos = 0;
        ByteBuffer bb = ByteBuffer.allocate(8);
        channel.read(bb);
        if (!Arrays.equals(AR_MAGIC, bb.array()))
        {
            throw new UnsupportedOperationException("not a deb file");
        }
        pos += 8;
        ArHeader packageSection = new ArHeader(channel);
        pos += 60;
        int arSize = packageSection.getSize();
        bb.clear().limit(arSize);
        channel.read(bb);
        pos += arSize;
        for (int ii=0;ii<arSize;ii++)
        {
            if (DEB_VERSION[ii] != bb.get(ii))
            {
                throw new UnsupportedOperationException(HexDump.toHex(bb, 0, arSize)+" wrong version deb file");
            }
        }
        ArHeader controlSection = new ArHeader(channel);
        pos += 60;
        Path controlfile = getPath(controlSection.getFilename());
        FilterChannel controlChannel = new FilterChannel(channel, 4096, 512, CompressorFactory.input(controlfile), null);
        load(controlChannel, controlRoot);
        pos += controlSection.getSize();
        channel.position(pos);
        ArHeader dataSection = new ArHeader(channel);
        pos += 60;
        Path datafile = getPath(dataSection.getFilename());
        FilterChannel dataChannel = new FilterChannel(channel, 4096, 512, CompressorFactory.input(datafile), null);
        load(dataChannel, root);
        pos += dataSection.getSize();
        control = new Control(controlRoot);
        conffiles = new Conffiles(controlRoot);
        docs = new Docs(controlRoot);
        Path docPath = getPath("/usr/share/doc");
        Path packageDocPath = docPath.resolve(getPackageName());
        Path changeLogPath = packageDocPath.resolve("changelog.Debian.gz");
        changeLog = new ChangeLog(changeLogPath);
        copyright = new Copyright(packageDocPath);
        md5Sums = new MD5Sums(controlRoot, root);
        setFileAttributes();
    }
    private static SeekableByteChannel openChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        return FileChannel.open(path, options, attrs);
    }    
    private void setFileAttributes() throws IOException
    {
        for (Path p : docs.getFiles())
        {
            EnumSet<FileUse> usage = PackageFileAttributes.getUsage(p);
            usage.add(FileUse.DOCUMENTATION);
            PackageFileAttributes.setUsage(p, usage);
        }
        for (Path p : conffiles.getFiles())
        {
            EnumSet<FileUse> usage = PackageFileAttributes.getUsage(p);
            usage.add(FileUse.CONFIGURATION);
            PackageFileAttributes.setUsage(p, usage);
        }
    }

    private void checkFormat(FileFormat fmt)
    {
        switch (fmt)
        {
            case TAR_PAX:
            case TAR_USTAR:
            case TAR_GNU:
            case TAR_OLDGNU:
            case TAR_V7:
                break;
            default:
                throw new UnsupportedOperationException(fmt.name());
        }
    }

    @Override
    public PackageManagerAttributeView addConflict(String name, String version, Condition... dependency)
    {
        control.addConflict(name, version, dependency);
        return this;
    }

    @Override
    public Collection<String> getConflicts()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DependencyCondition getConflict(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView addProvide(String name, String version, Condition... dependency)
    {
        control.addProvides(name);
        return this;
    }

    @Override
    public Collection<String> getProvides()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DependencyCondition getProvide(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView addRequire(String name, String version, Condition... dependency)
    {
        control.addDepends(name, version, dependency);
        return this;
    }

    @Override
    public Collection<String> getRequires()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DependencyCondition getRequire(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setArchitecture(String architecture)
    {
        control.setArchitecture(architecture);
        return this;
    }

    @Override
    public String getArchitecture()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setDescription(String description)
    {
        control.setDescription(description);
        return this;
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setCopyright(String copyright)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCopyright()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setLicense(String license)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLicense()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPackageName(String name)
    {
        control.setPackage(name);
        return this;
    }

    @Override
    public String getPackageName()
    {
        return control.getPackage();
    }

    @Override
    public PackageManagerAttributeView setOperatingSystem(String os)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getOperatingSystem()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPostInstallation()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPostInstallationInterpreter()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPostUnInstallation()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPostUnInstallationInterpreter()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPreInstallation()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPreInstallationInterpreter()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPreUnInstallation()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPreUnInstallationInterpreter()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDefaultInterpreter()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPostInstallation(String script, String interpreter)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPostUnInstallation(String script, String interpreter)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPreInstallation(String script, String interpreter)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPreUnInstallation(String script, String interpreter)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setRelease(String release)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRelease()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setSummary(String summary)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSummary()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setVersion(String version)
    {
        control.setVersion(version);
        return this;
    }

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setApplicationArea(String area)
    {
        control.setSection(area);
        return this;
    }

    @Override
    public String getApplicationArea()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPriority(String priority)
    {
        control.setPriority(priority);
        return this;
    }

    @Override
    public String getPriority()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setMaintainer(String maintainer)
    {
        control.setMaintainer(maintainer);
        return this;
    }

    @Override
    public String getMaintainer()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String name()
    {
        return "org.vesalainen.vfs.pm.deb";
    }

}
