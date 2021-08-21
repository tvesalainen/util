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
package org.vesalainen.pm.rpm;

import org.vesalainen.pm.PackageBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static java.nio.file.StandardOpenOption.READ;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.vesalainen.nio.DynamicByteBuffer;
import org.vesalainen.nio.FilterByteBuffer;
import org.vesalainen.nio.file.PathHelper;
import org.vesalainen.nio.file.attribute.PosixHelp;
import static org.vesalainen.pm.rpm.HeaderTag.*;
import org.vesalainen.util.HexUtil;
import org.vesalainen.pm.ComponentBuilder;
import org.vesalainen.pm.Condition;
import org.vesalainen.pm.FileUse;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMBuilder extends RPMBase implements PackageBuilder
{
    private static final String INTERPRETER = "/bin/sh";
    private List<FileBuilder> fileBuilders = new ArrayList<>();
    public RPMBuilder()
    {
        signature = new HeaderStructure();
        header = new HeaderStructure();
        addString(RPMTAG_PAYLOADFORMAT, "cpio");
        addString(RPMTAG_PAYLOADCOMPRESSOR, "gzip");
        addString(RPMTAG_PAYLOADFLAGS, "9");
    }
    /**
     * Set RPMTAG_NAME
     * @param name
     * @return 
     */
    @Override
    public RPMBuilder setPackageName(String name)
    {
        addString(RPMTAG_NAME, name);
        return this;
    }
    /**
     * Set RPMTAG_VERSION
     * @param version
     * @return 
     */
    @Override
    public RPMBuilder setVersion(String version)
    {
        addString(RPMTAG_VERSION, version);
        return this;
    }
    /**
     * Set RPMTAG_RELEASE
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setRelease(String v)
    {
        addString(RPMTAG_RELEASE, v);
        return this;
    }
    /**
     * Set RPMTAG_SUMMARY
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setSummary(String v)
    {
        addString(RPMTAG_SUMMARY, v);
        return this;
    }
    /**
     * Set RPMTAG_DESCRIPTION
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setDescription(String v)
    {
        addString(RPMTAG_DESCRIPTION, v);
        return this;
    }
    /**
     * No operation
     * @param copyright
     * @return 
     */
    @Override
    public RPMBuilder setCopyright(String copyright)
    {
        return this;
    }
    /**
     * Set RPMTAG_LICENSE
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setLicense(String v)
    {
        addString(RPMTAG_LICENSE, v);
        return this;
    }
    /**
     * Set RPMTAG_GROUP
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setApplicationArea(String v)
    {
        addString(RPMTAG_GROUP, v);
        return this;
    }
    /**
     * Set RPMTAG_OS
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setOperatingSystem(String v)
    {
        addString(RPMTAG_OS, v);
        return this;
    }
    /**
     * Set RPMTAG_ARCH
     * @param v
     * @return 
     */
    @Override
    public RPMBuilder setArchitecture(String v)
    {
        addString(RPMTAG_ARCH, v);
        return this;
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
    public RPMBuilder setPreInstallation(String script, String interpreter)
    {
        addString(RPMTAG_PREIN, script);
        addString(RPMTAG_PREINPROG, interpreter);
        ensureBinSh();
        return this;
    }
    /**
     * Set RPMTAG_POSTIN and RPMTAG_POSTINPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public RPMBuilder setPostInstallation(String script, String interpreter)
    {
        addString(RPMTAG_POSTIN, script);
        addString(RPMTAG_POSTINPROG, interpreter);
        ensureBinSh();
        return this;
    }
    /**
     * Set RPMTAG_PREUN and RPMTAG_PREUNPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public RPMBuilder setPreUnInstallation(String script, String interpreter)
    {
        addString(RPMTAG_PREUN, script);
        addString(RPMTAG_PREUNPROG, interpreter);
        ensureBinSh();
        return this;
    }
    /**
     * Set RPMTAG_POSTUN and RPMTAG_POSTUNPROG
     * @param script
     * @param interpreter
     * @return 
     */
    @Override
    public RPMBuilder setPostUnInstallation(String script, String interpreter)
    {
        addString(RPMTAG_POSTUN, script);
        addString(RPMTAG_POSTUNPROG, interpreter);
        ensureBinSh();
        return this;
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
    public RPMBuilder addProvide(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_PROVIDENAME, name);
        addString(RPMTAG_PROVIDEVERSION, version);
        addInt32(RPMTAG_PROVIDEFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }
    /**
     * Add RPMTAG_REQUIRENAME, RPMTAG_REQUIREVERSION and RPMTAG_REQUIREFLAGS
     * @param name
     * @param version
     * @param dependency
     * @return 
     */
    @Override
    public RPMBuilder addRequire(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_REQUIRENAME, name);
        addString(RPMTAG_REQUIREVERSION, version);
        addInt32(RPMTAG_REQUIREFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    private RPMBuilder addRequireInt(String name, String version, int... dependency)
    {
        addString(RPMTAG_REQUIRENAME, name);
        addString(RPMTAG_REQUIREVERSION, version);
        addInt32(RPMTAG_REQUIREFLAGS, Dependency.or(dependency));
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
    public RPMBuilder addConflict(String name, String version, Condition... dependency)
    {
        addString(RPMTAG_CONFLICTNAME, name);
        addString(RPMTAG_CONFLICTVERSION, version);
        addInt32(RPMTAG_CONFLICTFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    private void ensureVersionReq(String version)
    {
        if (!version.isEmpty() && !contains(RPMTAG_REQUIRENAME, "rpmlib(VersionedDependencies)"))
        {
            addRequireInt("rpmlib(VersionedDependencies)", "3.0.3-1", Dependency.EQUAL, Dependency.LESS, Dependency.RPMLIB);
        }
    }
    /**
     * Does nothing.
     * @param priority
     * @return 
     */
    @Override
    public RPMBuilder setPriority(String priority)
    {
        return this;
    }
    /**
     * Does nothing.
     * @param maintainer
     * @return 
     */
    @Override
    public RPMBuilder setMaintainer(String maintainer)
    {
        return this;
    }
    
    /**
     * Add file to package from path.
     * @param source
     * @param target Target path like /opt/application/bin/foo
     * @return
     * @throws IOException 
     */
    @Override
    public FileBuilder addFile(Path source, Path target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb =  new FileBuilder();
        fb.target = target;
        fb.size = (int) Files.size(source);
        try (FileChannel fc = FileChannel.open(source, READ))
        {
            fb.content = fc.map(FileChannel.MapMode.READ_ONLY, 0, fb.size);
        }
        FileTime fileTime = Files.getLastModifiedTime(source);
        fb.time = (int)fileTime.to(TimeUnit.SECONDS);
        fb.compressFilename();
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add file to package from content
     * @param content
     * @param target Target path like /opt/application/bin/foo
     * @return
     * @throws IOException 
     */
    @Override
    public FileBuilder addFile(ByteBuffer content, Path target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb = new FileBuilder();
        fb.content = content;
        fb.target = target;
        fb.size = content.limit();
        fb.compressFilename();
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add directory to package.
     * @param target Target path like /opt/application/bin/foo
     * @return
     * @throws IOException 
     */
    @Override
    public FileBuilder addDirectory(Path target) throws IOException
    {
        checkTarget(target);
        FileBuilder fb = new FileBuilder();
        fb.target = target;
        fb.content = ByteBuffer.allocate(0);
        fb.size = fb.content.limit();
        fb.mode = 040000;
        fb.compressFilename();
        fileBuilders.add(fb);
        return fb;
    }
    /**
     * Add symbolic link to package.
     * @param target Target path like /opt/application/bin/foo
     * @param linkTarget Link target like ../lib/bar
     * @return
     * @throws IOException 
     */
    @Override
    public FileBuilder addSymbolicLink(Path target, Path linkTarget) throws IOException
    {
        checkTarget(target);
        checkTarget(linkTarget);
        FileBuilder fb = new FileBuilder();
        fb.target = target;
        fb.linkTo = linkTarget;
        fb.content = ByteBuffer.wrap(PathHelper.posixString(linkTarget).getBytes(US_ASCII));
        fb.size = fb.content.limit();
        fb.mode = (short) 0120000;
        fb.compressFilename();
        fileBuilders.add(fb);
        return fb;
    }
    
    /**
     * Creates RPM file in dir. Returns Path of created file.
     * @param dir
     * @return
     * @throws IOException 
     */
    @Override
    public Path build(Path dir) throws IOException
    {
        String name = getName();
        lead = new Lead(name);
        addProvide(getString(RPMTAG_NAME), getString(RPMTAG_VERSION), Condition.EQUAL);
        addRequireInt("rpmlib(CompressedFileNames)", "3.0.4-1", Dependency.EQUAL, Dependency.LESS, Dependency.RPMLIB);
        addInt32(RPMTAG_SIZE, getInt32Array(RPMTAG_FILESIZES).stream().collect(Collectors.summingInt((i) -> i)));
        // trailer
        CPIO cpio = new CPIO();
        cpio.namesize = 11;
        fileRecords.add(new FileRecord(cpio, "TRAILER!!!", ByteBuffer.allocate(0)));
        // header
        ByteBuffer hdr = DynamicByteBuffer.create(Integer.MAX_VALUE);
        hdr.order(ByteOrder.BIG_ENDIAN);
        header.save(hdr);
        hdr.flip();
        // payload
        ByteBuffer payload = DynamicByteBuffer.create(Integer.MAX_VALUE);
        payload.order(ByteOrder.BIG_ENDIAN);
        try (final FilterByteBuffer fbb = new FilterByteBuffer(payload, null, GZIPOutputStream::new))
        {
            for (FileRecord fr : fileRecords)
            {
                fr.save(fbb);
            }
        }
        payload.flip();
        // signature
        MessageDigest md5;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException(ex);
        }
        ByteBuffer dupHdr = hdr.duplicate();
        md5.update(dupHdr);
        ByteBuffer dupPayload = payload.duplicate();
        md5.update(dupPayload);
        byte[] digest = md5.digest();
        setBin(RPMSIGTAG_MD5, digest);
        addInt32(RPMSIGTAG_SIZE, hdr.limit() + payload.limit());
        checkRequiredTags();
        ByteBuffer rpm = DynamicByteBuffer.create(Integer.MAX_VALUE);
        rpm.order(ByteOrder.BIG_ENDIAN);
        lead.save(rpm);
        signature.save(rpm);
        align(rpm, 8);
        rpm.put(hdr);
        rpm.put(payload);
        rpm.flip();
        Path path = dir.resolve("lsb-" + name + ".rpm");
        try (final FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            fc.write(rpm);
        }
        return path;
    }

    private String getName()
    {
        return String.format("%s-%s-%s", getString(RPMTAG_NAME), getString(RPMTAG_VERSION), getString(RPMTAG_RELEASE));
    }
    
    public class FileBuilder implements ComponentBuilder
    {
        CPIO cpio = new CPIO();
        ByteBuffer content;
        Path target;
        String base;
        String dir;
        int size;
        int time = (int) (System.currentTimeMillis()/1000);
        short mode = (short) 0100000;   // regular file
        short rdev;
        Path linkTo;
        int flag;
        String username = "root";
        String groupname = "root";
        int device;
        int inode;
        String lang = "";

        private FileBuilder()
        {
        }
        /**
         * Set last modified time.
         * @param time
         * @return 
         */
        @Override
        public FileBuilder setLastModifiedTime(Instant time)
        {
            return setTime((int)time.getEpochSecond());
        }
        /**
         * Set file time as seconds from epoch.
         * @param time
         * @return 
         */
        private FileBuilder setTime(int time)
        {
            this.time = time;
            return this;
        }
        /**
         * Set mode in rwxrwxrwx string. rwxr--r-- = 0744
         * @param mode
         * @return 
         */
        @Override
        public FileBuilder setPermissions(String mode)
        {
            return setMode(PosixHelp.getMode('-'+mode));
        }
        /**
         * Set mode as short. E.g. (short)0744
         * @param mode
         * @return 
         */
        public FileBuilder setMode(short mode)
        {
            this.mode |= mode & 0777;
            return this;
        }

        public FileBuilder setRdev(short rdev)
        {
            this.rdev = rdev;
            return this;
        }
        /**
         * Set FileFlags.
         * @param use
         * @return 
         */
        @Override
        public FileBuilder setUsage(FileUse... use)
        {
            this.flag = FileFlag.or(use);
            return this;
        }
        /**
         * Set file owner
         * @param owner
         * @return 
         */
        @Override
        public FileBuilder setOwner(String owner)
        {
            this.username = owner;
            return this;
        }
        /**
         * Set file group
         * @param group
         * @return 
         */
        @Override
        public FileBuilder setGroup(String group)
        {
            this.groupname = group;
            return this;
        }

        public FileBuilder setDevice(int device)
        {
            this.device = device;
            return this;
        }

        public FileBuilder setInode(int inode)
        {
            this.inode = inode;
            return this;
        }

        public FileBuilder setLang(String lang)
        {
            this.lang = lang;
            return this;
        }
        /**
         * throw UnsupportedOperationException
         * @param attrs
         * @return 
         */
        @Override
        public FileBuilder addFileAttributes(FileAttribute<?>... attrs)
        {
            throw new UnsupportedOperationException("This shouldn't be called.");
        }
        /**
         * Does nothing
         * @param copyright
         * @return 
         */
        @Override
        public FileBuilder setCopyright(String copyright)
        {
            return this;
        }
        /**
         * Does nothing
         * @param license
         * @return 
         */
        @Override
        public FileBuilder setLicense(String license)
        {
            return this;
        }

        @Override
        public void build()
        {
            int index;
            addString(RPMTAG_BASENAMES, base);
            if (!contains(RPMTAG_DIRNAMES, dir))
            {
                index = addString(RPMTAG_DIRNAMES, dir);
            }
            else
            {
                index = indexOf(RPMTAG_DIRNAMES, dir);
            }
            addInt32(RPMTAG_DIRINDEXES, index);
            addInt32(RPMTAG_FILESIZES, size);
            cpio.filesize = size;
            addInt32(RPMTAG_FILEMTIMES, time);
            cpio.mtime = time;
            // md5
            ByteBuffer duplicate = content.duplicate();
            MessageDigest md5;
            try
            {
                md5 = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new RuntimeException(ex);
            }
            md5.update(duplicate);
            byte[] digest = md5.digest();
            addString(RPMTAG_FILEMD5S, HexUtil.toString(digest));
        
            addInt16(RPMTAG_FILEMODES, mode);
            cpio.mode = mode & 0xffff;
            addInt16(RPMTAG_FILERDEVS, rdev);
            addString(RPMTAG_FILELINKTOS, linkTo != null ? PathHelper.posixString(linkTo) : "");
            addInt32(RPMTAG_FILEFLAGS, flag);
            addString(RPMTAG_FILEUSERNAME, username);
            addString(RPMTAG_FILEGROUPNAME, groupname);
            addInt32(RPMTAG_FILEDEVICES, device);
            addInt32(RPMTAG_FILEINODES, inode);
            addString(RPMTAG_FILELANGS, lang);
            
            String trg = PathHelper.posixString(target);
            cpio.namesize = trg.length()+1;
            
            FileRecord fileRecord = new FileRecord(cpio, trg, content);
            fileRecords.add(fileRecord);
            cpio = null;
            content = null;
            target = null;
        }
        private void compressFilename()
        {
            String trg = PathHelper.posixString(target);
            int idx = trg.lastIndexOf('/');
            if (idx == -1)
            {
                throw new IllegalArgumentException("directory missing "+target);
            }
            dir  = trg.substring(0, idx+1);
            base = trg.substring(idx+1);
        }
    }
}
