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
    protected float sampleRate;
    protected int frameSize;
    protected boolean bigEndian;
    protected int size;
    protected ByteBuffer byteBuffer;
    protected FloatFFT_1D fft;
    protected float[] filter;
    protected float[] floatBuffer;
    protected FrequencyCounter frequencyCounter;
    
    private AudioReader(AudioInputStream ais, float sampleRate, int frameSize, boolean bigEndian, int size, float... filter)
    {
        this.ais = ais;
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.bigEndian = bigEndian;
        this.size = size;
        this.filter = filter;
        if ((filter.length % 2) != 0)
        {
            throw new IllegalArgumentException("even number of filter points");
        }
    }
    
    protected void init()
    {
        byteBuffer = ByteBuffer.allocate(size*frameSize);
        byteBuffer.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
        if (filter.length > 0)
        {
            fft = new FloatFFT_1D(size);
            floatBuffer = new float[2*size];
        }
        frequencyCounter = new FrequencyCounter((int) sampleRate);
    }
    public static AudioReader getInstance(TargetDataLine line, int size, float... filter)
    {
        return getInstance(new AudioInputStream(line), size, filter);
    }

    public static AudioReader getInstance(AudioInputStream ais, int size, float... filter)
    {
        return getInstance(ais, ais.getFormat(), size, filter);
    }

    public static AudioReader getInstance(AudioInputStream ais, AudioFormat format, int size, float... filter)
    {
        return getInstance(ais, format.getSampleRate(), format.getFrameSize(), format.isBigEndian(), size, filter);
    }
    /**
     * 
     * @param ais
     * @param frameSize
     * @param bigEndian
     * @param size
     * @param filter Ascending even number of allowed frequency limits in hz. 
     * E.g. 1000, 2000, 4000, 5000 filters other frequences than 1000-2000 and
     * 4000-5000
     * @return 
     */
    public static AudioReader getInstance(AudioInputStream ais, float sampleRate, int frameSize, boolean bigEndian, int size, float... filter)
    {
        AudioReader audioReader;
        switch (frameSize)
        {
            case 1:
                audioReader = new ByteAudioReader(ais, sampleRate, bigEndian, size, filter);
                break;
            case 2:
                audioReader = new ShortAudioReader(ais, sampleRate, bigEndian, size, filter);
                break;
            case 4:
                audioReader = new IntAudioReader(ais, sampleRate, bigEndian, size, filter);
                break;
            default:
                throw new UnsupportedOperationException(frameSize+" not supported");
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
    protected void filter()
    {
        if (filter.length > 0)
        {
            int len = limit();
            for (int ii=0;ii<len;ii++)
            {
                floatBuffer[2*ii] = get(ii);
                floatBuffer[2*ii+1] = 0;
            }
            fft.complexForward(floatBuffer);
            int d = ((int)sampleRate/size);
            int begin = 0;
            for (int ii=0;ii<filter.length;ii+=2)
            {
                int end = Math.min((int)filter[ii]/d, size);
                for (int jj=begin;jj<end;jj++)
                {
                    floatBuffer[2*jj] = 0;
                    floatBuffer[2*jj+1] = 0;
                }
                begin = Math.min((int)filter[ii+1]/d, size);
            }
            fft.complexInverse(floatBuffer, false);
            for (int ii=0;ii<len;ii++)
            {
                float strength = (float) Math.hypot(floatBuffer[2*ii], floatBuffer[2*ii+1]);
                put(ii, strength);
            }
        }
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
        return sampleRate;
    }

    public int getFrameSize()
    {
        return frameSize;
    }
    
    public abstract int getSample() throws IOException;
    
    protected abstract int limit();
    protected abstract int get(int index);
    protected abstract void put(int index, float value);
    
    public static class IntAudioReader extends AudioReader
    {
        private IntBuffer intBuffer;
        
        public IntAudioReader(AudioInputStream ais, float sampleRate, boolean bigEndian, int size, float... filter)
        {
            super(ais, sampleRate, 4, bigEndian, size, filter);
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
                filter();
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
        
        public ShortAudioReader(AudioInputStream ais, float sampleRate, boolean bigEndian, int size, float... filter)
        {
            super(ais, sampleRate, 2, bigEndian, size, filter);
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
                filter();
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
        
        private ByteAudioReader(AudioInputStream ais, float sampleRate, boolean bigEndian, int size, float... filter)
        {
            super(ais, sampleRate, 1, bigEndian, size, filter);
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
                filter();
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
