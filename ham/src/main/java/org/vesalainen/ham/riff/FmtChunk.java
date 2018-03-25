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
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import static javax.sound.sampled.AudioFormat.Encoding.*;
import static org.vesalainen.ham.riff.FormatCode.*;
import org.vesalainen.nio.channels.BufferedFileBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FmtChunk extends Chunk
{
    protected FormatCode formatCode;
    protected int channels;
    protected int samplesPerSecond;
    protected int avgBytesPerSec;
    protected int blockAlign;

    private FmtChunk(Chunk chunk)
    {
        super(chunk);
        this.formatCode = FormatCode.of(data.getShort());
        this.channels = data.getShort();
        this.samplesPerSecond = data.getInt();
        this.avgBytesPerSec = data.getInt();
        this.blockAlign = data.getShort();
    }

    public FmtChunk(AudioFormat audioFormat)
    {
        super("fmt ");
        this.formatCode = getFormatCode(audioFormat.getEncoding());
        this.channels = audioFormat.getChannels();
        this.samplesPerSecond = (int) audioFormat.getSampleRate();
        this.avgBytesPerSec = samplesPerSecond*audioFormat.getFrameSize();
        this.blockAlign = audioFormat.getFrameSize();
    }

    @Override
    protected void storeData(BufferedFileBuilder bb) throws IOException
    {
        bb.putShort((short) formatCode.getCode());
        bb.putShort((short) channels);
        bb.putInt(samplesPerSecond);
        bb.putInt(avgBytesPerSec);
        bb.putShort((short) blockAlign);
    }
    
    private FormatCode getFormatCode(Encoding encoding)
    {
        if (encoding == Encoding.PCM_SIGNED || encoding == Encoding.PCM_UNSIGNED || encoding == Encoding.PCM_FLOAT)
        {
            return FormatCode.WAVE_FORMAT_PCM;
        }
        if (encoding == Encoding.ALAW)
        {
            return FormatCode.WAVE_FORMAT_ALAW;
        }
        if (encoding == Encoding.ULAW)
        {
            return FormatCode.WAVE_FORMAT_MULAW;
        }
        throw new UnsupportedOperationException(encoding+" not supported");
    }
    public int getBitsPerSample()
    {
        return 8*blockAlign/channels;
    }
    public Encoding getEncoding()
    {
        switch (formatCode)
        {
            case WAVE_FORMAT_PCM:
                if (getBitsPerSample() == 8)
                {
                    return PCM_UNSIGNED;
                }
                else
                {
                    return PCM_SIGNED;
                }
            case WAVE_FORMAT_IEEE_FLOAT:
                return PCM_FLOAT;
            case WAVE_FORMAT_ALAW:
                return ALAW;
            case WAVE_FORMAT_MULAW:
                return ULAW;
            default:
                throw new UnsupportedOperationException(formatCode+" not supported");
        }
    }
    public AudioFormat getAudioFormat()
    {
        return new AudioFormat(getEncoding(), samplesPerSecond, getBitsPerSample(), channels, blockAlign, samplesPerSecond, false);
    }
    public static final FmtChunk getInstance(Map<String,Chunk> chunkMap)
    {
        Chunk fmt = chunkMap.get("fmt ");
        if (fmt == null)
        {
            throw new IllegalArgumentException("fmt chunk not found");
        }
        switch (fmt.getSize())
        {
            case 14:
                return new FmtChunk(fmt);
            case 16:
                return new Fmt16Chunk(fmt);
            case 18:
                return new Fmt18Chunk(fmt);
            case 40:
                return new Fmt40Chunk(fmt);
            default:
                throw new UnsupportedOperationException(fmt.getSize()+"  not supported fmt size");
        }
    }
    public FormatCode getFormatCode()
    {
        return formatCode;
    }

    public int getChannels()
    {
        return channels;
    }

    public int getSamplesPerSecond()
    {
        return samplesPerSecond;
    }

    public int getAvgBytesPerSec()
    {
        return avgBytesPerSec;
    }

    public int getBlockAlign()
    {
        return blockAlign;
    }
    
    public static class Fmt16Chunk extends FmtChunk
    {
        protected int bitsPerSample;

        private Fmt16Chunk(Chunk chunk)
        {
            super(chunk);
            this.bitsPerSample = data.getShort();
            assert avgBytesPerSec == channels*samplesPerSecond*bitsPerSample/8;
            assert blockAlign == channels*bitsPerSample/8;
        }

        public Fmt16Chunk(AudioFormat audioFormat)
        {
            super(audioFormat);
            this.bitsPerSample = audioFormat.getSampleSizeInBits();
        }

        @Override
        protected void storeData(BufferedFileBuilder bb) throws IOException
        {
            super.storeData(bb);
            bb.putShort((short) bitsPerSample);
        }
        @Override
        public int getBitsPerSample()
        {
            return bitsPerSample;
        }
        
    }
    public static class Fmt18Chunk extends Fmt16Chunk
    {
        protected int extSize;

        private Fmt18Chunk(Chunk chunk)
        {
            super(chunk);
            this.extSize = data.getShort();
        }
        public Fmt18Chunk(AudioFormat audioFormat)
        {
            super(audioFormat);
        }
        
        @Override
        protected void storeData(BufferedFileBuilder bb) throws IOException
        {
            super.storeData(bb);
            bb.putShort((short) extSize);
        }
    }
    public static class Fmt40Chunk extends Fmt18Chunk
    {
        protected int validBitsPerSample;
        protected int channelMask;
        protected byte[] subFormat;

        private Fmt40Chunk(Chunk chunk)
        {
            super(chunk);
            this.validBitsPerSample = data.getShort();
            this.channelMask = data.getInt();
            data.get(subFormat);
        }

        public int getExtSize()
        {
            return extSize;
        }

        public int getValidBitsPerSample()
        {
            return validBitsPerSample;
        }

        public int getChannelMask()
        {
            return channelMask;
        }

        public byte[] getSubFormat()
        {
            return subFormat;
        }
        
    }
}
