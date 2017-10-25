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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import static org.vesalainen.pm.rpm.IndexType.*;
import org.vesalainen.util.HexDump;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
class HeaderStructure
{
    static final byte[] HEADER_MAGIC = new byte[]{(byte) 0x8e, (byte) 0xad, (byte) 0xe8, (byte) 0x01};
    boolean signature;
    byte[] magic = HEADER_MAGIC;
    byte[] reserved = new byte[4];
    int nindex;
    int hsize;
    List<IndexRecord> indexRecords = new ArrayList<>();
    Map<HeaderTag,IndexRecord> indexMap = new EnumMap<>(HeaderTag.class);
    ByteBuffer storage;

    public HeaderStructure(ByteBuffer bb, boolean signature)
    {
        this.signature = signature;
        RPM.align(bb, 8);
        bb.get(magic);
        if (!Arrays.equals(RPM.HEADER_MAGIC, magic))
        {
            throw new IllegalArgumentException("not header structure");
        }
        bb.get(reserved);
        nindex = bb.getInt();
        hsize = bb.getInt();
        // extract storage
        bb.mark();
        bb.position(bb.position()+16*nindex);
        storage = bb.slice();
        storage.limit(hsize);
        bb.reset();
        // index records
        for (int ii = 0; ii < nindex; ii++)
        {
            IndexRecord indexRecord = loadIndexRecord(bb);
            addIndexRecord(indexRecord);
        }
        RPM.skip(bb, hsize);
    }

    HeaderStructure()
    {
    }
    
    void addIndexRecord(IndexRecord indexRecord)
    {
        indexRecords.add(indexRecord);
        indexMap.put(indexRecord.tag, indexRecord);
    }
    void save(ByteBuffer bb)
    {
        RPM.align(bb, 8);
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
        RPM.skip(bb, hsize);
    }
    
    public void append(Appendable out) throws IOException
    {
        for (IndexRecord ir : indexRecords)
        {
            ir.append(out);
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
    IndexRecord loadIndexRecord(ByteBuffer bb)
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
                return new IndexRecord<Short>(tag, type, offset, count, Types.getInt16(storage, offset, count));
            case INT32:
                return new IndexRecord<Integer>(tag, type, offset, count, Types.getInt32(storage, offset, count));
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return new IndexRecord<String>(tag, type, offset, count, Types.getStringArray(storage, offset, count));
            case BIN:
                return new IndexRecord(tag, type, offset, count, Types.getBin(storage, offset, count));
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
                return new IndexRecord<Short>(tag, new ArrayList<>());
            case INT32:
                return new IndexRecord<Integer>(tag, new ArrayList<>());
            case STRING:
            case I18NSTRING:
            case STRING_ARRAY:
                return new IndexRecord<String>(tag, new ArrayList<>());
            case BIN:
                return new IndexRecord(tag);
            default:
                throw new UnsupportedOperationException("not supported\n");
        }
    }
    class IndexRecord<T>
    {
        HeaderTag tag;
        IndexType type;
        int offset;
        int count;
        List<T> list;
        byte[] bin;

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

        public void save(ByteBuffer bb)
        {
            offset = Types.setData(storage, list, bin, type);
            count = getCount();
            bb.putInt(tag.getTagValue());
            bb.putInt(type.ordinal());
            bb.putInt(offset);
            bb.putInt(count);
        }
        boolean contains(T item)
        {
            return list.contains(item);
        }
        int indexOf(T item)
        {
            return list.indexOf(item);
        }
        int addItem(T item)
        {
            if (type == STRING && !list.isEmpty())
            {
                throw new IllegalArgumentException("more that one item in STRING");
            }
            list.add(item);
            return list.size()-1;
        }

        public void setBin(byte[] bin)
        {
            this.bin = bin;
        }
        
        int getCount()
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
        List<T> getArray(IndexType it)
        {
            return list;
        }
        T getSingle(IndexType it)
        {
            List<T> list = getArray(it);
            if (list.size() != 1)
            {
                throw new IllegalArgumentException("count > 1");
            }
            return list.get(0);
        }
        public byte[] getBin()
        {
            if (type != BIN)
            {
                throw new IllegalArgumentException("tag data is "+type+" not "+BIN);
            }
            return bin;
        }
        public void append(Appendable out) throws IOException
        {
            out.append(tag.name()).append(String.format(" type=%s offset=%d count=%d\n", type, offset, count));
            switch (type)
            {
                case INT16:
                    out.append(String.format("value=%s \n", Types.getInt16(storage, offset, count)));
                    break;
                case INT32:
                    out.append(String.format("value=%s \n", Types.getInt32(storage, offset, count)));
                    break;
                case STRING:
                case I18NSTRING:
                case STRING_ARRAY:
                    out.append(String.format("value=%s \n", Types.getStringArray(storage, offset, count)));
                    break;
                case BIN:
                    out.append(String.format("%s\n", HexDump.toHex(Types.getBin(storage, offset, count))));
                    break;
                default:
                    out.append("not supported\n");
            }
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
