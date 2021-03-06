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

import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FFTFrequencyDetector
{
    protected FFT fft;
    protected int sampleRate;
    protected IntArray array;
    protected int index;
    protected double frequency;
    protected FrequencyDomain fd;

    public FFTFrequencyDetector(int n, int sampleRate)
    {
        this.fft = new FFT(n);
        this.sampleRate = sampleRate;
        this.array = IntArray.getInstance(n);
        this.fd = new FrequencyDomainImpl(sampleRate, fft);
    }
    
    public void update(int sample)
    {
        array.put(index++, sample);
        if (index == array.length())
        {
            index = 0;
            calc();
        }
    }

    public double getFrequency()
    {
        return frequency;
    }

    private void calc()
    {
        fft.forward(array);
        frequency = fd.stream(0.01, FrequencyDomain.comparatorByMagnitude().reversed())
                .filter((f)->f.getFrequency() > 0)
                .findFirst().get().getFrequency();
    }
}
