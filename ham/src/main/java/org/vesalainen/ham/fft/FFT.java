/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.fft;

import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.jtransforms.fft.FloatFFT_1D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FFT
{
    private int samples;
    private float[] complex;
    private final FloatFFT_1D fft;

    public FFT(int samples)
    {
        fft = new FloatFFT_1D(samples);
        this.samples = samples;
        complex = new float[samples*2];
    }
    
    public float frequency(byte[] sample, int sampleRate)
    {
        return frequency(sample, sampleRate, 1, false);
    }
    public float frequency(byte[] sample, int sampleRate, int frameSize, boolean bigEndian)
    {
        return frequency(sample, sampleRate, frameSize, bigEndian, 0F, Float.MAX_VALUE);
    }
    public float frequency(byte[] sample, int sampleRate, int frameSize, boolean bigEndian, float min, float max)
    {
        int len = sample.length/frameSize;
        if (len != samples)
        {
            throw new IllegalArgumentException("wrong number of samples");
        }
        populate(sample, sampleRate, frameSize, bigEndian);
        fft.complexForward(complex);
        float maxStrength = -1;
        float hz = 0;
        for (int ii=0;ii<len;ii++)
        {
            float strength = (float) Math.hypot(complex[2*ii], complex[2*ii+1]);
            float freq = ((float)ii/(float)len)*(float)sampleRate;
            if (strength > maxStrength && freq >= min && freq <= max)
            {
                maxStrength = strength;
                hz = freq;
            }
        }
        return hz;
    }

    private void populate(byte[] sample, int sampleRate, int frameSize, boolean bigEndian)
    {
        int len = sample.length/frameSize;
        switch (frameSize)
        {
            case 1:
                for (int ii=0;ii<len;ii++)
                {
                    complex[2*ii] = sample[ii];
                    complex[2*ii+1] = 0;
                }
                break;
            case 2:
                ByteBuffer bb2 = ByteBuffer.wrap(sample);
                bb2.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
                ShortBuffer sb = bb2.asShortBuffer();
                for (int ii=0;ii<len;ii++)
                {
                    complex[2*ii] = sb.get(ii);
                    complex[2*ii+1] = 0;
                }
                break;
            case 4:
                ByteBuffer bb4 = ByteBuffer.wrap(sample);
                bb4.order(bigEndian ? BIG_ENDIAN : LITTLE_ENDIAN);
                IntBuffer ib = bb4.asIntBuffer();
                for (int ii=0;ii<len;ii++)
                {
                    complex[2*ii] = ib.get(ii);
                    complex[2*ii+1] = 0;
                }
                break;
            default:
                throw new IllegalArgumentException("illegal bytesPerSample "+frameSize);
        }
    }
}
