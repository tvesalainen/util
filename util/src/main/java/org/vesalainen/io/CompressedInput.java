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
package org.vesalainen.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.vesalainen.graph.IO;
import org.vesalainen.util.BitArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedInput extends CompressedIO
{
    private InputStream in;

    public CompressedInput(InputStream in) throws IOException
    {
        this(in, null);
    }

    public CompressedInput(InputStream in, String source) throws IOException
    {
        super(source);
        this.in = in;
        header();
    }
    
    private void header() throws IOException
    {
        DataInputStream dis = new DataInputStream(in);
        
        String src = dis.readUTF();
        if (source != null && !source.equals(src))
        {
            throw new IllegalArgumentException("file is not ´"+source);
        }
        source = src;
        short fieldCount = dis.readShort();
        for (int ii=0;ii<fieldCount;ii++)
        {
            String name = dis.readUTF();
            String type = dis.readUTF();
            Property prop = new Property(name, type, bytes);
            bytes += prop.getSize();
            properties.put(name, prop);
        }
        long mostSigBits = dis.readLong();
        long leastSigBits = dis.readLong();
        uuid = new UUID(mostSigBits, leastSigBits);
        bb1 = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN);
        bitArray = new BitArray(bytes);
        bits = bitArray.getArray();
    }
    public float read() throws IOException
    {
        int rc = IO.readFully(in::read, bits);
        if (rc == -1)
        {
            return -1;
        }
        int cnt = 0;
        for (int ii=0;ii<bytes;ii++)
        {
            if (bitArray.isSet(ii))
            {
                bb1.put(ii, (byte) in.read());
                cnt++;
            }
        }
        return (float)cnt/(float)bytes;
    }
    @Override
    public void close() throws IOException
    {
        in.close();
    }
    public void dump(Appendable out) throws IOException
    {
        out.append("Source:").append(source).append('\n');
        out.append("UUID  :").append(uuid.toString()).append('\n');
        List<Property> list = new ArrayList<>(properties.values());
        list.sort(null);
        out.append(list.stream().map((p)->p.getName()).collect(Collectors.joining(", "))).append('\n');
        List<String> names = list.stream().map((p)->p.getName()).collect(Collectors.toList());
        float rc = read();
        while (rc >= 0)
        {
            out.append(names.stream().map(this::get).map((o)->o.toString()).collect(Collectors.joining(", "))).append('\n');
            rc = read();
        }
    }

}
