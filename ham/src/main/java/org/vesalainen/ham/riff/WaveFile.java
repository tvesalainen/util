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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.Map;
import java.util.Map.Entry;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.riff.FmtChunk.Fmt18Chunk;
import org.vesalainen.nio.channels.BufferedFileBuilder;
import org.vesalainen.util.MapSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WaveFile extends RIFFFile
{
    private FmtChunk fmt;
    private DataChunk data;
    
    public WaveFile()
    {
    }
    public WaveFile(MapSet<String, Chunk> chunkMap)
    {
        super(chunkMap);
        this.fmt = FmtChunk.getInstance(chunkMap);
        if (chunkMap.containsKey("data"))
        {
            this.data = new DataChunk(chunkMap.getSingle("data"));
        }
        else
        {
            throw new IllegalArgumentException("no data chunk");
        }
    }
    public void store(AudioInputStream audioInputStream, Path target, OpenOption... options) throws IOException
    {
        if (options.length == 0)
        {
            options = new OpenOption[]{CREATE, WRITE};
        }
        ContainerChunk riffChunk = new ContainerChunk("RIFF", "WAVE");
        Fmt18Chunk fmtChunk = new Fmt18Chunk(audioInputStream.getFormat());
        riffChunk.add(fmtChunk);
        DataChunk dataChunk = new DataChunk(audioInputStream);
        riffChunk.add(dataChunk);
        if (!listInfoChunk.isEmpty())
        {
            riffChunk.add(listInfoChunk);
        }
        riffChunk.add(new DispChunk("this chunk is here because otherwise windows explorer doesn't show metadata"));
        try (BufferedFileBuilder bfb = new BufferedFileBuilder(4096, true, target, options))
        {
            bfb.order(LITTLE_ENDIAN);
            riffChunk.store(bfb);
        }
    }
    public AudioFormat getAudioFormat()
    {
        return fmt.getAudioFormat();
    }
    public AudioInputStream getAudioInputStream() throws IOException
    {
        InputStream inputStream = data.getInputStream();
        AudioFormat audioFormat = fmt.getAudioFormat();
        return new AudioInputStream(inputStream, audioFormat, inputStream.available()/audioFormat.getFrameSize());
    }

    public ByteBuffer getData()
    {
        return data.getData();
    }
    
    public SampleBuffer getSampleBuffer(int viewLength)
    {
        return new SampleBufferImpl(getAudioFormat(), getData(), viewLength);
    }
}
