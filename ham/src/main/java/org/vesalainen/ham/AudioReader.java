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
package org.vesalainen.ham;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import org.jtransforms.fft.FloatFFT_1D;
import org.vesalainen.ham.hffax.FrequencyCounter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AudioReader
{
    protected AudioInputStream ais;
    protected AudioFormat format;
    protected int size;
    protected ByteBuffer byteBuffer;
    protected FrequencyCounter frequencyCounter;
    
    private AudioReader(AudioInputStream ais, AudioFormat format, int size)
    {
        this.ais = ais;
        this.format = format;
        this.size = size;
    }
    
    protected void init()
    {
        byteBuffer = ByteBuffer.allocate(size*format.getSampleSizeInBits()*format.getChannels()/8);
        byteBuffer.order(format.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);
        frequencyCounter = new FrequencyCounter((int) format.getSampleRate());
    }
    public static AudioReader getInstance(TargetDataLine line, int size)
    {
        return getInstance(new AudioInputStream(line), size);
    }

    public static AudioReader getInstance(AudioInputStream ais, int size)
    {
        return getInstance(ais, ais.getFormat(), size);
    }

    /**
     * 
     * @param ais
     * @param format
     * @param size
     * @return 
     */
    public static AudioReader getInstance(AudioInputStream ais, AudioFormat format, int size)
    {
        if (format.getChannels() != 1)
        {
            format = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize()/format.getChannels(), format.getFrameRate(), format.isBigEndian());
            ais = AudioSystem.getAudioInputStream(format, ais);
        }
        AudioReader audioReader;
        switch ((format.getSampleSizeInBits()/8)/format.getChannels())
        {
            case 1:
                audioReader = new ByteAudioReader(ais, format, size);
                break;
            case 2:
                audioReader = new ShortAudioReader(ais, format, size);
                break;
            case 4:
                audioReader = new IntAudioReader(ais, format, size);
                break;
            default:
                throw new UnsupportedOperationException(format+" not supported");
        }
        audioReader.init();
        return audioReader;
    }
    
    protected boolean fill() throws IOException
    {
        int rc = ais.read(byteBuffer.array());
        if (rc == -1)
        {
            return false;
        }
        byteBuffer.position(0).limit(rc);
        return byteBuffer.hasRemaining();
    }
    /**
     * Returns next frequency that deviates more than delta from previous.
     * @param delta
     * @return
     * @throws IOException 
     */
    public float getNextFrequency(float delta) throws IOException
    {
        float last = frequencyCounter.getFrequency();
        float min = last-delta;
        float max = last+delta;
        while (true)
        {
            int sample = getSample();
            if (frequencyCounter.update(sample))
            {
                float next = frequencyCounter.getFrequency();
                if (next > max || next < min)
                {
                    return next;
                }
            }
        }
    }
    /**
     * Reads one half-wave and returns frequency
     * @return
     * @throws IOException 
     */
    public float getHalfWave() throws IOException
    {
        while (true)
        {
            int sample = getSample();
            if (frequencyCounter.update(sample))
            {
                return frequencyCounter.getFrequency();
            }
        }
    }
    /**
     * Returns microseconds from start of sample
     * @return 
     */
    public long getMicros()
    {
        return frequencyCounter.getMicros();
    }

    public float getAmplitude()
    {
        return frequencyCounter.getAmplitude();
    }

    public float getSampleRate()
    {
        return format.getSampleRate();
    }

    public int getFrameSize()
    {
        return format.getSampleSizeInBits()/8;
    }
    
    public abstract int getSample() throws IOException;
    
    protected abstract int limit();
    protected abstract int get(int index);
    protected abstract void put(int index, float value);
    
    public static class IntAudioReader extends AudioReader
    {
        private IntBuffer intBuffer;
        
        public IntAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected void init()
        {
            super.init();
            intBuffer = byteBuffer.asIntBuffer();
            intBuffer.compact();
            byteBuffer.compact();
        }

        @Override
        public int getSample() throws IOException
        {
            if (!intBuffer.hasRemaining())
            {
                if (!fill())
                {
                    throw new EOFException();
                }
                intBuffer.limit(byteBuffer.limit()/4).position(0);
            }
            return intBuffer.get();
        }

        @Override
        protected int limit()
        {
            return intBuffer.limit();
        }

        @Override
        protected int get(int index)
        {
            return intBuffer.get(index);
        }

        @Override
        protected void put(int index, float value)
        {
            intBuffer.put(index, (int) value);
        }
        
    }
    public static class ShortAudioReader extends AudioReader
    {
        private ShortBuffer shortBuffer;
        
        public ShortAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected void init()
        {
            super.init();
            shortBuffer = byteBuffer.asShortBuffer();
            shortBuffer.compact();
            byteBuffer.compact();
        }

        @Override
        public int getSample() throws IOException
        {
            if (!shortBuffer.hasRemaining())
            {
                if (!fill())
                {
                    throw new EOFException();
                }
                shortBuffer.limit(byteBuffer.limit()/2).position(0);
            }
            return shortBuffer.get();
        }
        @Override
        protected int limit()
        {
            return shortBuffer.limit();
        }

        @Override
        protected int get(int index)
        {
            return shortBuffer.get(index);
        }

        @Override
        protected void put(int index, float value)
        {
            shortBuffer.put(index, (short) value);
        }
        
        
    }
    public static class ByteAudioReader extends AudioReader
    {
        
        private ByteAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected void init()
        {
            super.init();
            byteBuffer.compact();
        }

        @Override
        public int getSample() throws IOException
        {
            if (!byteBuffer.hasRemaining())
            {
                if (!fill())
                {
                    throw new EOFException();
                }
            }
            return byteBuffer.get();
        }
        @Override
        protected int limit()
        {
            return byteBuffer.limit();
        }

        @Override
        protected int get(int index)
        {
            return byteBuffer.get(index);
        }

        @Override
        protected void put(int index, float value)
        {
            byteBuffer.put(index, (byte) value);
        }
        
    }
}
