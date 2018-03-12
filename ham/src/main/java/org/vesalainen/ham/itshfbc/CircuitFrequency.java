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
package org.vesalainen.ham.itshfbc;

import org.vesalainen.ham.EmissionClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CircuitFrequency implements Comparable<CircuitFrequency>
{
    private Circuit circuit;
    private double frequency;
    private HourPrediction prediction;
    private EmissionClass emissionClass;

    public CircuitFrequency(Circuit circuit, double frequency, HourPrediction prediction, EmissionClass emissionClass)
    {
        this.circuit = circuit;
        this.frequency = frequency;
        this.prediction = prediction;
        this.emissionClass = emissionClass;
        if (prediction == null)
        {
            System.err.println();
        }
    }

    public double snr()
    {
        return prediction.getValue("SNR", frequency);
    }
    public double snr90()
    {
        return prediction.getValue("SNRxx", frequency);
    }
    @Override
    public int compareTo(CircuitFrequency o)
    {
        int cmp = Double.compare(snr90(), o.snr90());
        if (cmp == 0)
        {
            return -Double.compare(snr(), o.snr());
        }
        else
        {
            return -cmp;
        }
    }

    public double getFrequency()
    {
        return frequency;
    }

    public HourPrediction getPrediction()
    {
        return prediction;
    }

    public EmissionClass getEmissionClass()
    {
        return emissionClass;
    }

    @Override
    public String toString()
    {
        return "CircuitFrequency{" + "frequency=" + frequency + " SNR90=" +snr90()+" SNR=" +snr()+'}';
    }
    
}
