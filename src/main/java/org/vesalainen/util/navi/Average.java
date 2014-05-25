/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util.navi;

/**
 * Average class is a simple utility to count some statistic values.
 * 
 * @author tkv
 */
public class Average
{
    private double average;
    private double deviationSquare;
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;
    private int count;

    public Average()
    {
    }

    public Average(Average a)
    {
        average = a.average;
        count = a.count;
        deviationSquare = a.deviationSquare;
        max = a.max;
        min = a.min;
    }

    public Average(Average a1, Average a2)
    {
        count = a1.count + a2.count;
        average = (a1.count*a1.average+a2.count*a2.average)/count;
        deviationSquare = (a1.count*a1.deviationSquare+a2.count*a2.deviationSquare)/count;
        max = Math.max(a1.max, a2.max);
        min = Math.min(a1.min, a2.min);
    }
    /**
     * Add a new values.
     * @param value 
     */
    public void add(double... value)
    {
        for (double v : value)
        {
            add(v);
        }
    }
    /**
     * Add a new value.
     * @param value 
     */
    public void add(double value)
    {
        max = Math.max(max, value);
        min = Math.min(min, value);
        if (count == 0)
        {
            average = value;
        }
        else
        {
            average = count*average/(count+1) + value/(count+1);
            deviationSquare = count*deviationSquare/(count+1) + Math.pow(value-average, 2)/(count+1);
        }
        count++;
    }
    /**
     * Returns average of added values.
     * @return 
     */
    public double getAverage()
    {
        return average;
    }
    /**
     * Returns standard deviation of values.
     * @return 
     */
    public double getDeviation()
    {
        return Math.sqrt(deviationSquare);
    }
    /**
     * Returns the greatest of the values.
     * @return 
     */
    public double getMax()
    {
        return max;
    }
    /**
     * Returns the smallest value.
     * @return 
     */
    public double getMin()
    {
        return min;
    }
    /**
     * Returns getMax() - getMin()
     * @return 
     */
    public double getRange()
    {
        return max-min;
    }
    /**
     * Clears the added values.
     */
    public void clear()
    {
        count = 0;
        average = 0;
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        deviationSquare = 0;
    }

    @Override
    public String toString()
    {
        return Double.toString(average);
    }

}
