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
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.GZIPChannel;
import org.vesalainen.util.HexUtil;
import org.vesalainen.vfs.Root;
import org.vesalainen.vfs.VirtualFileStore;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import org.vesalainen.vfs.arch.FileFormat;
import org.vesalainen.vfs.arch.Header;
import org.vesalainen.vfs.arch.cpio.CPIOHeader;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.pm.ChangeLog;
import org.vesalainen.vfs.pm.Condition;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;
import org.vesalainen.vfs.pm.rpm.HeaderStructure.IndexRecord;
import static org.vesalainen.vfs.pm.rpm.HeaderTag.*;
import org.vesalainen.vfs.unix.UnixFileAttributeView;
import org.vesalainen.vfs.unix.UnixFileAttributes;
import org.vesalainen.vfs.pm.Dependency;
import org.vesalainen.vfs.pm.SimpleChangeLog;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://refspecs.linux-foundation.org/LSB_4.0.0/LSB-Core-generic/LSB-Core-generic/book1.html">Linux Standard Base Core Specification 4.0</a>
 */
public final class RPMFileSystem extends ArchiveFileSystem implements PackageManagerAttributeView
{
    private static final String INTERPRETER = "/bin/sh";

    private Lead lead;
    private HeaderStructure signature;
    private HeaderStructure header;
    private MessageDigest md5;
    private final Root root;

    public RPMFileSystem(VirtualFileSystemProvider provider, Path path, Map<String, ?> env) throws IOException
    {
        this(provider, path, CPIOHeader::new, getOpenOptions(path, env), getFileAttributes(env), getFileFormat(path, env));
    }

