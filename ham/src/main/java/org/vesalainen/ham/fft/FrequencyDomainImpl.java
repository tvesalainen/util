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

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyDomainImpl implements FrequencyDomain
{
    private double sampleFrequency;
    private double[] x;
    private double[] y;
    private double frequencyInterval;

    public FrequencyDomainImpl(double sampleFrequency, FFT fft)
    {
        this(sampleFrequency, fft.x, fft.y);
    }
    public FrequencyDomainImpl(double sampleFrequency, double[] x, double[] y)
    {
        this.sampleFrequency = sampleFrequency;
        this.x = x;
        this.y = y;
        this.frequencyInterval = sampleFrequency/x.length;
    }

    @Override
    public double getSampleFrequency()
    {
        return sampleFrequency;
    }

    @Override
    public int getSampleCount()
    {
        return x.length;
    }

    @Override
    public double getFrequencyInterval()
    {
        return frequencyInterval;
    }
    @Override
    public int getFrequencyIndex(double frequency)
    {
        if (frequency < 0 || frequency > sampleFrequency/2)
        {
            throw new IllegalArgumentException(frequency+" out of range");
        }
        return (int) Math.round(frequency/frequencyInterval);
    }

    @Override
    public double getMagnitudeMax()
    {
        return IntStream.rangeClosed(0, x.length/2+1)
                .mapToDouble(this::getMagnitude)
                .max().getAsDouble();
    }

    @Override
    public double getMagnitudeSum()
    {
        return IntStream.range(0, x.length)
                .mapToDouble(this::getMagnitude)
                .sum();
    }
    
    @Override
    public double getMagnitude(int index)
    {
        return Math.hypot(x[index], y[index]);
    }

    @Override
    public double getPhase(int index)
    {
        return Math.atan2(y[index], x[index]);
    }

    @Override
    public double getRe(int index)
    {
        return x[index];
    }

    @Override
    public double getIm(int index)
    {
        return y[index];
    }

    @Override
    public double[] getRe()
    {
        return x;
    }

    @Override
    public double[] getIm()
    {
        return y;
    }

    @Override
    public Stream<Frequency> stream(IntPredicate predicate)
    {
        return IntStream.rangeClosed(0, x.length/2+1)
                .filter(predicate)
                .mapToObj((i)->new Freq(i, this))
                ;
    }
    
    private class Freq implements Frequency
    {
        private int index;
        private FrequencyDomain outer;

        public Freq(int index, FrequencyDomain outer)
        {
            this.index = index;
            this.outer = outer;
        }

        @Override
        public double getFrequency()
        {
            return frequencyInterval*index;
        }

        @Override
        public double getMagnitude()
        {
            return outer.getMagnitude(index);
        }

        @Override
        public double getPhase()
        {
            return outer.getPhase(index);
        }
    }                
}
