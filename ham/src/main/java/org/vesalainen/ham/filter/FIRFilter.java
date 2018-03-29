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

import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FIRFilter implements Filter
{

    public FIRFilter(double sampleFrequency, double low, double high)
    {
    }

    @Override
    public void filter(IntArray array)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void calcCoefficients(int firLen, double sampleRate, double bandwidth)
    {
// Calculate FIR filter coefficients
// using the windowed-sinc method
        {
            int c[firLen
            ]; // Coefficient array
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
// Convert from floating point to int:
                coef *= 1 << (COEF_WIDTH - 1);
                coef_int = (int) coef;
// Symmetrical impulse response:
                c[i + firLen / 2] = coef_int;
                c[firLen / 2 - 1 - i] = coef_int;
            }
        }
    }
}
