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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Chunk
{
    
    protected String id;
    protected int size;
    protected ByteBuffer data;

    public Chunk(String id, ByteBuffer data)
    {
        this.id = id;
        this.size = data.limit();
        this.data = data;
    }

    public Chunk(Chunk other)
    {
        this.id = other.id;
        this.size = other.size;
        this.data = other.data;
    }

    public Chunk(ByteBuffer bb)
    {
        byte[] b = new byte[4];
        bb.get(b);
        id = new String(b, StandardCharsets.ISO_8859_1);
        size = bb.getInt();
        if (size < 0)
        {
            throw new UnsupportedOperationException("chunk data size over " + Integer.MAX_VALUE + " not supported");
        }
        data = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        data.limit(size);
        int position = bb.position();
        position += size;
        int padding = position % 2;
        bb.position(position + padding);
    }

    public void store(ByteBuffer bb)
    {
        bb.put(id.getBytes(US_ASCII));
        bb.putInt(size);
        bb.p
    }
    public String getId()
    {
        return id;
    }

    public int getSize()
    {
        return size;
    }

    public ByteBuffer getData()
    {
        return data;
    }

    public String getSZ(int index, int length)
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
