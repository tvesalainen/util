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
package org.vesalainen.ham.fft;

import org.vesalainen.ham.filter.Filter;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FFTFilter extends FFT implements Filter
{
    private double sampleFrequency;
    private double low;
    private double high;

    public FFTFilter(double sampleFrequency, double low, double high, int n)
    {
        super(n);
        this.sampleFrequency = sampleFrequency;
        this.low = low;
        this.high = high;
    }
    
    @Override
    public void filter(IntArray array)
    {
        forward(array);
        window();
        reverse(array);
    }
    public void window()
    {
        double frequencyInterval = sampleFrequency/n;
        int lowIndex = (int) Math.round(low/frequencyInterval);
        int highIndex = (int) Math.round(high/frequencyInterval);
        x[0] = 0;
        y[0] = 0;
        for (int ii=1;ii<lowIndex;ii++)
        {
            x[ii] = 0;
            y[ii] = 0;
            x[n-ii] = 0;
            y[n-ii] = 0;
        }
        for (int ii=highIndex;ii<n-highIndex;ii++)
        {
            x[ii] = 0;
            y[ii] = 0;
        }
    }
}
