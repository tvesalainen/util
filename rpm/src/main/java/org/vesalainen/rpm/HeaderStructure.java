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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
class HeaderStructure
{
    boolean signature;
    byte[] magic = RPM.HEADER_MAGIC;
    byte[] reserved = new byte[4];
    int nindex;
    int hsize;
    List<IndexRecord> indexRecords = new ArrayList<>();
    Map<HeaderTag,IndexRecord> indexMap = new HashMap<>();
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
            IndexRecord indexRecord = new IndexRecord(bb);
            indexRecords.add(indexRecord);
            indexMap.put(indexRecord.tag, indexRecord);
        }
        RPM.skip(bb, hsize);
    }

    public void save(ByteBuffer bb)
    {
        RPM.align(bb, 8);
        bb.put(magic);
        bb.put(reserved);
        bb.putInt(indexRecords.size());
        int hsizeIndex = bb.position();
        bb.putInt(hsize);
        // extract storage
        bb.mark();
        bb.position(bb.position()+16*nindex);
        storage = bb.slice();
        bb.reset();
        // index records
        Collections.sort(indexRecords);
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
    class IndexRecord implements Comparable<IndexRecord>
    {
        HeaderTag tag;
        IndexType type;
        int offset;
        int count;
        Object data;

        public IndexRecord(ByteBuffer bb)
        {
            tag = HeaderTag.valueOf(bb.getInt(), signature);
            type = IndexType.values()[bb.getInt()];
            if (type != tag.getType())
            {
                throw new IllegalArgumentException(tag+" type differs from "+type);
            }
            offset = bb.getInt();
            count = bb.getInt();
            data = Types.getData(storage, offset, count, type);
        }
        public void save(ByteBuffer bb)
        {
            offset = Types.setData(storage, data, type);
            count = Types.getCount(data, type);
            bb.putInt(tag.getTagValue());
            bb.putInt(type.ordinal());
            bb.putInt(offset);
            bb.putInt(count);
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

        @Override
        public int compareTo(IndexRecord o)
        {
            return offset - o.offset;
        }
        
    }
}
