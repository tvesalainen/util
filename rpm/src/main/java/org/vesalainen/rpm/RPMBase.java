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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.vesalainen.rpm.HeaderTag.*;

/**
 *
 * @author tkv
 */
public class RPMBase
{
    
    static final byte[] LEAD_MAGIC = new byte[]{(byte) 0xed, (byte) 0xab, (byte) 0xee, (byte) 0xdb};
    static final byte[] HEADER_MAGIC = new byte[]{(byte) 0x8e, (byte) 0xad, (byte) 0xe8, (byte) 0x01};
    Lead lead;
    HeaderStructure signature;
    HeaderStructure header;
    List<FileRecord> fileRecords = new ArrayList<>();

    static void align(ByteBuffer bb, int align)
    {
        bb.position(alignedPosition(bb, align));
    }

    static int alignedPosition(ByteBuffer bb, int align)
    {
        int position = bb.position();
        int mod = position % align;
        if (mod > 0)
        {
            return position + align - mod;
        }
        else
        {
            return position;
        }
    }

    static void skip(ByteBuffer bb, int skip)
    {
        bb.position(bb.position() + skip);
    }

    public RPMBase()
    {
    }

    public void checkRequiredTags()
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
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getCount();
    }

    public short getInt16(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return (short) indexRecord.getSingle(IndexType.INT16);
    }

    public int getInt32(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return (int) indexRecord.getSingle(IndexType.INT32);
    }

    public String getString(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return (String) indexRecord.getSingle(IndexType.STRING);
    }

    public List<Short> getInt16Array(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.INT16);
    }

    public List<Integer> getInt32Array(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.INT32);
    }

    public List<String> getStringArray(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getArray(IndexType.STRING);
    }

    public byte[] getBin(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord = getIndexRecord(tag);
        return indexRecord.getBin();
    }

    protected HeaderStructure.IndexRecord getIndexRecord(HeaderTag tag)
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

    protected HeaderStructure.IndexRecord getOrCreateIndexRecord(HeaderTag tag)
    {
        HeaderStructure.IndexRecord indexRecord;
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
        HeaderStructure.IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return true;
        }
        return false;
    }

    protected <T> boolean contains(HeaderTag tag, T value)
    {
        HeaderStructure.IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.contains(value);
        }
        return false;
    }

    protected <T> int indexOf(HeaderTag tag, T value)
    {
        HeaderStructure.IndexRecord<T> indexRecord = getIndexRecord(tag);
        if (indexRecord != null)
        {
            return indexRecord.indexOf(value);
        }
        return -1;
    }

    protected final int addInt16(HeaderTag tag, short value)
    {
        HeaderStructure.IndexRecord<Short> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final int addInt32(HeaderTag tag, int value)
    {
        HeaderStructure.IndexRecord<Integer> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final int addString(HeaderTag tag, String value)
    {
        HeaderStructure.IndexRecord<String> indexRecord = getOrCreateIndexRecord(tag);
        return indexRecord.addItem(value);
    }

    protected final void setBin(HeaderTag tag, byte[] bin)
    {
        HeaderStructure.IndexRecord indexRecord = getOrCreateIndexRecord(tag);
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

}
