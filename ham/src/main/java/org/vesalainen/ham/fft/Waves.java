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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import org.vesalainen.nio.IntArray;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Waves
{
    public static final TimeDomain createSample(int sampleRate, long duration, TimeUnit unit, Frequency... frequencies)
    {
        int size = (int) (unit.toNanos(duration)*sampleRate/1000000000);
        IntArray samples = IntArray.getInstance(size);
        double[] x = new double[frequencies.length];
        double[] d = new double[frequencies.length];
        double[] m = new double[frequencies.length];
        for (int ii=0;ii<frequencies.length;ii++)
        {
            Frequency f = frequencies[ii];
            double waveLen = (double)sampleRate / f.getFrequency();
            d[ii] = (2* Math.PI) / waveLen;
            x[ii] = f.getPhase();
            m[ii] = f.getMagnitude();
        }
        for (int jj=0;jj<size;jj++)
        {
            double y = 0;
            for (int ii=0;ii<frequencies.length;ii++)
            {
                y += m[ii]*Math.sin(x[ii]);
                x[ii] += d[ii];
            }
            samples.put(jj, (int) y);
        }
        return createTimeDomain(sampleRate, samples);
    }
    public static final TimeDomain createTimeDomain(int sampleRate, IntArray samples)
    {
        return new TimeDomainImpl(sampleRate, samples);
    }
    public static final Frequency of(double frequency, double magnitude, double phase)
    {
        return new FrequencyImpl(frequency, magnitude, phase);
    }
    public static final void plot(TimeDomain samples, Path out) throws IOException
    {
        int sampleCount = samples.getSampleCount();
        Plotter plotter = new Plotter(sampleCount, sampleCount/2);
        samples.getSamples().forEach((i,s)->plotter.drawPoint(i, s));
        plotter.plot(out.toFile(), "png");
    }
    public static class TimeDomainImpl implements TimeDomain
    {
        private int sampleRate;
        private IntArray samples;

        public TimeDomainImpl(int sampleRate, IntArray samples)
        {
            this.sampleRate = sampleRate;
            this.samples = samples;
        }

        @Override
        public int getSampleRate()
        {
            return sampleRate;
        }

        @Override
        public IntArray getSamples()
        {
            return samples;
        }
        
    }
    public static class FrequencyImpl implements Frequency
    {
        private double frequency;
        private double magnitude;
        private double phase;

        public FrequencyImpl(double frequency, double magnitude, double phase)
        {
            this.frequency = frequency;
            this.magnitude = magnitude;
            this.phase = phase;
        }

        @Override
        public double getFrequency()
        {
            return frequency;
        }

        @Override
        public double getMagnitude()
        {
            return magnitude;
        }

        @Override
        public double getPhase()
        {
            return phase;
        }
        
    }
}
