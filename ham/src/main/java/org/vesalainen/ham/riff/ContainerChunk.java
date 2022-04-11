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
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.nio.channels.BufferedFileBuilder;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ContainerChunk extends Chunk
{
    
    protected String type;
    protected MapSet<String, Chunk> subChunks = new HashMapSet<>();
    protected List<Chunk> chunkList = new ArrayList<>();

    public ContainerChunk(ContainerChunk other)
    {
        super(other);
        subChunks.putAll(other.subChunks);
    }

    public ContainerChunk(Chunk other)
    {
        super(other);
        byte[] b = new byte[4];
        data.get(b);
        type = new String(b, StandardCharsets.ISO_8859_1);
        data = data.slice().order(ByteOrder.LITTLE_ENDIAN);
        while (data.hasRemaining())
        {
            Chunk chunk = new Chunk(data);
            add(chunk);
        }
    }

    public ContainerChunk(String id, String type)
    {
        super(id);
        this.type = type;
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        bb.put(type, ISO_8859_1);
        for (Chunk chunk : chunkList)
        {
            chunk.store(bb);
        }
    }

    public void add(Chunk chunk)
    {
        subChunks.add(chunk.getId(), chunk);
        chunkList.add(chunk);
    }
    public String getType()
    {
        return type;
    }

    public MapSet<String, Chunk> getSubChunks()
    {
        return subChunks;
    }
    
}
