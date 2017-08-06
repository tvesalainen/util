/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static java.nio.file.StandardOpenOption.READ;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.vesalainen.nio.DynamicByteBuffer;
import org.vesalainen.nio.FilterByteBuffer;
import org.vesalainen.nio.file.attribute.PosixHelp;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_BASENAMES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_DIRINDEXES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_DIRNAMES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEDEVICES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEFLAGS;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEGROUPNAME;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEINODES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILELANGS;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILELINKTOS;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEMD5S;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEMODES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEMTIMES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILERDEVS;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILESIZES;
import static org.vesalainen.rpm.HeaderTag.RPMTAG_FILEUSERNAME;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author tkv
 */
public class Builder extends RPMBase
{
    
    public Builder()
    {
        signature = new HeaderStructure();
        header = new HeaderStructure();
        addString(HeaderTag.RPMTAG_PAYLOADFORMAT, "cpio");
        addString(HeaderTag.RPMTAG_PAYLOADCOMPRESSOR, "gzip");
        addString(HeaderTag.RPMTAG_PAYLOADFLAGS, "9");
    }

    public Builder setName(String name)
    {
        addString(HeaderTag.RPMTAG_NAME, name);
        return this;
    }

    public Builder setVersion(String version)
    {
        addString(HeaderTag.RPMTAG_VERSION, version);
        return this;
    }

    public Builder setRelease(String v)
    {
        addString(HeaderTag.RPMTAG_RELEASE, v);
        return this;
    }

    public Builder setSummary(String v)
    {
        addString(HeaderTag.RPMTAG_SUMMARY, v);
        return this;
    }

    public Builder setDescription(String v)
    {
        addString(HeaderTag.RPMTAG_DESCRIPTION, v);
        return this;
    }

    private Builder setSize(int v)
    {
        addInt32(HeaderTag.RPMTAG_SIZE, v);
        return this;
    }

    public Builder setLicense(String v)
    {
        addString(HeaderTag.RPMTAG_LICENSE, v);
        return this;
    }

    public Builder setGroup(String v)
    {
        addString(HeaderTag.RPMTAG_GROUP, v);
        return this;
    }

    public Builder setOs(String v)
    {
        addString(HeaderTag.RPMTAG_OS, v);
        return this;
    }

    public Builder setArch(String v)
    {
        addString(HeaderTag.RPMTAG_ARCH, v);
        return this;
    }

    public Builder setPreIn(String v)
    {
        addString(HeaderTag.RPMTAG_PREIN, v);
        addString(HeaderTag.RPMTAG_PREINPROG, "/bin/sh");
        ensureBinSh();
        return this;
    }

    public Builder setPostIn(String v)
    {
        addString(HeaderTag.RPMTAG_POSTIN, v);
        addString(HeaderTag.RPMTAG_POSTINPROG, "/bin/sh");
        ensureBinSh();
        return this;
    }

    public Builder setPreUn(String v)
    {
        addString(HeaderTag.RPMTAG_PREUN, v);
        addString(HeaderTag.RPMTAG_PREUNPROG, "/bin/sh");
        ensureBinSh();
        return this;
    }

    public Builder setPostUn(String v)
    {
        addString(HeaderTag.RPMTAG_POSTUN, v);
        addString(HeaderTag.RPMTAG_POSTUNPROG, "/bin/sh");
        ensureBinSh();
        return this;
    }

    private void ensureBinSh()
    {
        if (!contains(HeaderTag.RPMTAG_REQUIRENAME, "/bin/sh"))
        {
            addRequire("/bin/sh");
        }
    }

    public Builder addProvide(String name)
    {
        return addProvide(name, "");
    }

