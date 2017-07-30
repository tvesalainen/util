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
import java.util.Arrays;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
class HeaderStructure
{
    
    byte[] magic = RPM.HEADER_MAGIC;
    byte[] reserved = new byte[4];
    int nindex;
    int hsize;
    IndexRecord[] indexRecords;
    ByteBuffer storage;

    public HeaderStructure(ByteBuffer bb)
    {
        RPM.align(bb, 8);
        bb.get(magic);
        if (!Arrays.equals(RPM.HEADER_MAGIC, magic))
        {
            throw new IllegalArgumentException("not header structure");
        }
        bb.get(reserved);
        nindex = bb.getInt();
        hsize = bb.getInt();
        indexRecords = new IndexRecord[nindex];
        // index records
        for (int ii = 0; ii < nindex; ii++)
        {
            indexRecords[ii] = new IndexRecord(bb);
        }
        storage = bb.slice();
        storage.limit(hsize);
        RPM.skip(bb, hsize);
    } // index records

    public void save(ByteBuffer bb)
    {
        bb.put(magic);
        bb.put(reserved);
        bb.putInt(nindex);
        bb.putInt(hsize);
        // index records
        for (int ii = 0; ii < nindex; ii++)
        {
            indexRecords[ii].save(bb);
        }
        bb.put(storage);
    }
    
    public void append(Appendable out) throws IOException
    {
        for (IndexRecord ir : indexRecords)
        {
            ir.append(out);
        }
    }
    class IndexRecord
    {
        TagValue tag;
        IndexType type;
        int offset;
        int count;

        public IndexRecord(ByteBuffer bb)
        {
            tag = TagValue.valueOf(bb.getInt());
            type = IndexType.values()[bb.getInt()];
            offset = bb.getInt();
            count = bb.getInt();
        }
        public void save(ByteBuffer bb)
        {
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
                case CHAR:
                case INT8:
                    out.append(String.format("value=%s \n", Arrays.toString(Types.getChar(storage, offset, count))));
                    break;
                case INT16:
                    out.append(String.format("value=%s \n", Arrays.toString(Types.getInt16(storage, offset, count))));
                    break;
                case INT32:
                    out.append(String.format("value=%s \n", Arrays.toString(Types.getInt32(storage, offset, count))));
                    break;
                case STRING:
                    out.append(String.format("value='%s' \n", Types.getString(storage, offset)));
                    break;
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
    }
}
