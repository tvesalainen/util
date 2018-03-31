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
import static java.nio.ByteOrder.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.channels.FileChannel.MapMode.*;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RIFFFile extends JavaLogging
{
    protected MapSet<String, Chunk> chunkMap;
    protected Map<String, ContainerChunk> listMap;
    protected ListInfoChunk listInfoChunk;

    public RIFFFile()
    {
        super(RIFFFile.class);
        this.listInfoChunk = new ListInfoChunk();
    }

    protected RIFFFile(MapSet<String, Chunk> chunkMap)
    {
        super(RIFFFile.class);
        this.chunkMap = chunkMap;
        if (chunkMap.containsKey("LIST"))
        {
            listMap = new HashMap<>();
            for (Chunk chunk : chunkMap.get("LIST"))
            {
                ContainerChunk listChunk = new ContainerChunk(chunk);
                listMap.put(listChunk.getType(), listChunk);
            }
            if (listMap.containsKey("INFO"))
            {
                this.listInfoChunk = new ListInfoChunk(listMap.get("INFO"));
            }
        }
    }

    public static final RIFFFile open(Path file, OpenOption... options) throws IOException
    {
        try (FileChannel fc = FileChannel.open(file, options))
        {
            MappedByteBuffer mbb = fc.map(READ_ONLY, 0, fc.size());
            mbb.order(LITTLE_ENDIAN);
            Chunk chunk = new Chunk(mbb);
            if (!chunk.getId().equals("RIFF"))
            {
                throw new UnsupportedOperationException(chunk.getId() + " not supported");
            }
            ContainerChunk riffChunk = new ContainerChunk(chunk);
            switch (riffChunk.getType())
            {
                case "WAVE":
                    return new WaveFile(riffChunk.getSubChunks());
                default:
                    throw new UnsupportedOperationException(riffChunk.getType() + " not supported");
            }
        }
    }

    public ListInfoChunk getListInfoChunk()
    {
        return listInfoChunk;
    }

    public void setListInfoChunk(ListInfoChunk listInfoChunk)
    {
        this.listInfoChunk = listInfoChunk;
    }
}
