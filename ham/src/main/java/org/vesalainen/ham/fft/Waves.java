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
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.vesalainen.nio.IntArray;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Waves
{
    public static final void addWhiteNoise(IntArray samples, double amplitude)
    {
        Random random = new Random(12345678);
        int bound = (int) (2*amplitude);
        int len = samples.length();
        for (int ii=0;ii<len;ii++)
        {
            int nextInt = random.nextInt(bound);
            samples.put(ii, samples.get(ii)+nextInt);
        }
    }
    public static final void window(FrequencyDomain fd, double low, double high)
    {
        int n = fd.getSampleCount();
        int lowIndex = fd.getFrequencyIndex(low);
        int highIndex = fd.getFrequencyIndex(high);
        double x[] = fd.getRe();
        double y[] = fd.getIm();
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
    public static final TimeDomain ifft(FrequencyDomain fd)
    {
        int n = fd.getSampleCount();
        int m = FFT.exponentOf(n);
        IntArray samples = IntArray.getInstance(n);
        double x[] = fd.getRe();
        double y[] = fd.getIm();
        FFT.fft(false, m, x, y);
        for (int ii=0;ii<n;ii++)
        {
            samples.put(ii, (int) x[ii]);
        }
        return createTimeDomain(fd.getSampleFrequency(), samples);
    }
    public static final FrequencyDomain fft(TimeDomain timeDomain)
    {
        int n = timeDomain.getSampleCount();
        int m = FFT.exponentOf(n);
        double[] x = new double[n];
        double[] y = new double[n];
        IntArray samples = timeDomain.getSamples();
        samples.fill(x);
        FFT.fft(true, m, x, y);
        return new FrequencyDomainImpl(n, x, y);
    }
    public static final TimeDomain createFMSample(double sampleFrequency, double amplitude, double on, double off, TimeUnit unit, long... codec)
    {
        long duration = 0;
        for (long d : codec)
        {
            duration += d;
        }
        int size = (int) (unit.toNanos(duration)*sampleFrequency/1000000000);
        IntArray samples = IntArray.getInstance(size);
        double[] d = new double[2];
        double waveLenOn = sampleFrequency / on;
        d[0] = (2* Math.PI) / waveLenOn;
        double waveLenOff = sampleFrequency / off;
        d[1] = (2* Math.PI) / waveLenOff;
        int idx = 0;
        double x = 0;
        for (int kk=0;kk<codec.length;kk++)
        {
            int siz = (int) (unit.toNanos(codec[kk])*sampleFrequency/1000000000);
            for (int jj=0;jj<siz;jj++)
            {
                samples.put(idx++, (int) (amplitude*Math.cos(x)));
                x += d[kk%2];
            }
        }
        return createTimeDomain(sampleFrequency, samples);
    }
    public static final TimeDomain createSample(double sampleFrequency, double dc, long duration, TimeUnit unit, Frequency... frequencies)
    {
        int size = (int) (unit.toNanos(duration)*sampleFrequency/1000000000);
        IntArray samples = IntArray.getInstance(size);
        double[] x = new double[frequencies.length];
        double[] d = new double[frequencies.length];
        double[] m = new double[frequencies.length];
        for (int ii=0;ii<frequencies.length;ii++)
        {
            Frequency f = frequencies[ii];
            double waveLen = sampleFrequency / f.getFrequency();
            d[ii] = (2* Math.PI) / waveLen;
            x[ii] = f.getPhase();
            m[ii] = f.getMagnitude();
        }
        for (int jj=0;jj<size;jj++)
        {
            double y = dc;
            for (int ii=0;ii<frequencies.length;ii++)
            {
                y += m[ii]*Math.cos(x[ii]);
                x[ii] += d[ii];
            }
            samples.put(jj, (int) y);
        }
        return createTimeDomain(sampleFrequency, samples);
    }
    public static final TimeDomain createTimeDomain(double sampleFrequency, IntArray samples)
    {
        return new TimeDomainImpl(sampleFrequency, samples);
    }
    public static final Frequency of(double frequency, double magnitude, double phase)
    {
        return new FrequencyImpl(frequency, magnitude, phase);
    }
    public static final void plot(IntArray samples, Path out) throws IOException
    {
        int sampleCount = samples.length();
        Plotter plotter = new Plotter(sampleCount, sampleCount/2);
        samples.forEach((i,s)->plotter.drawPoint(i, s));
        plotter.plot(out.toFile(), "png");
    }
    public static final void plot(FrequencyDomain fd, Path out) throws IOException
    {
        int sampleCount = fd.getSampleCount();
        Plotter plotter = new Plotter(sampleCount, sampleCount/2);
        fd.stream(1e-8).forEach((f)->plotter.drawLine(f.getFrequency(), 0, f.getFrequency(), f.getMagnitude()));
        plotter.plot(out.toFile(), "png");
    }
    public static class TimeDomainImpl implements TimeDomain
    {
        private double sampleFrequency;
        private IntArray samples;

        public TimeDomainImpl(double sampleFrequency, IntArray samples)
        {
            this.sampleFrequency = sampleFrequency;
            this.samples = samples;
        }

        @Override
        public double getSampleFrequency()
        {
            return sampleFrequency;
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
