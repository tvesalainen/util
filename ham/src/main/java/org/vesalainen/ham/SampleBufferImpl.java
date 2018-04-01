
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

import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.time.Duration;
import javax.sound.sampled.AudioFormat;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SampleBufferImpl implements SampleBuffer
{
    private int channels;
    private long sampleFrequency;
    private int maxAmplitude;
    private IntArray array;
    private int viewLength;
    private int offset;

    public SampleBufferImpl(long sampleFrequency, int maxAmplitude, IntArray array)
    {
        this(1, sampleFrequency, maxAmplitude, array, array.length());
    }
    public SampleBufferImpl(AudioFormat audioFormat, ByteBuffer buffer, int viewLength)
    {
        this(
                audioFormat.getChannels(), 
                (long) audioFormat.getFrameRate(), 
                Waves.maxAmplitude(audioFormat.getSampleSizeInBits()), 
                IntArray.getInstance(buffer, audioFormat.getSampleSizeInBits(), audioFormat.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN), 
                viewLength
        );
    }
    public SampleBufferImpl(int channels, long sampleFrequency, int maxAmplitude, IntArray array, int viewLength)
    {
        this.channels = channels;
        this.sampleFrequency = sampleFrequency;
        this.maxAmplitude = maxAmplitude;
        this.array = array;
        this.viewLength = viewLength;
    }

    public IntArray getArray()
    {
        return array;
    }

    @Override
    public int getChannels()
    {
        return channels;
    }

    @Override
    public void goTo(Duration duration)
    {
        offset = (int) (channels*duration.toNanos()*sampleFrequency/1000000000L);
    }

    @Override
    public Duration getDuration()
    {
        long nanos = (long) (1000000000L*array.length()/sampleFrequency/channels);
        return Duration.ofNanos(nanos);
    }

    @Override
    public int get(int index, int channel)
    {
        if (channel < 0 || channel > channels)
        {
            throw new IllegalArgumentException("illegal channel");
        }
        return array.get(offset+channels*index+channel);
    }

    @Override
    public Duration remaining()
    {
        long nanos = (long) (1000000000L*(array.length()-offset)/sampleFrequency/channels);
        return Duration.ofNanos(nanos);
    }

    @Override
    public double getSampleFrequency()
    {
        return sampleFrequency;
    }

    @Override
    public int getMaxAmplitude()
    {
        return maxAmplitude;
    }

    @Override
    public int getViewLength()
    {
        return Math.min(viewLength, (array.length()-offset)/channels);
    }

    @Override
    public String toString()
    {
        return "SampleBufferImpl{" + "sampleFrequency=" + sampleFrequency + ", viewLength=" + viewLength + '}';
    }

}
