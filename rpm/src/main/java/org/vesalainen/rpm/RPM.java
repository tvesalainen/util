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
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.vesalainen.nio.FilterByteBuffer;
import static org.vesalainen.rpm.Dependency.*;
import org.vesalainen.rpm.HeaderStructure.IndexRecord;
import static org.vesalainen.rpm.HeaderTag.*;
import static org.vesalainen.rpm.IndexType.*;
import static org.vesalainen.rpm.TagStatus.Required;

/**
 *
 * @author tkv
 */
public class RPM implements AutoCloseable
{
    static final byte[] LEAD_MAGIC = new byte[]{(byte)0xed, (byte)0xab, (byte)0xee, (byte)0xdb};
    static final byte[] HEADER_MAGIC = new byte[]{(byte)0x8e, (byte)0xad, (byte)0xe8, (byte)0x01};
    private FileChannel fc;
    private ByteBuffer bb;
    Lead lead;
    HeaderStructure signature;
    HeaderStructure header;
    List<FileRecord> fileRecords = new ArrayList<>();
    private int signatureStart;
    private int headerStart;
    private int payloadStart;

    public RPM()
    {
        
    }
    public void load(Path path) throws IOException, NoSuchAlgorithmException
    {
        fc = FileChannel.open(path, READ);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, Files.size(path));
        bb.order(BIG_ENDIAN);
        // lead
        lead = new Lead(bb);
        
        signatureStart = bb.position();
        
        signature = new HeaderStructure(bb, true);

        align(bb, 8);
        ByteBuffer rest = bb.slice();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(rest);
        byte[] digest = md5.digest();
        byte[] dig = getBin(RPMSIGTAG_MD5);
        
        if (!Arrays.equals(dig, digest))
        {
            throw new IllegalArgumentException("md5 don't match");
        }
        headerStart = bb.position();
        
        header = new HeaderStructure(bb, false);
        
        checkRequiredTags();
        
        payloadStart = bb.position();
        
