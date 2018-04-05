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

import java.util.function.IntPredicate;
import java.util.stream.Stream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface FrequencyDomain
{
    default Stream<Frequency> stream(double minMagnitude)
    {
        return stream((int i)->getMagnitude(i)>=minMagnitude);
    }
    Stream<Frequency> stream(IntPredicate predicate);
    double getSampleFrequency();
    default double getNyqvistFrequency()
    {
        return getSampleFrequency()/2;
    }
    int getSampleCount();
    default double getFrequencyInterval()
    {
        return getSampleFrequency()/getSampleCount();
    }
    int getFrequencyIndex(double frequency);
    default double getMagnitude(double frequency)
    {
        return getMagnitude(getFrequencyIndex(frequency));
    }
    default double getPhase(double frequency)
    {
        return getPhase(getFrequencyIndex(frequency));
    }
    default double getRe(double frequency)
    {
        return getRe(getFrequencyIndex(frequency));
    }
    default double getIm(double frequency)
    {
        return getIm(getFrequencyIndex(frequency));
    }
    double getMagnitudeSum();
    double getMagnitudeMax();
    double getMagnitude(int index);
    double getPhase(int index);
    double getRe(int index);
    double getIm(int index);
    double[] getRe();
    double[] getIm();
}
