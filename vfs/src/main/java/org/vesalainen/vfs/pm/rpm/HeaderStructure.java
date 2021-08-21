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
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.nio.channels.ChannelHelper;
//import static org.vesalainen.pm.rpm.IndexType.*;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.logging.AttachedLogger;
import static org.vesalainen.vfs.pm.rpm.IndexType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class HeaderStructure implements AttachedLogger
{
    private static final byte[] HEADER_MAGIC = new byte[]{(byte) 0x8e, (byte) 0xad, (byte) 0xe8, (byte) 0x01};
    private boolean signature;
    private byte[] magic = HEADER_MAGIC;
    private byte[] reserved = new byte[4];
    private int nindex;
    private int hsize;
    private List<IndexRecord> indexRecords = new ArrayList<>();
    private Map<HeaderTag,IndexRecord> indexMap = new EnumMap<>(HeaderTag.class);
    private ByteBuffer storage;

    public HeaderStructure(SeekableByteChannel ch, boolean signature) throws IOException
    {
        this.signature = signature;
        ChannelHelper.align(ch, 8);
        ByteBuffer bb = ByteBuffer.allocateDirect(16).order(BIG_ENDIAN);
        ch.read(bb);
        bb.flip();
        bb.get(magic);
        if (!Arrays.equals(HEADER_MAGIC, magic))
        {
            throw new IllegalArgumentException("not header structure");
        }
        bb.get(reserved);
        nindex = bb.getInt();
        hsize = bb.getInt();
        // extract storage
        bb = ByteBuffer.allocateDirect(16*nindex+hsize).order(BIG_ENDIAN);
        ch.read(bb);
        bb.position(16*nindex);
        storage = bb.slice();
        storage.limit(hsize);
        bb.position(0);
        // index records
        for (int ii = 0; ii < nindex; ii++)
        {
            IndexRecord indexRecord = loadIndexRecord(bb);
            addIndexRecord(indexRecord);
            finest("loaded: %s", indexRecord);
        }
        storage = null;
    }

    HeaderStructure()
    {
    }
    
    void addIndexRecord(IndexRecord indexRecord)
    {
        indexRecords.add(indexRecord);
        indexMap.put(indexRecord.tag, indexRecord);
    }
    void save(SeekableByteChannel ch) throws IOException
    {
        ChannelHelper.align(ch, 8);
        int size = countSize() + 16;
        ByteBuffer bb = ByteBuffer.allocateDirect(size).order(BIG_ENDIAN);
        bb.clear();
        bb.put(magic);
        bb.put(reserved);
        nindex = indexRecords.size();
        bb.putInt(nindex);
        int hsizeIndex = bb.position();
        bb.putInt(hsize);
        // extract storage
        bb.mark();
        bb.position(bb.position()+16*nindex);
        storage = bb.slice();
        bb.reset();
        // index records
        indexRecords.forEach((i)->i.save(bb));
        storage.flip();
        hsize = storage.remaining();
        bb.putInt(hsizeIndex, hsize);
        bb.position(bb.position()+hsize);
        assert !bb.hasRemaining();
        bb.flip();
        ch.write(bb);
    }
    private int countSize()
    {
        int hdr = 0;
        int stor = 0;
        for (IndexRecord ir : indexRecords)
        {
            hdr += 16;
            IndexType type = ir.getType();
            int typeSize = type.getSize();
            if (typeSize != -1)
            {
                int mod = stor % typeSize;
                stor = mod>0 ? stor + typeSize - mod : stor;
            }
            stor += ir.size();
            System.err.println(ir+" "+hdr+" "+stor);
        }
        return hdr  + stor;
    }
    public void append(Appendable out) throws IOException
    {
        for (IndexRecord ir : indexRecords)
        {
            out.append(ir.toString());
        }
    }
    public IndexRecord getIndexRecord(HeaderTag tag)
    {
        return indexMap.get(tag);
    }
    public Set<HeaderTag> getAllTags()
    {
        return indexMap.keySet();
    }
    private IndexRecord loadIndexRecord(ByteBuffer bb)
    {
        HeaderTag tag = HeaderTag.valueOf(bb.getInt(), signature);
        IndexType type = IndexType.values()[bb.getInt()];
        if (type != tag.getType())
        {
            throw new IllegalArgumentException(tag+" type differs from "+type);
        }
        int offset = bb.getInt();
        int count = bb.getInt();
        switch (type)
        {
            case INT16:
                return new IndexRecord<>(tag, type, offset, count, getInt16(storage, offset, count));
            case INT32:
                return new IndexRecord<>(tag, type, offset, count, getInt32(storage, offset, count));
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return new IndexRecord<>(tag, type, offset, count, getStringArray(storage, offset, count));
            case BIN:
                return new IndexRecord(tag, type, offset, count, getBin(storage, offset, count));
            default:
                throw new UnsupportedOperationException("not supported\n");
        }
    }
    IndexRecord createIndexRecord(HeaderTag tag)
    {
        IndexType type = tag.getType();
        switch (type)
        {
            case INT16:
                return new IndexRecord<>(tag, new ArrayList<>());
            case INT32:
                return new IndexRecord<>(tag, new ArrayList<>());
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return new IndexRecord<>(tag, new ArrayList<>());
            case BIN:
                return new IndexRecord(tag);
            default:
                throw new UnsupportedOperationException("not supported\n");
        }
    }
    public static byte[] getChar(ByteBuffer bb, int index, int count)
    {
        byte[] buf = new byte[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.get(index+ii);
        }
        return buf;
    }
    public static byte[] getInt8(ByteBuffer bb, int index, int count)
    {
        return getChar(bb, index, count);
    }
    public static List<Short> getInt16(ByteBuffer bb, int index, int count)
    {
        List<Short> list = new ArrayList<>();
        for (int ii=0;ii<count;ii++)
        {
            list.add(bb.getShort(index+2*ii));
        }
        return list;
    }
    public static List<Integer> getInt32(ByteBuffer bb, int index, int count)
    {
        List<Integer> list = new ArrayList<>();
        for (int ii=0;ii<count;ii++)
        {
            list.add(bb.getInt(index+4*ii));
        }
        return list;
    }
    public static String getString(ByteBuffer bb, int index)
    {
        return getStringArray(bb, index, 1).get(0);
    }
    public static List<String> getStringArray(ByteBuffer bb, int index, int count)
    {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (int ii=index;count>0;ii++)
        {
            byte cc = bb.get(ii);
            if (cc == 0)
            {
                list.add(sb.toString());
                sb.setLength(0);
                count--;
            }
            else
            {
                sb.append((char)cc);
            }
        }
        return list;
    }
    public static byte[] getBin(ByteBuffer bb, int index, int count)
    {
        byte[] buf = new byte[count];
        for (int ii=0;ii<count;ii++)
        {
            buf[ii] = bb.get(index+ii);
        }
        return buf;
    }
    public static Object getData(ByteBuffer bb, int index, int count, IndexType type)
    {
        switch (type)
        {
            case INT16:
                return getInt16(bb, index, count);
            case INT32:
                return getInt32(bb, index, count);
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return getStringArray(bb, index, count);
            case BIN:
                return getBin(bb, index, count);
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }
    public static <T> int setData(ByteBuffer bb, List<T> list, byte[] bin, IndexType type)
    {
        switch (type)
        {
            case INT16:
                return setInt16(bb, (List<Short>) list);
            case INT32:
                return setInt32(bb, (List<Integer>) list);
            case STRING:
                if (list.size() != 1)
                {
                    throw new IllegalArgumentException("STRING size != 1 = "+list.size());
                }
            case I18NSTRING:
            case STRING_ARRAY:
                return setStringArray(bb, (List<String>) list);
            case BIN:
                return setBin(bb, bin);
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    private static int setInt16(ByteBuffer bb, List<Short> list)
    {
        ByteBuffers.align(bb, 2);
        int position = bb.position();
        for (Short s : list)
        {
            bb.putShort(s.shortValue());
        }
        return position;
    }

    private static int setInt32(ByteBuffer bb, List<Integer> list)
    {
        ByteBuffers.align(bb, 4);
        int position = bb.position();
        for (Integer i : list)
        {
            bb.putInt(i.intValue());
        }
        return position;
    }

    private static int setStringArray(ByteBuffer bb, List<String> list)
    {
        int position = bb.position();
        for (String s : list)
        {
            byte[] bytes = s.getBytes(US_ASCII);
            bb.put(bytes).put((byte)0);
        }
        return position;
    }

    private static int setBin(ByteBuffer bb, byte[] bin)
    {
        int position = bb.position();
        bb.put(bin);
        return position;
    }
    public static int getCount(Object data, IndexType type)
    {
        switch (type)
        {
            case INT16:
            case INT32:
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                List<?> list = (List<?>) data;
                return list.size();
            case BIN:
                byte[] buf = (byte[]) data;
                return buf.length;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    public class IndexRecord<T>
    {
        private HeaderTag tag;
        private IndexType type;
        private int offset;
        private int count;
        private List<T> list;
        private byte[] bin;

        private IndexRecord(HeaderTag tag, List<T> list)
        {
            this.tag = tag;
            this.type = tag.getType();
            this.list = list;
        }

        private IndexRecord(HeaderTag tag)
        {
            this.tag = tag;
            this.type = tag.getType();
        }

        private IndexRecord(HeaderTag tag, IndexType type, int offset, int count, List<T> list)
        {
            this.tag = tag;
            this.type = type;
            this.offset = offset;
            this.count = count;
            this.list = list;
        }

        private IndexRecord(HeaderTag tag, IndexType type, int offset, int count, byte[] bin)
        {
            this.tag = tag;
            this.type = type;
            this.offset = offset;
            this.count = count;
            this.bin = bin;
        }

        public int size()
        {
            int s = 0;
            switch (type)
            {
                case STRING:
                case STRING_ARRAY:
                case I18NSTRING:
                    for (T item : list)
                    {
                        String str = (String) item;
                        s += str.length()+1;
                    }
                    return s;
                default:
                    return getCount()*type.getSize();
            }
        }
        public void save(ByteBuffer bb)
        {
            finest("stored: %s", this);
            offset = setData(storage, list, bin, type);
            count = getCount();
            bb.putInt(tag.getTagValue());
            bb.putInt(type.ordinal());
            bb.putInt(offset);
            bb.putInt(count);
        }
        public boolean contains(T item)
        {
            return list.contains(item);
        }
        public int indexOf(T item)
        {
            return list.indexOf(item);
        }
        public int addItem(T item)
        {
            if (type == STRING)
            {
                list.add(0, item);
            }
            else
            {
                list.add(item);
            }
            return list.size()-1;
        }
        public int setItem(T item, int index)
        {
            if (type == STRING && !list.isEmpty())
            {
                throw new IllegalArgumentException("more that one item in STRING");
            }
            list.set(index, item);
            return list.size()-1;
        }

        public void setBin(byte[] bin)
        {
            this.bin = bin;
        }
        
        public int getCount()
        {
            if (bin != null)
            {
                return bin.length;
            }
            else
            {
                return list.size();
            }
        }
        public List<T> getArray(IndexType it)
        {
            return list;
        }
        public T getSingle(IndexType it)
        {
            List<T> list = getArray(it);
            if (list.size() != 1)
            {
                throw new IllegalArgumentException("count > 1");
            }
            return list.get(0);
        }
        public byte[] getBinValue()
        {
            if (type != BIN)
            {
                throw new IllegalArgumentException("tag data is "+type+" not "+BIN);
            }
            return bin;
        }
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(tag.name()).append(String.format(" type=%s offset=%d count=%d\n", type, offset, count));
            switch (type)
            {
                case INT16:
                    sb.append(String.format("value=%s \n", getInt16(storage, offset, count)));
                    break;
                case INT32:
                    sb.append(String.format("value=%s \n", getInt32(storage, offset, count)));
                    break;
                case STRING:
                case I18NSTRING:
                case STRING_ARRAY:
                    sb.append(String.format("value=%s \n", getStringArray(storage, offset, count)));
                    break;
                case BIN:
                    sb.append(String.format("%s\n", HexDump.toHex(getBin(storage, offset, count))));
                    break;
                default:
                    sb.append("not supported\n");
            }
            return sb.toString();
        }

        public HeaderTag getTag()
        {
            return tag;
        }

        public IndexType getType()
        {
            return type;
        }

        public int getOffset()
        {
            return offset;
        }
        
    }
}
