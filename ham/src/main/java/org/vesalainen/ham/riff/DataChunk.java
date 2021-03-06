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
import java.io.InputStream;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import javax.sound.sampled.AudioInputStream;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.channels.BufferedFileBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DataChunk extends Chunk
{
    private AudioInputStream audioInputStream;
    private byte[] buffer;
    
    public DataChunk(AudioInputStream audioInputStream)
    {
        this(audioInputStream, 4096);
    }
    public DataChunk(AudioInputStream audioInputStream, int bufferSize)
    {
        super("data");
        this.audioInputStream = audioInputStream;
        this.buffer = new byte[bufferSize];
    }

    public DataChunk(Chunk other)
    {
        super(other);
        if (!"data".equals(other.id))
        {
            throw new IllegalArgumentException(other.id);
        }
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        try
        {
            int rc = audioInputStream.read(buffer);
            while (rc != -1)
            {
                bb.put(buffer, 0, rc);
                rc = audioInputStream.read(buffer);
            }
        }
        finally
        {
            audioInputStream.close();
        }
    }
    /**
     * Returns input-stream for data.
     * @return 
     */
    public InputStream getInputStream()
    {
        return new ByteBufferInputStream(getData());
    }
    /**
     * Returns duplicate read-only byte-buffer
     * @return 
     */
    public ByteBuffer getData()
    {
        return data.duplicate().order(LITTLE_ENDIAN);
    }
}
