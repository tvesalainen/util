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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.vesalainen.nio.FilterByteBuffer;
import org.vesalainen.rpm.HeaderStructure.IndexRecord;
import static org.vesalainen.rpm.HeaderTag.*;
import static org.vesalainen.rpm.IndexType.INT16;
import static org.vesalainen.rpm.IndexType.INT32;
import static org.vesalainen.rpm.IndexType.STRING;
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
    private void addInt16(HeaderTag tag, short value)
    {
        IndexRecord<Short> indexRecord = getOrCreateIndexRecord(tag);
        indexRecord.addItem(value);
    }
    private void addInt32(HeaderTag tag, int value)
    {
        IndexRecord<Integer> indexRecord = getOrCreateIndexRecord(tag);
        indexRecord.addItem(value);
    }
    private void addString(HeaderTag tag, String value)
    {
        IndexRecord<String> indexRecord = getOrCreateIndexRecord(tag);
        indexRecord.addItem(value);
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
            lead = new Lead(name);
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
    }
}
