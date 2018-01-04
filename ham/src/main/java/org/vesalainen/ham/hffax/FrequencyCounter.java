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
package org.vesalainen.ham.hffax;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.vesalainen.math.sliding.SlidingMax;
import org.vesalainen.math.sliding.SlidingMin;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyCounter
{
    private float sampleRate;
    private float prev;
    private float runLength;
    private float frequency;
    private SlidingMin min = new SlidingMin(15);
    private SlidingMax max = new SlidingMax(15);
    private float zero;
    private long count;
    private float amplitude;

    public FrequencyCounter(int sampleRate)
    {
        this.sampleRate = sampleRate;
    }
    
    public float count(IntBuffer ib)
    {
        while (ib.hasRemaining())
        {
            update(ib.get());
        }
        return frequency;
    }
    public float count(ShortBuffer sb)
    {
        while (sb.hasRemaining())
        {
            update(sb.get());
        }
        return frequency;
    }
    public float count(byte[] buffer)
    {
        return count(buffer, 0, buffer.length);
    }
    public float count(byte[] buffer, int offset, int length)
    {
        for (int ii=0;ii<length;ii++)
        {
            update(buffer[ii+offset]);
        }
        return frequency;
    }
    public boolean update(int sample)
    {
        count++;
        boolean upd = false;
        float now = sample-zero;
        boolean ns = now>=0;
        boolean vs = prev>=0;
        if (ns != vs)
        {
            upd = true;
            float an = Math.abs(now);
            float ap = Math.abs(prev);
            float s = an+ap;
            frequency = 0.5F/((runLength-an/s)/sampleRate);
            runLength = 1+an/s;
            if (max.count() > 10)
            {
                amplitude = (float) (max.getMax() + min.getMin());
                zero = (float) (amplitude/2.0);
            }
        }
        else
        {
            runLength++;
        }
        prev = now;
        if (ns)
        {
            max.accept(sample);
        }
        else
        {
            min.accept(sample);
        }
        return upd;
    }

    public float getFrequency()
    {
        return frequency;
    }

    public long getCount()
    {
        return count;
    }
    
    public long getMicros()
    {
        return 1000000L*count/(long)sampleRate;
    }

    public float getZero()
    {
        return zero;
    }

    public float getAmplitude()
    {
        return amplitude;
    }
    
}
