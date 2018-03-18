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

import java.util.Arrays;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://paulbourke.net/miscellaneous/dft/">Fast Fourier
 * Transform</a>
 */
public class FFT
{
    private int n;
    private int m;
    private double[] x;
    private double[] y;

    public FFT(int n)
    {
        this.n = n;
        this.m = exponentOf(n);
        x = new double[n];
        y = new double[n];
    }
    public static final int exponentOf(int n)
    {
        if (Integer.bitCount(n) != 1)
        {
            throw new IllegalArgumentException(n+" is not power of 2");
        }
        int m = 0;
        int nn = n;
        while (nn != 1)
        {
            nn/=2;
            m++;
        }
        return m;
    }
    public double frequency(float sampleRate, IntArray sample)
    {
        fft(true, sample);
        int len = sample.length()/2;
        double f = 0;
        double d = sampleRate/sample.length();
        int max = Short.MIN_VALUE;
        for (int ii=0;ii<len;ii++)
        {
            int v = sample.get(ii);
            if (v > max)
            {
                max = v;
                f = d*ii;
            }
        }
        return f;
    }
    public void fft(boolean forward, IntArray sample)
    {
        fft(forward, sample, sample);
    }
    public void fft(boolean forward, IntArray in, IntArray out)
    {
        if (in.length() != x.length)
        {
            throw new IllegalArgumentException("illegal length");
        }
        for (int ii=0;ii<n;ii++)
        {
            x[ii] = in.get(ii);
        }
        Arrays.fill(y, 0);
        fft(forward, m, x, y);
        for (int ii=0;ii<n;ii++)
        {
            out.put(ii, (int) Math.hypot(x[ii], y[ii]));
        }
    }
    public static double average(float sampleRate, IntArray sample)
    {
        int len = sample.length()/2;
        double ave = 0;
        for (int ii=0;ii<len;ii++)
        {
            ave += sample.get(ii);
        }
        return ave/len;
    }
    public static double frequencyStrength(float sampleRate, float frequency, IntArray sample)
    {
        int len = sample.length()/2;
        double d = sampleRate/sample.length();
        int index = (int) (frequency/d);
        if (index < sample.length())
        {
            return sample.get(index);
        }
        else
        {
            throw new IllegalArgumentException("frequency too high");
        }
    }
    /*
   This computes an in-place complex-to-complex FFT 
   x and y are the real and imaginary arrays of 2^m points.
   dir =  1 gives forward transform
   dir = -1 gives reverse transform 
     */
    public static void fft(boolean forward, long m, double[] x , double[] y)
    {
        int n, i, i1, j, k, i2, l, l1, l2;
        double c1, c2, tx, ty, t1, t2, u1, u2, z;

        /* Calculate the number of points */
        n = 1;
        for (i = 0; i < m; i++)
        {
            n *= 2;
        }

        /* Do the bit reversal */
        i2 = n >> 1;
        j = 0;
        for (i = 0; i < n - 1; i++)
        {
            if (i < j)
            {
                tx = x[i];
                ty = y[i];
                x[i] = x[j];
                y[i] = y[j];
                x[j] = tx;
                y[j] = ty;
            }
            k = i2;
            while (k <= j)
            {
                j -= k;
                k >>= 1;
            }
            j += k;
        }

        /* Compute the FFT */
        c1 = -1.0;
        c2 = 0.0;
        l2 = 1;
        for (l = 0; l < m; l++)
        {
            l1 = l2;
            l2 <<= 1;
            u1 = 1.0;
            u2 = 0.0;
            for (j = 0; j < l1; j++)
            {
                for (i = j; i < n; i += l2)
                {
                    i1 = i + l1;
                    t1 = u1 * x[i1] - u2 * y[i1];
                    t2 = u1 * y[i1] + u2 * x[i1];
                    x[i1] = x[i] - t1;
                    y[i1] = y[i] - t2;
                    x[i] += t1;
                    y[i] += t2;
                }
                z = u1 * c1 - u2 * c2;
                u2 = u1 * c2 + u2 * c1;
                u1 = z;
            }
            c2 = Math.sqrt((1.0 - c1) / 2.0);
            if (forward)
            {
                c2 = -c2;
            }
            c1 = Math.sqrt((1.0 + c1) / 2.0);
        }

        /* Scaling for forward transform */
        if (forward)
        {
            for (i = 0; i < n; i++)
            {
                x[i] /= n;
                y[i] /= n;
            }
        }
    }
}
