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
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;

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
    byte[] leadMagic = LEAD_MAGIC;
    byte major = 3;
    byte minor;
    short type;
    short archnum;
    String name;
    short osnum;
    short signatureType;
    byte[] leadReserved = new byte[16];
    byte[] headerMagic = HEADER_MAGIC;
    byte[] headerReserved = new byte[4];
    int nindex;
    int hsize;
    IndexRecord[] indexRecords;

    public RPM()
    {
        
    }
    public void load(Path path) throws IOException
    {
        fc = FileChannel.open(path, READ);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, Files.size(path));
        bb.order(BIG_ENDIAN);
        // lead
        bb.get(leadMagic);
        major = bb.get();
        minor = bb.get();
        type = bb.getShort();
        archnum = bb.getShort();
        name = readString(66);
        osnum = bb.getShort();
        signatureType = bb.getShort();
        bb.get(leadReserved);
        // header record
        bb.get(headerMagic);
        bb.get(headerReserved);
        nindex = bb.getInt();
        hsize = bb.getInt();
        indexRecords = new IndexRecord[nindex];
        // index records
        for (int ii=0;ii<nindex;ii++)
        {
            indexRecords[ii] = new IndexRecord();
        }
    }
    void save(ByteBuffer bb) throws IOException
    {
        this.bb = bb;
        bb.order(BIG_ENDIAN);
        // lead
        bb.put(leadMagic);
        bb.put(major);
        bb.put(minor);
        bb.putShort(type);
        bb.putShort(archnum);
        writeString(name, 66);
        bb.putShort(osnum);
        bb.putShort(signatureType);
        bb.put(leadReserved);
        // header record
        bb.put(headerMagic);
        bb.put(headerReserved);
        bb.putInt(nindex);
        bb.putInt(hsize);
        // index records
        for (int ii=0;ii<nindex;ii++)
        {
            indexRecords[ii].save();
        }
    }
    public void save(Path path) throws IOException
    {
        fc = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING);
        bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, Files.size(path));
        save(bb);
    }
    private String readString(int size)
    {
        byte[] buf = new byte[size];
        bb.get(buf);
        int len = buf.length;
        for (int ii=0;ii<len;ii++)
        {
            if (buf[ii] == 0)
            {
                return new String(buf, 0, ii, US_ASCII);
            }
        }
        throw new IllegalArgumentException("no null terminator for string "+bb);
    }
    private void writeString(String str, int size)
    {
        byte[] buf = str.getBytes(US_ASCII);
        if (buf.length >= size)
        {
            throw new IllegalArgumentException(str+" length > "+size);
        }
        bb.put(buf);
        int left = size - buf.length;
        for (int ii=0;ii<left;ii++)
        {
            bb.put((byte)0);
        }
    }
    @Override
    public void close() throws IOException
    {
        if (fc != null)
        {
            fc.close();
        }
    }
    class IndexRecord
    {
        int tag;
        IndexType type;
        int offset;
        int count;

        public IndexRecord()
        {
            tag = bb.getInt();
            type = IndexType.values()[bb.getInt()];
            offset = bb.getInt();
            count = bb.getInt();
        }
        public void save()
        {
            bb.putInt(tag);
            bb.putInt(type.ordinal());
            bb.putInt(offset);
            bb.putInt(count);
        }
    }
}
