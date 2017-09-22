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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.FilterChannel;
import org.vesalainen.regex.RegexMatcher;
import org.vesalainen.util.HexDump;
import org.vesalainen.vfs.CompressorFactory;
import static org.vesalainen.vfs.CompressorFactory.Compressor.*;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import org.vesalainen.vfs.arch.FileFormat;
import org.vesalainen.vfs.arch.Header;
import org.vesalainen.vfs.arch.tar.TARHeader;
import static org.vesalainen.vfs.arch.tar.TARHeader.TAR_BLOCK_SIZE;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.pm.Condition;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;
import org.vesalainen.vfs.pm.Dependency;
import static org.vesalainen.vfs.pm.FileUse.*;
import org.vesalainen.vfs.pm.deb.Copyright.FileCopyright;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEBFileSystem extends ArchiveFileSystem implements PackageManagerAttributeView
{
    private static final RegexMatcher<Boolean> PACKAGE_MATCHER = new RegexMatcher("[a-z][0-9a-z\\.\\+\\-]+", true).compile();
    private static final RegexMatcher<Boolean> UPSTREAM_VERSION_MATCHER = new RegexMatcher("[0-9][0-9a-zA-Z\\.\\+\\-~]*", true).compile();
    private static final RegexMatcher<Boolean> DEBIAN_REVISION_MATCHER = new RegexMatcher("[0-9a-zA-Z\\.\\+~]*", true).compile();
    private static final int BUF_SIZE = 4096;
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
    private MaintainerScript preinst;
    private MaintainerScript postinst;
    private MaintainerScript prerm;
    private MaintainerScript postrm;

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
            control = new Control();
            conffiles = new Conffiles();
            docs = new Docs();
            changeLog = new ChangeLog();
            copyright = new Copyright();
            preinst = new MaintainerScript(controlRoot, "preinst");
            postinst = new MaintainerScript(controlRoot, "postinst");
            prerm = new MaintainerScript(controlRoot, "prerm");
            postrm = new MaintainerScript(controlRoot, "postrm");
            provisionFromPath();
        }
    }

    @Override
    public void close() throws IOException
    {
        if (!isReadOnly())
        {
            storeDEB();
        }
        channel.close();
    }
    private void storeDEB() throws IOException
    {
        getFileAttributes();
        Path docPath = getPath("/usr/share/doc");
        Path packageDocPath = docPath.resolve(getPackageName());
        Files.createDirectories(packageDocPath);
        Path changeLogPath = packageDocPath.resolve("changelog.Debian.gz");
        control.save(controlRoot);
        conffiles.save(controlRoot);
        docs.save(controlRoot);
        changeLog.save(changeLogPath);
        copyright.save(packageDocPath);
        MD5Sums.save(controlRoot, root);
        preinst.save();
        postinst.save();
        prerm.save();
        postrm.save();
        
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.put(AR_MAGIC).flip();
        channel.write(bb);
        ArHeader packageSection = new ArHeader("debian-binary", 4);
        packageSection.save(channel);
        bb.clear();
        bb.put(DEB_VERSION);
        bb.flip();
        channel.write(bb);
        // control
        long controlArStart = channel.position();
        ChannelHelper.skip(channel, ArHeader.AR_HEADER_SIZE);
        long controlStart = channel.position();
        try (FilterChannel controlChannel = new FilterChannel(channel, BUF_SIZE, TAR_BLOCK_SIZE, null, CompressorFactory.output(GZIP)))
        {
            store(controlChannel, controlRoot);
        }
        long controlEnd = channel.position();
        ArHeader controlSection = new ArHeader("control.tar.gz", (int) (controlEnd-controlStart));
        channel.position(controlArStart);
        controlSection.save(channel);
        channel.position(controlEnd);
        // data
        long dataArStart = channel.position();
        ChannelHelper.skip(channel, ArHeader.AR_HEADER_SIZE);
        long dataStart = channel.position();
        try (FilterChannel dataChannel = new FilterChannel(channel, BUF_SIZE, TAR_BLOCK_SIZE, null, CompressorFactory.output(XZ)))
        {
            store(dataChannel, root);
        }
        long dataEnd = channel.position();
        ArHeader dataSection = new ArHeader("data.tar.xz", (int) (dataEnd-dataStart));
        channel.position(dataArStart);
        dataSection.save(channel);
        channel.position(dataEnd);
    }
    private void getFileAttributes() throws IOException
    {
        walk(root).filter((p)->Files.isRegularFile(p)).forEach((p)->
        {
            try
            {
                FileCopyright fileCopyright = null;
                String cr = PackageFileAttributes.getCopyright(p);
                if (cr != null)
                {
                    if (fileCopyright == null)
                    {
                        fileCopyright = copyright.addFile(p);
                    }
                    fileCopyright.addCopyright(cr);
                }
                String lic = PackageFileAttributes.getLicense(p);
                if (lic != null)
                {
                    if (fileCopyright == null)
                    {
                        fileCopyright = copyright.addFile(p);
                    }
                    fileCopyright.addLicense(lic);
                }
                EnumSet<FileUse> usage = PackageFileAttributes.getUsage(p);
                if (usage.contains(DOCUMENTATION))
                {
                    docs.addFile(p);
                }
                if (usage.contains(CONFIGURATION))
                {
                    conffiles.addFile(p);
                }
            }
            catch (IOException ex)
            {
                Logger.getLogger(DEBFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    
    private void loadDEB() throws IOException
    {
        long pos = 0;
        ByteBuffer bb = ByteBuffer.allocate(8);
        channel.read(bb);
        if (!Arrays.equals(AR_MAGIC, bb.array()))
        {
            throw new UnsupportedOperationException(HexDump.toHex(bb, 0, 8)+" not a deb file");
        }
        pos = channel.position();
        ArHeader packageSection = new ArHeader(channel);
        pos = channel.position();
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
        pos = channel.position();
        Path controlfile = getPath(controlSection.getFilename());
        FilterChannel controlChannel = new FilterChannel(channel, BUF_SIZE, TAR_BLOCK_SIZE, CompressorFactory.input(controlfile), null);
        load(controlChannel, controlRoot);
        pos += controlSection.getSize();
        channel.position(pos);
        ArHeader dataSection = new ArHeader(channel);
        pos = channel.position();
        Path datafile = getPath(dataSection.getFilename());
        FilterChannel dataChannel = new FilterChannel(channel, BUF_SIZE, TAR_BLOCK_SIZE, CompressorFactory.input(datafile), null);
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
        MD5Sums md5Sums = new MD5Sums(controlRoot, root);
        preinst = new MaintainerScript(controlRoot, "preinst");
        postinst = new MaintainerScript(controlRoot, "postinst");
        prerm = new MaintainerScript(controlRoot, "prerm");
        postrm = new MaintainerScript(controlRoot, "postrm");
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
        return control.getConflicts();
    }
    @Override
    public Dependency getConflict(String name)
    {
        return control.getConflict(name);
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
        return control.getProvides();
    }

    @Override
    public Dependency getProvide(String name)
    {
        return control.getProvide(name);
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
        return control.getRequires();
    }

    @Override
    public Dependency getRequire(String name)
    {
        return control.getRequire(name);
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
        return control.getArchitecture();
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
        return control.getDescription();
    }

    @Override
    public PackageManagerAttributeView setCopyright(String cr)
    {
        copyright.setCopyright(cr);
        return this;
    }

    @Override
    public String getCopyright()
    {
        return copyright.getCopyright();
    }

    @Override
    public PackageManagerAttributeView setLicense(String license)
    {
        copyright.setLicense(license);
        return this;
    }

    @Override
    public String getLicense()
    {
        return copyright.getLicense();
    }

    @Override
    public PackageManagerAttributeView setPackageName(String name)
    {
        if (PACKAGE_MATCHER.match(name) == null)
        {
            throw new IllegalArgumentException(name+" syntax error");
        }
        control.setPackage(name);
        changeLog.setPackageName(name);
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
        return this;
    }

    @Override
    public String getOperatingSystem()
    {
        return "linux";
    }

    @Override
    public String getPostInstallation()
    {
        return postinst.getScript();
    }

    @Override
    public String getPostInstallationInterpreter()
    {
        return postinst.getInterpreter();
    }

    @Override
    public String getPostUnInstallation()
    {
        return postrm.getScript();
    }

    @Override
    public String getPostUnInstallationInterpreter()
    {
        return postrm.getInterpreter();
    }

    @Override
    public String getPreInstallation()
    {
        return preinst.getScript();
    }

    @Override
    public String getPreInstallationInterpreter()
    {
        return preinst.getInterpreter();
    }

    @Override
    public String getPreUnInstallation()
    {
        return prerm.getScript();
    }

    @Override
    public String getPreUnInstallationInterpreter()
    {
        return prerm.getInterpreter();
    }

    @Override
    public String getDefaultInterpreter()
    {
        return INTERPRETER;
    }

    @Override
    public PackageManagerAttributeView setPostInstallation(String script, String interpreter)
    {
        postinst.set(interpreter, script);
        return this;
    }

    @Override
    public PackageManagerAttributeView setPostUnInstallation(String script, String interpreter)
    {
        postrm.set(interpreter, script);
        return this;
    }

    @Override
    public PackageManagerAttributeView setPreInstallation(String script, String interpreter)
    {
        preinst.set(interpreter, script);
        return this;
    }

    @Override
    public PackageManagerAttributeView setPreUnInstallation(String script, String interpreter)
    {
        prerm.set(interpreter, script);
        return this;
    }
    /**
     * sets upstream_version
     * @param version
     * @return 
     */
    @Override
    public PackageManagerAttributeView setVersion(String version)
    {
        if (UPSTREAM_VERSION_MATCHER.match(version) == null)
        {
            throw new IllegalArgumentException(version+" syntax error");
        }
        control.setUpstreamVersion(version);
        changeLog.setUpstreamVersion(version);
        return this;
    }
    /**
     * Gets upstream_version
     * @return 
     */
    @Override
    public String getVersion()
    {
        return control.getUpstreamVersion();
    }

    /**
     * Sets debian_revision
     * @param release
     * @return 
     */
    @Override
    public PackageManagerAttributeView setRelease(String release)
    {
        if (DEBIAN_REVISION_MATCHER.match(release) == null)
        {
            throw new IllegalArgumentException(release+" syntax error");
        }
        control.setDebianRevision(release);
        changeLog.setDebianRevision(release);
        return this;
    }
    /**
     * Gets debian_revision
     * @return 
     */
    @Override
    public String getRelease()
    {
        return control.getDebianRevision();
    }

    @Override
    public PackageManagerAttributeView setSummary(String summary)
    {
        control.setSummary(summary);
        return this;
    }

    @Override
    public String getSummary()
    {
        return control.getSummary();
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
        return control.getSection();
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
        return control.getPriority();
    }

    @Override
    public PackageManagerAttributeView setMaintainer(String maintainer)
    {
        control.setMaintainer(maintainer);
        changeLog.setMaintainer(maintainer);
        return this;
    }

    @Override
    public String getMaintainer()
    {
        return control.getMaintainer();
    }

    @Override
    public String name()
    {
        return "org.vesalainen.vfs.pm.deb";
    }

    private void provisionFromPath()
    {
        String[] split = path.getFileName().toString().split("_");
        if (split.length == 3)
        {
            setPackageName(split[0]);
            int idx = split[1].lastIndexOf('-');
            if (idx != -1)
            {
                setVersion(split[1].substring(0, idx));
                setRelease(split[1].substring(idx+1));
            }
            else
            {
                setVersion(split[1]);
                setRelease("0");
            }
            idx = split[2].indexOf(".deb");
            setArchitecture(split[2].substring(0, idx));
        }
        else
        {
            warning("filename %s is illegal", path);
        }
    }

}
