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
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://paulbourke.net/miscellaneous/dft/">Fast Fourier
 * Transform</a>
 */
public class FFT
{
    protected int n;
    protected int m;
    protected double[] x;
    protected double[] y;

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
    public void forward(SampleBuffer in, int channel)
    {
        if (in.getViewLength()< n)
        {
            throw new IllegalArgumentException("illegal length");
        }
        for (int ii=0;ii<n;ii++)
        {
            x[ii] = in.get(ii, channel);
        }
        Arrays.fill(y, 0);
        fft(true, m, x, y);
    }
    public void forward(IntArray in)
    {
        if (in.length() < n)
        {
            throw new IllegalArgumentException("illegal length");
        }
        for (int ii=0;ii<n;ii++)
        {
            x[ii] = in.get(ii);
        }
        Arrays.fill(y, 0);
        fft(true, m, x, y);
    }
    public void reverse(IntArray out)
    {
        if (out.length() < n)
        {
            throw new IllegalArgumentException("illegal length");
        }
        fft(false, m, x, y);
        for (int ii=0;ii<n;ii++)
        {
            out.put(ii, (int) x[ii]);
        }
    }

    public double getMagnitude(double sampleRate, double frequency)
    {
        double frequencyInterval = sampleRate/x.length;
        int index = (int) Math.round(frequency/frequencyInterval);
        return Math.hypot(x[index], y[index]);
    }
    public int getN()
    {
        return n;
    }

    public int getM()
    {
        return m;
    }

    public double[] getX()
    {
        return x;
    }

    public double[] getY()
    {
        return y;
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
