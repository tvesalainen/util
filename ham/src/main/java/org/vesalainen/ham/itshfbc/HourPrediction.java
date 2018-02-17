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

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.regex.PrefixMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HourPrediction
{
    private int hour;
    private Double[] frequences;
    private Double muf;
    private Map<String, Object[]> attributes = new HashMap<>();
    private PrefixMap<Integer> frequencyIndexMap;

    public HourPrediction(Object[] frequences)
    {
        this.hour = ((Double)frequences[0]).intValue();
        this.muf = ((Double)frequences[1]);
        this.frequences = filter(frequences);
        Map<String,Integer> m = new HashMap<>();
        for (int ii=0;ii<frequences.length;ii++)
        {
            m.put(frequences[ii].toString(), ii);
        }
        frequencyIndexMap = new PrefixMap<>(m);
    }
    public void addAttribute(Object[] array)
    {
        attributes.put((String) array[array.length-1], array);
    }

    public int getHour()
    {
        return hour;
    }

    public Double[] getFrequences()
    {
        return frequences;
    }

    public double getValue(String attribute, double frequency)
    {
        Integer index = frequencyIndexMap.get(Double.toString(frequency));
        if (index != null)
        {
            return getValue(attribute, index);
        }
        throw new UnsupportedOperationException(frequency+" not supported");
    }
    public double getValue(String attribute, int index)
    {
        Object[] attr = attributes.get(attribute);
        if (attr == null)
        {
            throw new UnsupportedOperationException(attribute+" not supported");
        }
        return (double) attr[index];
    }
    private Double[] filter(Object[] frequences)
    {
        int size = 0;
        for (int ii=2;ii<frequences.length;ii++)
        {
            if ((Double)frequences[ii] > 0.0)
            {
                size++;
            }
        }
        Double[] arr = new Double[size];
        int index = 0;
        for (int ii=2;ii<frequences.length;ii++)
        {
            if ((Double)frequences[ii] > 0.0)
            {
                arr[index++] = (Double) frequences[ii];
            }
        }
        return arr;
    }
    
}
