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
package org.vesalainen.vfs.pm.rpm;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import org.vesalainen.vfs.arch.FileFormat;
import org.vesalainen.vfs.arch.Header;
import org.vesalainen.vfs.arch.cpio.CPIOHeader;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.pm.Condition;
import org.vesalainen.vfs.pm.Dependency;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMFileSystem extends ArchiveFileSystem implements PackageManagerAttributeView
{

    private Lead lead;
    private HeaderStructure signature;
    private HeaderStructure header;

    public RPMFileSystem(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        this(provider, path, CPIOHeader::new, getOpenOptions(path, env), getFileAttributes(env), getFileFormat(path, env));
    }

    protected RPMFileSystem(
            VirtualFileSystemProvider provider, 
            Path path, 
            Supplier<Header> headerSupplier, 
            Set<? extends OpenOption> openOptions, 
            FileAttribute<?>[] fileAttributes, FileFormat format) throws IOException
    {
        super(provider, path, headerSupplier, openOptions, fileAttributes, format, openChannel(path, openOptions, fileAttributes));
        checkFormat(format);
        VirtualFileStore defStore = new VirtualFileStore(this, UNIX_VIEW, USER_VIEW);
        defStore.addFileStoreAttributeView(this);
        addFileStore("/",defStore , true);
        if (isReadOnly())
        {
            loadRPM();
        }
    }

    private void loadRPM() throws IOException
    {
        lead = new Lead(channel);
        signature = new HeaderStructure(channel, true);
        header = new HeaderStructure(channel, false);
        load(); // CPIO
    }
    private void checkFormat(FileFormat fmt)
    {
        switch (fmt)
        {
            case CPIO_NEWC:
            case CPIO_CRC:
            case CPIO_ODC:
            case CPIO_BIN:
                break;
            default:
                throw new UnsupportedOperationException(fmt.name());
        }
    }
    private static SeekableByteChannel openChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        return FileChannel.open(path, options, attrs);
    }    

    @Override
    public PackageManagerAttributeView addConflict(String name, String version, Condition... dependency)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getConflicts()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Dependency getConflict(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView addProvide(String name, String version, Condition... dependency)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getProvides()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Dependency getProvide(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView addRequire(String name, String version, Condition... dependency)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getRequires()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Dependency getRequire(String name)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setArchitecture(String architecture)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getArchitecture()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setDescription(String description)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPackageName()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setApplicationArea(String area)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getApplicationArea()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setPriority(String priority)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPriority()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PackageManagerAttributeView setMaintainer(String maintainer)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMaintainer()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String name()
    {
        return "org.vesalainen.vfs.pm.rpm";
    }

}
