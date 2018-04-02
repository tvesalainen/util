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
package org.vesalainen.ham.filter;

import static java.lang.Math.*;
import org.vesalainen.ham.DataListener;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FIRFilter implements DataListener
{
    private double[] coef;
    private double[] shift;
    private int m;
    private int index;
    
    public FIRFilter(double[] coef)
    {
        this.m = coef.length;
        this.coef = coef;
        double sum = 0;
        for (int ii=0;ii<m;ii++)
        {
            sum += coef[ii];
        }
        for (int ii=0;ii<m;ii++)
        {
            coef[ii] /= sum;
        }
        shift = new double[m];
    }

    @Override
    public void update(IntArray array)
    {
        int al = array.length();
        for (int ii=0;ii<al;ii++)
        {
            double sum = 0;
            shift[index] = array.get(ii);
            for (int jj=0;jj<m;jj++)
            {
                int j = (index+jj)%m;
                sum += coef[jj]*shift[j];
            }
            array.put(ii, (int) sum);
            index++;
            index %= m;
        }
    }

    public static double[] calcCoefficients(int firLen, double sampleRate, double bandwidth)
    {
// Calculate FIR filter coefficients
// using the windowed-sinc method
        double[] c = new double[firLen]; // Coefficient array
        int i; // Coefficient index
        double ph; // Phase in radians
        double coef; // Filter coefficient
        int coef_int; // Digitized coefficient
        double bw_ratio; // Normalized bandwidth
        bw_ratio = 2 * bandwidth / sampleRate;
        for (i = 0; i < (firLen / 2); i++)
        {
// Brick-wall filter:
            ph = PI * (i + 0.5) * bw_ratio;
            coef = sin(ph) / ph;
// Hann window:
            ph = PI * (i + 0.5) / (firLen / 2);
            coef *= (1 + cos(ph)) / 2;
// Symmetrical impulse response:
            c[i + firLen / 2] = coef;
            c[firLen / 2 - 1 - i] = coef;
        }
        return c;
    }
}