    private RPMFileSystem(
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
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new UnsupportedOperationException(ex);
        }
        if (isReadOnly())
        {
            loadRPM();
        }
        else
        {
            signature = new HeaderStructure();
            header = new HeaderStructure();
            addString(RPMTAG_PAYLOADFORMAT, "cpio");
            addString(RPMTAG_PAYLOADCOMPRESSOR, "gzip");
            addString(RPMTAG_PAYLOADFLAGS, "9");
            provisionFromPath();
            setOperatingSystem("linux");
        }
    }

    @Override
    protected Stream<Path> walk(Root root) throws IOException
    {
        return super.walk(root).filter((p)->!Files.isDirectory(p));
    }

    private void loadRPM() throws IOException
    {
        lead = new Lead(channel);
        signature = new HeaderStructure(channel, true);
        ChannelHelper.align(channel, 8);
        int restSize = (int) (channel.size()-channel.position());
        int sigSize = getInt32(RPMSIGTAG_SIZE);
        if (sigSize != restSize)
        {
            throw new IllegalArgumentException("sig size don't match");
        }
        md5.reset();
        UpdateCounter counter = new UpdateCounter(md5::update);
        SeekableByteChannel md5Channel = ChannelHelper.traceableChannel(channel, counter);
        header = new HeaderStructure(md5Channel, false);
        GZIPChannel gzipChannel = new GZIPChannel(path, md5Channel, 4096, 8, openOptions);
        load(gzipChannel, root); // CPIO
        
        byte[] digest = md5.digest();
        byte[] dig = getBin(RPMSIGTAG_MD5);
        if (!Arrays.equals(dig, digest))
        {
            throw new IllegalArgumentException("md5 don't match");
        }
        // set file attributes
        List<Integer> sizes = getInt32Array(RPMTAG_FILESIZES);
        List<String> md5List = getStringArray(RPMTAG_FILEMD5S);
        List<Integer> fileFlags = getInt32Array(RPMTAG_FILEFLAGS);
        List<String> users = getStringArray(RPMTAG_FILEUSERNAME);
        List<String> groups = getStringArray(RPMTAG_FILEGROUPNAME);
        List<String> langs = getStringArray(RPMTAG_FILELANGS);
        UserPrincipalLookupService upls = getUserPrincipalLookupService();
        int index = 0;
        for (String fn : getFilenames())
        {
            Path p = getPath(fn);
            if (Files.exists(p))
            {
                // checks
                if (Files.isRegularFile(p))
                {
                    if (Files.size(p) != sizes.get(index))
                    {
                        throw new IllegalArgumentException(p+" sizes dont match");
                    }
                    byte[] fileDigest = (byte[]) Files.getAttribute(p, MD5);
                    String digStr = HexUtil.toString(fileDigest);
                    if (!digStr.equalsIgnoreCase(md5List.get(index)))
                    {
                        throw new IllegalArgumentException(p+" md5 conflict");
                    }
                }
                // attributes
                UnixFileAttributeView unix = Files.getFileAttributeView(p, UnixFileAttributeView.class);
                PackageFileAttributes.setUsage(p, FileFlag.fromFileFlags(fileFlags.get(index)));
                PackageFileAttributes.setLanguage(p, langs.get(index));
                UserPrincipal user = upls.lookupPrincipalByName(users.get(index));
                unix.setOwner(user);
                GroupPrincipal group = upls.lookupPrincipalByGroupName(groups.get(index));
                unix.setGroup(group);
            }
            index++;
        }
    }

    @Override
    public void close() throws IOException
    {
        if (!readOnly)
        {
            storeRPM();
        }
        channel.close();
    }
    private void storeRPM() throws IOException
    {
        lead = new Lead(getName());
        setFileHeaders();
        addProvide(getString(RPMTAG_NAME), getString(RPMTAG_VERSION), Condition.EQUAL);
        addRequireInt("rpmlib(CompressedFileNames)", "3.0.4-1", RPMDependency.EQUAL, RPMDependency.LESS, RPMDependency.RPMLIB);
        addInt32(RPMTAG_SIZE, getInt32Array(RPMTAG_FILESIZES).stream().collect(Collectors.summingInt((i) -> i)));
        
        // placeholders
        md5.reset();
        setBin(RPMSIGTAG_MD5, md5.digest());
        addInt32(RPMSIGTAG_SIZE, 0);
        
        checkRequiredTags();
        
        lead.save(channel);
        long sigPos = channel.position();
        signature.save(channel);
        
        UpdateCounter counter = new UpdateCounter(md5::update);
        SeekableByteChannel md5Channel = ChannelHelper.traceableChannel(channel, counter);
        
        header.save(md5Channel);
        
        GZIPChannel gzipChannel = new GZIPChannel(path, md5Channel, 4096, 4, openOptions);
        Root root = (Root) getPath("/");
        store(gzipChannel, root);
        gzipChannel.flush();
        // md5
        setBin(RPMSIGTAG_MD5, md5.digest());
        setInt32(RPMSIGTAG_SIZE, counter.getCount(), 0);
        channel.position(sigPos);
        signature.save(channel);
    }
    private void setFileHeaders() throws IOException
    {
        enumerateInodes();
        Root root = (Root) getPath("/");
        walk(root).forEach((p)->
        {
            try
            {
                Path  dir = p.getParent();
                Path base = p.getFileName();
                int index;
                addString(RPMTAG_BASENAMES, base.toString());
                String dirStr = dir.toString()+'/';
                if (!contains(RPMTAG_DIRNAMES, dirStr))
                {
                    index = addString(RPMTAG_DIRNAMES, dirStr);
                }
                else
                {
                    index = indexOf(RPMTAG_DIRNAMES, dirStr);
                }
                UnixFileAttributeView view = Files.getFileAttributeView(p, UnixFileAttributeView.class);
                UnixFileAttributes unix = view.readAttributes();
                addInt32(RPMTAG_DIRINDEXES, index);
                addInt32(RPMTAG_FILESIZES, (int) unix.size());
                addInt32(RPMTAG_FILEMTIMES, (int) unix.lastModifiedTime().to(TimeUnit.SECONDS));
                // md5
                byte[] digest = (byte[]) Files.getAttribute(p, MD5);
                addString(RPMTAG_FILEMD5S, HexUtil.toString(digest));

                addInt16(RPMTAG_FILEMODES, unix.mode());
                addInt16(RPMTAG_FILERDEVS, (short)0);
                if (Files.isSymbolicLink(p))
                {
                    Path link = Files.readSymbolicLink(p);
                    addString(RPMTAG_FILELINKTOS, link.toString());
                }
                else
                {
                    addString(RPMTAG_FILELINKTOS, "");
                }
                Set<FileUse> usage = PackageFileAttributes.getUsage(p);
                
                addInt32(RPMTAG_FILEFLAGS, FileFlag.or(usage));
                UserPrincipal owner = unix.owner();
                addString(RPMTAG_FILEUSERNAME, owner != null ? owner.getName() : "root");
                GroupPrincipal group = unix.group();
                addString(RPMTAG_FILEGROUPNAME, group != null ? group.getName() : "root");
                addInt32(RPMTAG_FILEDEVICES, unix.device());
                addInt32(RPMTAG_FILEINODES, unix.inode());
                String language = PackageFileAttributes.getLanguage(p);
                addString(RPMTAG_FILELANGS, language != null ? language : "");
            }
            catch (IOException ex)
            {
                Logger.getLogger(RPMFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    private String getName()
    {
        return String.format("%s-%s-%s", getString(RPMTAG_NAME), getString(RPMTAG_VERSION), getString(RPMTAG_RELEASE));
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

    /**
     * Set RPMTAG_NAME
     * @param name
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPackageName(String name)
    {
        addString(RPMTAG_NAME, name);
        return this;
    }

    @Override
    public String getPackageName()
    {
        return getString(RPMTAG_NAME);
    }
    
    /**
     * Set RPMTAG_VERSION
     * @param version
     * @return 
     */
    @Override
    public PackageManagerAttributeView setVersion(String version)
    {
        addString(RPMTAG_VERSION, version);
        return this;
    }

    @Override
    public String getVersion()
    {
        return getString(RPMTAG_VERSION);
    }
    
    /**
     * Set RPMTAG_RELEASE
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setRelease(String v)
    {
        addString(RPMTAG_RELEASE, v);
        return this;
    }

    @Override
    public String getRelease()
    {
        return getString(RPMTAG_RELEASE);
    }
    
    /**
     * Set RPMTAG_SUMMARY
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setSummary(String v)
    {
        addString(RPMTAG_SUMMARY, v);
        return this;
    }

    @Override
    public String getSummary()
    {
        return getString(RPMTAG_SUMMARY);
    }
    
    /**
     * Set RPMTAG_DESCRIPTION
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setDescription(String v)
    {
        addString(RPMTAG_DESCRIPTION, v);
        return this;
    }

    @Override
    public String getDescription()
    {
        return getString(RPMTAG_DESCRIPTION);
    }
    
    /**
     * No operation
     * @param copyright
     * @return 
     */
    @Override
    public PackageManagerAttributeView setCopyright(String copyright)
    {
        return this;
    }

    @Override
    public String getCopyright()
    {
        return null;
    }
    
    /**
     * Set RPMTAG_LICENSE
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setLicense(String v)
    {
        addString(RPMTAG_LICENSE, v);
        return this;
    }

    @Override
    public String getLicense()
    {
        return getString(RPMTAG_LICENSE);
    }
    
    /**
     * Set RPMTAG_GROUP
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setApplicationArea(String v)
    {
        addString(RPMTAG_GROUP, v);
        return this;
    }

    @Override
    public String getApplicationArea()
    {
        return getString(RPMTAG_GROUP);
    }
    
    /**
     * Set RPMTAG_OS
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setOperatingSystem(String v)
    {
        addString(RPMTAG_OS, v);
        return this;
    }

    @Override
    public String getOperatingSystem()
    {
        return getString(RPMTAG_OS);
    }
    
    /**
     * Set RPMTAG_ARCH
     * @param v
     * @return 
     */
    @Override
    public PackageManagerAttributeView setArchitecture(String v)
    {
        addString(RPMTAG_ARCH, v);
        return this;
    }

    @Override
    public String getArchitecture()
    {
        return getString(RPMTAG_ARCH);
    }
    
    /**
     * Returns "/bin/sh"
     * @return 
     */
    @Override
    public String getDefaultInterpreter()
    {
        return INTERPRETER;
    }
    /**
     * Set RPMTAG_PREIN and RPMTAG_PREINPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPreInstallation(String script, String interpreter)
    {
        addString(RPMTAG_PREIN, script);
        addString(RPMTAG_PREINPROG, interpreter);
        ensureBinSh();
        return this;
    }

    @Override
    public String getPreInstallation()
    {
        return getString(RPMTAG_PREIN);
    }

    @Override
    public String getPreInstallationInterpreter()
    {
        return getString(RPMTAG_PREINPROG);
    }
    
    /**
     * Set RPMTAG_POSTIN and RPMTAG_POSTINPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPostInstallation(String script, String interpreter)
    {
        addString(RPMTAG_POSTIN, script);
        addString(RPMTAG_POSTINPROG, interpreter);
        ensureBinSh();
        return this;
    }
    @Override
    public String getPostInstallation()
    {
        return getString(RPMTAG_POSTIN);
    }

    @Override
    public String getPostInstallationInterpreter()
    {
        return getString(RPMTAG_POSTINPROG);
    }
    
    /**
     * Set RPMTAG_PREUN and RPMTAG_PREUNPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPreUnInstallation(String script, String interpreter)
    {
        addString(RPMTAG_PREUN, script);
        addString(RPMTAG_PREUNPROG, interpreter);
        ensureBinSh();
        return this;
    }
    @Override
    public String getPreUnInstallation()
    {
        return getString(RPMTAG_PREUN);
    }

    @Override
    public String getPreUnInstallationInterpreter()
    {
        return getString(RPMTAG_PREUNPROG);
    }
    /**
     * Set RPMTAG_POSTUN and RPMTAG_POSTUNPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPostUnInstallation(String script, String interpreter)
    {
        addString(RPMTAG_POSTUN, script);
        addString(RPMTAG_POSTUNPROG, interpreter);
        ensureBinSh();
        return this;
    }
    @Override
    public String getPostUnInstallation()
    {
        return getString(RPMTAG_POSTUN);
    }

    @Override
    public String getPostUnInstallationInterpreter()
    {
        return getString(RPMTAG_POSTUNPROG);
    }

    private void ensureBinSh()
    {
        if (!contains(RPMTAG_REQUIRENAME, "/bin/sh"))
        {
            addRequire("/bin/sh");
        }
    }
    /**
     * Add RPMTAG_PROVIDENAME, RPMTAG_PROVIDEVERSION and RPMTAG_PROVIDEFLAGS
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public PackageManagerAttributeView addProvide(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_PROVIDENAME, name);
        addString(RPMTAG_PROVIDEVERSION, version);
        addInt32(RPMTAG_PROVIDEFLAGS, RPMDependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    @Override
    public Collection<String> getProvides()
    {
        return getStringArray(RPMTAG_PROVIDENAME);
    }

    @Override
    public Dependency getProvide(String name)
    {
        return new DependencyConditionImpl(getString(RPMTAG_PROVIDENAME), getString(RPMTAG_PROVIDEVERSION), RPMDependency.toArray(getInt32(RPMTAG_PROVIDEFLAGS)));
    }
    
    /**
     * Add RPMTAG_REQUIRENAME, RPMTAG_REQUIREVERSION and RPMTAG_REQUIREFLAGS
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public PackageManagerAttributeView addRequire(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_REQUIRENAME, name);
        addString(RPMTAG_REQUIREVERSION, version);
        addInt32(RPMTAG_REQUIREFLAGS, RPMDependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }
    @Override
    public Collection<String> getRequires()
    {
        return getStringArray(RPMTAG_REQUIRENAME);
    }

    @Override
    public Dependency getRequire(String name)
    {
        return new DependencyConditionImpl(getString(RPMTAG_REQUIRENAME), getString(RPMTAG_REQUIREVERSION), RPMDependency.toArray(getInt32(RPMTAG_REQUIREFLAGS)));
    }
    
    private PackageManagerAttributeView addRequireInt(String name, String version, int... dependency)
    {
        addString(RPMTAG_REQUIRENAME, name);
        addString(RPMTAG_REQUIREVERSION, version);
        addInt32(RPMTAG_REQUIREFLAGS, RPMDependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }
    /**
     * Add RPMTAG_CONFLICTNAME, RPMTAG_CONFLICTVERSION and RPMTAG_CONFLICTFLAGS
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public PackageManagerAttributeView addConflict(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_CONFLICTNAME, name);
        addString(RPMTAG_CONFLICTVERSION, version);
        addInt32(RPMTAG_CONFLICTFLAGS, RPMDependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }
    @Override
    public Collection<String> getConflicts()
    {
        return getStringArray(RPMTAG_CONFLICTNAME);
    }
    @Override
    public Dependency getConflict(String name)
    {
        return new DependencyConditionImpl(getString(RPMTAG_CONFLICTNAME), getString(RPMTAG_CONFLICTVERSION), RPMDependency.toArray(getInt32(RPMTAG_CONFLICTFLAGS)));
    }

    private void ensureVersionReq(String version)
    {
        if (!version.isEmpty() && !contains(RPMTAG_REQUIRENAME, "rpmlib(VersionedDependencies)"))
        {
            addRequireInt("rpmlib(VersionedDependencies)", "3.0.3-1", RPMDependency.EQUAL, RPMDependency.LESS, RPMDependency.RPMLIB);
        }
    }
    /**
     * Does nothing.
     * @param priority
     * @return 
     */
    @Override
    public PackageManagerAttributeView setPriority(String priority)
    {
        return this;
    }

    @Override
    public String getPriority()
    {
        return null;
    }
    
    /**
     * Does nothing.
     * @param maintainer
     * @return 
     */
    @Override
    public PackageManagerAttributeView setMaintainer(String maintainer)
    {
        return this;
    }

    @Override
    public String getMaintainer()
    {
        return null;
    }

    @Override
    public PackageManagerAttributeView addChangeLog(ChangeLog changeLog)
    {
        addString(RPMTAG_CHANGELOGNAME, changeLog.getMaintainer());
        addString(RPMTAG_CHANGELOGTEXT, changeLog.getText());
        addInt32(RPMTAG_CHANGELOGTIME, (int) changeLog.getTime().to(TimeUnit.SECONDS));
        return this;
    }

    @Override
    public List<ChangeLog> getChangeLogs()
    {
        List<ChangeLog> list = new ArrayList<>();
        List<String> names = getStringArray(RPMTAG_CHANGELOGNAME);
        List<String> texts = getStringArray(RPMTAG_CHANGELOGTEXT);
        List<Integer> times = getInt32Array(RPMTAG_CHANGELOGTIME);
        if (names.size() != texts.size() || names.size() != times.size())
        {
            throw new IllegalArgumentException("change log sizes differ???");
        }
        int len = names.size();
        for (int ii=0;ii<len;ii++)
        {
            SimpleChangeLog log = new SimpleChangeLog(names.get(ii), FileTime.from(times.get(ii), TimeUnit.SECONDS), texts.get(ii));
            list.add(log);
        }
        return list;
    }

    @Override
    public String name()
    {
        return "org.vesalainen.vfs.pm.rpm";
    }

    private void checkRequiredTags()
    {
        Set<HeaderTag> required = EnumSet.noneOf(HeaderTag.class);
        for (HeaderTag tag : HeaderTag.values())
        {
            if (tag.getTagStatus() == TagStatus.Required)
            {
                required.add(tag);
            }
        }
        required.removeAll(signature.getAllTags());
        required.removeAll(header.getAllTags());
        if (!required.isEmpty())
        {
            throw new IllegalArgumentException("required tags missing " + required);
        }
    }

    public int getFileCount()
    {
        return getCount(HeaderTag.RPMTAG_FILESIZES);
    }

    public int getCount(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getCount();
    }

    public short getInt16(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (short) indexRecord.getSingle(IndexType.INT16);
    }

    public int getInt32(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (int) indexRecord.getSingle(IndexType.INT32);
    }

    public String getString(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (String) indexRecord.getSingle(IndexType.STRING);
    }

    public List<Short> getInt16Array(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.INT16);
    }

    public List<Integer> getInt32Array(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.INT32);
    }

    public List<String> getStringArray(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.STRING);
    }

    public byte[] getBin(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getBinValue();
    }

    protected IndexRecord getIndexRecord(HeaderTag tag)
    {
        if (tag.isSignature())
        {
            return signature.getIndexRecord(tag);
        }
        else
        {
            return header.getIndexRecord(tag);
        }
    }

    protected IndexRecord getOrCreateIndexRecord(HeaderTag tag)
    {
        IndexRecord indexRecord;
        if (tag.isSignature())
        {
            indexRecord = signature.getIndexRecord(tag);
            if (indexRecord == null)
            {
                indexRecord = signature.createIndexRecord(tag);
                signature.addIndexRecord(indexRecord);
            }
        }
        else
        {
            indexRecord = header.getIndexRecord(tag);
            if (indexRecord == null)
            {
                indexRecord = header.createIndexRecord(tag);
                header.addIndexRecord(indexRecord);
            }
        }
        return indexRecord;
    }

    protected <T> boolean contains(HeaderTag tag)
    {
        IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return true;
        }
        return false;
    }

    protected <T> boolean contains(HeaderTag tag, T value)
    {
        IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.contains(value);
        }
        return false;
    }

    protected <T> int indexOf(HeaderTag tag, T value)
    {
        IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.indexOf(value);
        }
        return -1;
    }

    protected final int addInt16(HeaderTag tag, short value)
    {
        IndexRecord<Short> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final int addInt32(HeaderTag tag, int value)
    {
        IndexRecord<Integer> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final int setInt32(HeaderTag tag, int value, int index)
    {
        IndexRecord<Integer> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.setItem(value, index);
    }

    protected final int addString(HeaderTag tag, String value)
    {
        IndexRecord<String> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final void setBin(HeaderTag tag, byte[] bin)
    {
        IndexRecord indexRecord = getOrCreateIndexRecord(tag);
        indexRecord.setBin(bin);
    }

    public List<String> getFilenames()
    {
        List<Integer> ind = getInt32Array(RPMTAG_DIRINDEXES);
        List<String> base = getStringArray(RPMTAG_BASENAMES);
        List<String> dir = getStringArray(RPMTAG_DIRNAMES);
        List<String> list = new ArrayList<>();
        int len = ind.size();
        for (int ii=0;ii<len;ii++)
        {
            list.add(dir.get(ind.get(ii))+base.get(ii));
        }
        return list;
    }

    private void provisionFromPath()
    {
        String[] split = path.getFileName().toString().split("-");
        if (split.length != 3)
        {
            warning(path+" not according to naming convention");
            return;
        }
        String[] split1 = split[2].split("\\.");
        if (split1.length != 3)
        {
            warning(path+" not according to naming convention");
            return;
        }
        setPackageName(split[0]);
        setVersion(split[1]);
        setRelease(split1[0]);
        setArchitecture(split1[1]);
    }

    public static class DependencyConditionImpl implements Dependency
    {
        private String name;
        private String version;
        private Condition[] conditions;

        public DependencyConditionImpl(String name, String version, Condition[] conditions)
        {
            this.name = name;
            this.version = version;
            this.conditions = conditions;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getVersion()
        {
            return version;
        }

        @Override
        public Condition[] getConditions()
        {
            return conditions;
        }
        
    }
}
