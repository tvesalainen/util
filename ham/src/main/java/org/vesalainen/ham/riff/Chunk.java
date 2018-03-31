/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.riff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.US_ASCII;
import org.vesalainen.nio.channels.BufferedFileBuilder;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Chunk extends JavaLogging
{
    
    protected String id;
    protected int size;
    protected ByteBuffer data;

    public Chunk(String id)
    {
        super(Chunk.class);
        if (id.length() != 4)
        {
            throw new IllegalArgumentException(id);
        }
        this.id = id;
    }

    public Chunk(Chunk other)
    {
        super(Chunk.class);
        this.id = other.id;
        this.size = other.size;
        this.data = other.data;
    }

    public Chunk(ByteBuffer bb)
    {
        super(Chunk.class);
        byte[] b = new byte[4];
        bb.get(b);
        id = new String(b, StandardCharsets.ISO_8859_1);
        size = bb.getInt();
        if (size < 0)
        {
            throw new UnsupportedOperationException("chunk data size over " + Integer.MAX_VALUE + " not supported");
        }
        data = bb.slice().asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        data.limit(size);
        int position = bb.position();
        position += size;
        int padding = position % 2;
        bb.position(position + padding);
    }

    public void store(BufferedFileBuilder bb) throws IOException
    {
        bb.put(id, US_ASCII);
        long sizePos = bb.position();
        bb.skip(4);
        storeData(bb);
        long curPos = bb.position();
        bb.position(sizePos);
        bb.putInt((int) (curPos-sizePos-4));
        bb.position(curPos);
        if (curPos%2 != 0)
        {
            bb.put((byte)0);
        }
    }
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        throw new UnsupportedOperationException("not supported yet");
    }
    
    public String getId()
    {
        return id;
    }

    public int getSize()
    {
        return size;
    }

    public final String getSZ(int index, int length)
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<length;ii++)
        {
            char cc = (char) data.get(ii+index);
            if (cc == 0)
            {
                break;
            }
            sb.append(cc);
        }
        return sb.toString();
    }
    @Override
    public String toString()
    {
        return "Chunk{" + "id=" + id + ", size=" + size + '}';
    }
    
}
