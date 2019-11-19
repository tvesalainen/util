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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.util.BitArray;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedInput extends CompressedIO
{
    private InputStream in;
    private Property[] offsets;
    private final Set<String> modified = new HashSet<>();
    private String filename;

    public CompressedInput(Path path) throws IOException
    {
        this(path, null);
    }
    public CompressedInput(Path path, String source) throws IOException
    {
        this(IO.buffer(Files.newInputStream(path)), source);
        filename = path.toString();
    }

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
        int fieldCount = Short.toUnsignedInt(dis.readShort());
        for (int ii=0;ii<fieldCount;ii++)
        {
            String name = dis.readUTF();
            String type = dis.readUTF();
            Property prop = new Property(name, type, bytes);
            properties.put(name, prop);
            bytes += prop.getSize();
        }
        long mostSigBits = dis.readLong();
        long leastSigBits = dis.readLong();
        uuid = new UUID(mostSigBits, leastSigBits);
        bb1 = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN);
        bitArray = new BitArray(bytes);
        bits = bitArray.getArray();
        // ofsets
        offsets = new Property[bytes];
        properties.values().forEach((property) ->
        {
            int offset = property.getOffset();
            int size = property.getSize();
            for (int ii=0;ii<size;ii++)
            {
                offsets[offset+ii] = property;
            }
        });
    }
    public void readAll(PropertySetter setter) throws IOException
    {
        readAll(setter, new HashSet<>(CollectionHelp.create(setter.getProperties())));
    }
    public void readAll(PropertySetter setter, Collection<String> needed) throws IOException
    {
        float rc = read();
        while (rc >= 0)
        {
            setModified(setter, needed);
            rc = read();
        }
    }
    public <T extends PropertySetter & Transactional> void readTransactional(T setter) throws IOException
    {
        readTransactional(setter, new HashSet<>(CollectionHelp.create(setter.getProperties())));
    }
    public <T extends PropertySetter & Transactional> void readTransactional(T setter, Collection<String> needed) throws IOException
    {
        float rc = read();
        while (rc >= 0)
        {
            setter.begin(filename);
            setModified(setter, needed);
            setter.commit(filename, modified);
            rc = read();
        }
    }
    public static <T extends PropertySetter & Transactional> void readTransactional(Stream<Path> paths, T setter) throws IOException
    {
        HashSet<String> needed = new HashSet<>(CollectionHelp.create(setter.getProperties()));
        Iterator<Path> iterator = paths.sorted().iterator();
        while (iterator.hasNext())
        {
            Path path = iterator.next();
            CompressedInput ci = new CompressedInput(path);
            ci.readTransactional(setter, needed);
        }
    }
    public void setModified(PropertySetter setter, Collection<String> needed)
    {
        modified.forEach((name) ->
        {
            if (needed.contains(name))
            {
                Property property = properties.get(name);
                property.set(setter);
            }
        });
    }
    public float read() throws IOException
    {
        int rc = IO.readFully(in::read, bits);
        if (rc == -1)
        {
            return -1;
        }
        modified.clear();
        int cnt = 0;
        for (int ii=0;ii<bytes;ii++)
        {
            if (bitArray.isSet(ii))
            {
                bb1.put(ii, (byte) in.read());
                modified.add(offsets[ii].getName());
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
