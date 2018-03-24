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
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import static javax.sound.sampled.AudioFormat.Encoding.*;
import javax.sound.sampled.AudioSystem;
import static org.vesalainen.ham.riff.FormatCode.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Fmt
{
    protected FormatCode formatCode;
    protected int channels;
    protected int samplesPerSecond;
    protected int avgBytesPerSec;
    protected int blockAlign;

    private Fmt(ByteBuffer bb)
    {
        this.formatCode = FormatCode.of(bb.getShort());
        this.channels = bb.getShort();
        this.samplesPerSecond = bb.getInt();
        this.avgBytesPerSec = bb.getInt();
        this.blockAlign = bb.getShort();
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
    public static final Fmt getInstance(Map<String,Chunk> chunkMap)
    {
        Chunk fmt = chunkMap.get("fmt ");
        if (fmt == null)
        {
            throw new IllegalArgumentException("fmt not found");
        }
        switch (fmt.getSize())
        {
            case 16:
                return new Fmt(fmt.getData());
            case 18:
                return new Fmt18(fmt.getData());
            case 40:
                return new Fmt40(fmt.getData());
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
    
    public static class Fmt18 extends Fmt
    {
        protected int bitsPerSample;

        private Fmt18(ByteBuffer bb)
        {
            super(bb);
            this.bitsPerSample = bb.getShort();
            assert avgBytesPerSec == channels*samplesPerSecond*bitsPerSample/8;
            assert blockAlign == channels*bitsPerSample/8;
        }

        public int getBitsPerSample()
        {
            return bitsPerSample;
        }

        
    }
    public static class Fmt40 extends Fmt18
    {
        protected int extSize;
        protected int validBitsPerSample;
        protected int channelMask;
        protected byte[] subFormat;

        private Fmt40(ByteBuffer bb)
        {
            super(bb);
            this.extSize = bb.getShort();
            this.validBitsPerSample = bb.getShort();
            this.channelMask = bb.getInt();
            bb.get(subFormat);
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
