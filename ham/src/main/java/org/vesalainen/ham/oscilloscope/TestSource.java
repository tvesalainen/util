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
package org.vesalainen.ham.oscilloscope;

import java.util.concurrent.TimeUnit;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.fft.TimeDomain;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TestSource extends AbstractSource
{
    private SampleBuffer samples;

    public TestSource(double sampleFrequency, int bitCount)
    {
        TimeDomain td = Waves.createSample(sampleFrequency, 0, 1, TimeUnit.SECONDS, Waves.of(sampleFrequency/10, Waves.maxAmplitude(bitCount)*0.9, 0));
        this.samples = new SampleBufferImpl((long) sampleFrequency, Waves.maxAmplitude(bitCount), td.getSamples());
    }
    
    @Override
    public void start()
    {
        fireUpdate(samples);
        fireUpdate();
    }

    @Override
    public String toString()
    {
        return "TestSource{" + "samples=" + samples + '}';
    }

}