        FilterByteBuffer fbb = new FilterByteBuffer(bb, GZIPInputStream::new, null);
        int fileCount = getFileCount();
        for (int ii=0;ii<fileCount;ii++)
        {
            fileRecords.add(new FileRecord(fbb));
        }
        
    }
    void save(ByteBuffer bb) throws IOException
    {
        this.bb = bb;
        bb.order(BIG_ENDIAN);
        // lead
        lead.save(bb);
        
        signature.saveLoaded(bb);
        header.saveLoaded(bb);
    }
    public void save(Path path) throws IOException
    {
        fc = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING);
        bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, Files.size(path));
        save(bb);
    }
    public void checkRequiredTags()
    {
        Set<HeaderTag> required = EnumSet.noneOf(HeaderTag.class);
        for (HeaderTag tag : HeaderTag.values())
        {
            if (tag.getTagStatus() == Required)
            {
                required.add(tag);
            }
        }
        required.removeAll(signature.getAllTags());
        required.removeAll(header.getAllTags());
        if (!required.isEmpty())
        {
            throw new IllegalArgumentException("required tags missing "+required);
        }
    }
    public int getFileCount()
    {
        return getCount(RPMTAG_FILESIZES);
    }
    public int getCount(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getCount();
    }
    public short getInt16(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (short) indexRecord.getSingle(INT16);
    }
    public int getInt32(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (int) indexRecord.getSingle(INT32);
    }
    public String getString(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return (String) indexRecord.getSingle(STRING);
    }
    public List<Short> getInt16Array(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(INT16);
    }
    public List<Integer> getInt32Array(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(INT32);
    }
    public List<String> getStringArray(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(STRING);
    }
    public byte[] getBin(HeaderTag tag)
    {
        IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getBin();
    }
    private IndexRecord getIndexRecord(HeaderTag tag)
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
    private IndexRecord getOrCreateIndexRecord(HeaderTag tag)
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
    public void append(Appendable out) throws IOException
    {
        out.append(String.format("lead %d signature %d header %d payload\n", signatureStart, headerStart, payloadStart));
        out.append(lead.name).append('\n');
        out.append("Signature\n");
        signature.append(out);
        out.append("Header\n");
        header.append(out);
    }
    static void align(ByteBuffer bb, int align)
    {
        bb.position(alignedPosition(bb, align));
    }
    static int alignedPosition(ByteBuffer bb, int align)
    {
        int position = bb.position();
        int mod =  position % align;
        if (mod > 0)
        {
            return position + align-mod;
        }
        else
        {
            return position;
        }
    }
    static void skip(ByteBuffer bb, int skip)
    {
        bb.position(bb.position()+skip);
    }
    @Override
    public void close() throws IOException
    {
        if (fc != null)
        {
            fc.close();
        }
    }
    private <T> boolean contains(HeaderTag tag, T value)
    {
        IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.contains(value);
        }
        return false;
    }
    private <T> int indexOf(HeaderTag tag, T value)
    {
        IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.indexOf(value);
        }
        return -1;
    }
    private int addInt16(HeaderTag tag, short value)
    {
        IndexRecord<Short> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }
    private int addInt32(HeaderTag tag, int value)
    {
        IndexRecord<Integer> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }
    private int addString(HeaderTag tag, String value)
    {
        IndexRecord<String> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }
    private void setBin(HeaderTag tag, byte[] bin)
    {
        IndexRecord indexRecord = getOrCreateIndexRecord(tag);
        indexRecord.setBin(bin);
    }
    public class Builder
    {
        public Builder(String name)
        {
            signature = new HeaderStructure();
            header = new HeaderStructure();
            addString(HeaderTag.RPMTAG_NAME, name);
            addString(HeaderTag.RPMTAG_PAYLOADFORMAT, "cpio");
            addString(HeaderTag.RPMTAG_PAYLOADCOMPRESSOR, "gzip");
            addString(HeaderTag.RPMTAG_PAYLOADFLAGS, "9");
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
            if (!version.isEmpty() && !contains(RPMTAG_REQUIRENAME, "rpmlib(VersionedDependencies)"))
            {
                addRequire("rpmlib(VersionedDependencies)", "3.0.3-1", EQUAL, LESS, RPMLIB);
            }
        }
        public void build(Path dir)
        {
            checkRequiredTags();
            lead = new Lead(String.format("%s-%s-%s", getString(HeaderTag.RPMTAG_NAME), getString(HeaderTag.RPMTAG_VERSION), getString(HeaderTag.RPMTAG_RELEASE)));
            addRequire("rpmlib(CompressedFileNames)", "3.0.4-1", EQUAL, LESS, RPMLIB);
        }
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

        public FileBuilder setTime(int time)
        {
            this.time = time;
            return this;
        }

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
            addString(HeaderTag.RPMTAG_BASENAMES, base);
            if (!contains(HeaderTag.RPMTAG_DIRNAMES, dir))
            {
                index = addString(HeaderTag.RPMTAG_DIRNAMES, dir);
            }
            else
            {
                index = indexOf(HeaderTag.RPMTAG_DIRNAMES, dir);
            }
            addInt32(HeaderTag.RPMTAG_DIRINDEXES, index);
            addInt32(HeaderTag.RPMTAG_FILESIZES, size);
            addInt32(HeaderTag.RPMTAG_FILEMTIMES, time);
            // md5
            ByteBuffer duplicate = content.duplicate();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(duplicate);
            byte[] digest = md5.digest();
            addString(HeaderTag.RPMTAG_FILEMD5S, toHex(digest));
        
            addInt16(HeaderTag.RPMTAG_FILEMODES, mode);
            addInt16(HeaderTag.RPMTAG_FILERDEVS, rdev);
            addString(HeaderTag.RPMTAG_FILELINKTOS, linkTo);
            addInt32(HeaderTag.RPMTAG_FILEFLAGS, flag);
            addString(HeaderTag.RPMTAG_FILEUSERNAME, username);
            addString(HeaderTag.RPMTAG_FILEGROUPNAME, groupname);
            addInt32(HeaderTag.RPMTAG_FILEDEVICES, device);
            addInt32(HeaderTag.RPMTAG_FILEINODES, inode);
            addString(HeaderTag.RPMTAG_FILELANGS, lang);
            
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
            base  = target.substring(0, idx);
            dir = target.substring(idx+1);
        }
        private String toHex(byte[] buf)
        {
            StringBuilder sb = new StringBuilder();
            for (byte b : buf)
            {
                String s = Integer.toHexString(Byte.toUnsignedInt(b));
                if (s.length() < 2)
                {
                    sb.append('0');
                }
                sb.append(s);
            }
            return sb.toString();
        }
    }
}
