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
import org.vesalainen.math.sliding.DoubleSlidingMax;
import org.vesalainen.math.sliding.DoubleSlidingMin;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyCounter
{
    private float sampleRate;
    private float prev;
    private float runLength;
    private float halfLength;
    private float frequency;
    private DoubleSlidingMin min = new DoubleSlidingMin(15);
    private DoubleSlidingMax max = new DoubleSlidingMax(15);
    private float zero;
    private long count;
    private float amplitude;
    private boolean up;
    private float mark;

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
    public boolean update(int now)
    {
        count++;
        boolean upd = false;
        if (up)
        {
            if (now > prev)
            {
                mark = runLength;
            }
            else
            {
                if (now < prev)
                {
                    upd = true;
                    float d = (runLength-mark)/2;
                    halfLength = runLength-d;
                    frequency = 0.5F/(halfLength/sampleRate);
                    runLength = d;
                    up = false;
                }
            }
        }
        else
        {
            if (now < prev)
            {
                mark = runLength;
            }
            else
            {
                if (now > prev)
                {
                    upd = true;
                    float d = (runLength-mark)/2;
                    halfLength = runLength-d;
                    frequency = 0.5F/(halfLength/sampleRate);
                    runLength = d;
                    up = true;
                }
            }
        }
        runLength++;
        prev = now;
        return upd;
    }
    public boolean update0(int sample)
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
            halfLength = runLength-an/s;
            frequency = 0.5F/(halfLength/sampleRate);
            runLength = 1+an/s;
            if (max.count() > 10)
            {
                double ma = max.getMax();
                double mi = min.getMin();
                amplitude = (float) (ma - mi);
                zero = (float) ((ma + mi)/2.0);
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