    public Builder addProvide(String name, String version, int... dependency)
    {
        addString(HeaderTag.RPMTAG_PROVIDENAME, name);
        addString(HeaderTag.RPMTAG_PROVIDEVERSION, version);
        addInt32(HeaderTag.RPMTAG_PROVIDEFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    public Builder addRequire(String name)
    {
        return addRequire(name, "");
    }

    public Builder addRequire(String name, String version, int... dependency)
    {
        addString(HeaderTag.RPMTAG_REQUIRENAME, name);
        addString(HeaderTag.RPMTAG_REQUIREVERSION, version);
        addInt32(HeaderTag.RPMTAG_REQUIREFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    public Builder addConflict(String name)
    {
        return addConflict(name, "");
    }

    public Builder addConflict(String name, String version, int... dependency)
    {
        addString(HeaderTag.RPMTAG_CONFLICTNAME, name);
        addString(HeaderTag.RPMTAG_CONFLICTVERSION, version);
        addInt32(HeaderTag.RPMTAG_CONFLICTFLAGS, Dependency.or(dependency));
        ensureVersionReq(version);
        return this;
    }

    private void ensureVersionReq(String version)
    {
        if (!version.isEmpty() && !contains(HeaderTag.RPMTAG_REQUIRENAME, "rpmlib(VersionedDependencies)"))
        {
            addRequire("rpmlib(VersionedDependencies)", "3.0.3-1", Dependency.EQUAL, Dependency.LESS, Dependency.RPMLIB);
        }
    }

    public FileBuilder addFile(Path source, String target) throws IOException
    {
        return new FileBuilder(source, target);
    }

    public FileBuilder addFile(ByteBuffer content, String target) throws IOException
    {
        return new FileBuilder(content, target);
    }

    public void build(Path dir) throws IOException, NoSuchAlgorithmException
    {
        String name = getName();
        lead = new Lead(name);
        addProvide(getString(HeaderTag.RPMTAG_NAME), getString(HeaderTag.RPMTAG_VERSION), Dependency.EQUAL);
        addRequire("rpmlib(CompressedFileNames)", "3.0.4-1", Dependency.EQUAL, Dependency.LESS, Dependency.RPMLIB);
        addInt32(HeaderTag.RPMTAG_SIZE, getInt32Array(HeaderTag.RPMTAG_FILESIZES).stream().collect(Collectors.summingInt((i) -> i)));
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
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        ByteBuffer dupHdr = hdr.duplicate();
        md5.update(dupHdr);
        ByteBuffer dupPayload = payload.duplicate();
        md5.update(dupPayload);
        byte[] digest = md5.digest();
        setBin(HeaderTag.RPMSIGTAG_MD5, digest);
        addInt32(HeaderTag.RPMSIGTAG_SIZE, hdr.limit() + payload.limit());
        checkRequiredTags();
        ByteBuffer rpm = DynamicByteBuffer.create(Integer.MAX_VALUE);
        rpm.order(ByteOrder.BIG_ENDIAN);
        lead.save(rpm);
        signature.save(rpm);
        align(rpm, 8);
        rpm.put(hdr);
        rpm.put(payload);
        rpm.flip();
        Path path = dir.resolve(name + ".rpm");
        try (final FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            fc.write(rpm);
        }
    }

    private String getName()
    {
        return String.format("%s-%s-%s", getString(HeaderTag.RPMTAG_NAME), getString(HeaderTag.RPMTAG_VERSION), getString(HeaderTag.RPMTAG_RELEASE));
    }
    
    public class FileBuilder
    {
        CPIO cpio = new CPIO();
        ByteBuffer content;
        String target;
        String base;
        String dir;
        int size;
        int time = (int) (System.currentTimeMillis()/1000);
        short mode;
        short rdev;
        String linkTo = "";
        int flag;
        String username = "root";
        String groupname = "root";
        int device;
        int inode;
        String lang = "";
        
        public FileBuilder(Path source, String target) throws IOException
        {
            this.target = target;
            size = (int) Files.size(source);
            try (FileChannel fc = FileChannel.open(source, READ))
            {
                content = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
            }
            FileTime fileTime = Files.getLastModifiedTime(source);
            time = (int)fileTime.to(TimeUnit.SECONDS);
            compressFilename();
        }

        public FileBuilder(ByteBuffer content, String target)
        {
            this.content = content;
            this.target = target;
            size = content.limit();
            compressFilename();
        }
        /**
         * Seet file time.
         * @param time
         * @return 
         */
        public FileBuilder setTime(Instant time)
        {
            return setTime((int)time.getEpochSecond());
        }
        /**
         * Set file time as seconds from epoch.
         * @param time
         * @return 
         */
        public FileBuilder setTime(int time)
        {
            this.time = time;
            return this;
        }
        /**
         * Set mode in rwxrwxrwx string. rwxr--r-- = 0744
         * @param mode
         * @return 
         */
        public FileBuilder setMode(String mode)
        {
            return setMode(PosixHelp.getMode(mode));
        }
        /**
         * Set mode as short. E.g. (short)0744
         * @param mode
         * @return 
         */
        public FileBuilder setMode(short mode)
        {
            this.mode = mode;
            return this;
        }

        public FileBuilder setRdev(short rdev)
        {
            this.rdev = rdev;
            return this;
        }

        public FileBuilder setLinkTo(String linkTo)
        {
            this.linkTo = linkTo;
            return this;
        }

        public FileBuilder setFlag(FileFlag... flags)
        {
            this.flag = FileFlag.or(flags);
            return this;
        }

        public FileBuilder setUsername(String username)
        {
            this.username = username;
            return this;
        }

        public FileBuilder setGroupname(String groupname)
        {
            this.groupname = groupname;
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

        public void build() throws NoSuchAlgorithmException
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
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(duplicate);
            byte[] digest = md5.digest();
            addString(RPMTAG_FILEMD5S, HexUtil.toString(digest));
        
            addInt16(RPMTAG_FILEMODES, mode);
            cpio.mode = mode & 0xffff;
            addInt16(RPMTAG_FILERDEVS, rdev);
            addString(RPMTAG_FILELINKTOS, linkTo);
            addInt32(RPMTAG_FILEFLAGS, flag);
            addString(RPMTAG_FILEUSERNAME, username);
            addString(RPMTAG_FILEGROUPNAME, groupname);
            addInt32(RPMTAG_FILEDEVICES, device);
            addInt32(RPMTAG_FILEINODES, inode);
            addString(RPMTAG_FILELANGS, lang);
            cpio.namesize = target.length()+1;
            
            FileRecord fileRecord = new FileRecord(cpio, target, content);
            fileRecords.add(fileRecord);
            cpio = null;
            content = null;
            target = null;
        }
        private void compressFilename()
        {
            int idx = target.lastIndexOf('/');
            if (idx == -1)
            {
                throw new IllegalArgumentException("directory missing "+target);
            }
            dir  = target.substring(0, idx+1);
            base = target.substring(idx+1);
        }
    }
}
