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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.hffax.FrequencyCounter;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AudioReader<T extends Buffer>
{
    private static final int WAVES_PER_BUFFER = 10;
    protected AudioInputStream ais;
    protected AudioFormat format;
    protected int size;
    protected ByteBuffer byteBuffer;
    protected FrequencyCounter frequencyCounter;
    protected float maxAmplitude;
    protected List<DataListener> dataListeners = new ArrayList<>();
    protected T buffer;
    protected IntArray intArray;
    
    private AudioReader(AudioInputStream ais, AudioFormat format, int size)
    {
        this.ais = ais;
        this.format = format;
        this.size = size;
        this.maxAmplitude = (float) Math.pow(2, format.getSampleSizeInBits());
    }
    
    private void init()
    {
        byteBuffer = ByteBuffer.allocate(size*format.getSampleSizeInBits()*format.getChannels()/8);
        byteBuffer.order(format.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);
        frequencyCounter = new FrequencyCounter((int) format.getSampleRate());
        buffer = initBuffers();
        byteBuffer.compact();
    }
    protected abstract T initBuffers();
    protected void fireDataListeners()
    {
        for (DataListener listener : dataListeners)
        {
            listener.data(intArray);
        }
    }
    public static AudioReader getInstance(TargetDataLine line, float frequency)
    {
        return getInstance(new AudioInputStream(line), frequency);
    }

    public static AudioReader getInstance(AudioInputStream ais, float frequency)
    {
        return getInstance(ais, ais.getFormat(), frequency);
    }

    /**
     * 
     * @param ais
     * @param format
     * @param size
     * @return 
     */
    public static AudioReader getInstance(AudioInputStream ais, AudioFormat format, float frequency)
    {
        if (format.getChannels() != 1)
        {
            format = new AudioFormat(format.getEncoding(), format.getSampleRate(), format.getSampleSizeInBits(), 1, format.getFrameSize()/format.getChannels(), format.getFrameRate(), format.isBigEndian());
            ais = AudioSystem.getAudioInputStream(format, ais);
        }
        AudioReader audioReader;
        int size = calcSize(format.getSampleRate(), frequency);
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
    public void addDataListener(DataListener dataListener)
    {
        dataListeners.add(dataListener);
    }
    public void removeDataListener(DataListener dataListener)
    {
        dataListeners.remove(dataListener);
    }

    public int getSize()
    {
        return size;
    }
    
    private static int calcSize(float sampleRate, float frequency)
    {
        int size = (int) (WAVES_PER_BUFFER*sampleRate/frequency);
        return Integer.highestOneBit(size);
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
    /**
     * Returns amplitude relative to maximum amplitude.
     * @return 0.0 - 1.0.
     */
    public float getAmplitude()
    {
        return frequencyCounter.getAmplitude()/maxAmplitude;
    }

    public float getSampleRate()
    {
        return format.getSampleRate();
    }
    /**
     * Returns frame size in bytes.
     * @return 
     */
    public int getFrameSize()
    {
        return format.getSampleSizeInBits()/8;
    }
    public float getWaveSizeInSamples(float frequency)
    {
        return format.getSampleRate()/frequency;
    }
    public abstract int getSample() throws IOException;
    
    public static class IntAudioReader extends AudioReader<IntBuffer>
    {
        
        public IntAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected IntBuffer initBuffers()
        {
            buffer = byteBuffer.asIntBuffer();
            buffer.compact();
            intArray = IntArray.getInstance(buffer);
            return buffer;
        }

        @Override
        public int getSample() throws IOException
        {
            if (!buffer.hasRemaining())
            {
                if (!fill())
                {
                    throw new EOFException();
                }
                buffer.limit(byteBuffer.limit()/4).position(0);
                fireDataListeners();
            }
            return buffer.get();
        }

    }
    public static class ShortAudioReader extends AudioReader<ShortBuffer>
    {
        
        public ShortAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected ShortBuffer initBuffers()
        {
            buffer = byteBuffer.asShortBuffer();
            buffer.compact();
            intArray = IntArray.getInstance(buffer);
            return buffer;
        }

        @Override
        public int getSample() throws IOException
        {
            if (!buffer.hasRemaining())
            {
                if (!fill())
                {
                    throw new EOFException();
                }
                buffer.limit(byteBuffer.limit()/2).position(0);
                fireDataListeners();
            }
            return buffer.get();
        }
        
    }
    public static class ByteAudioReader extends AudioReader<ByteBuffer>
    {
        
        private ByteAudioReader(AudioInputStream ais, AudioFormat format, int size)
        {
            super(ais, format, size);
        }

        @Override
        protected ByteBuffer initBuffers()
        {
            byteBuffer.compact();
            intArray = IntArray.getInstance(byteBuffer);
            return byteBuffer;
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
                fireDataListeners();
            }
            return byteBuffer.get();
        }
    }
}
