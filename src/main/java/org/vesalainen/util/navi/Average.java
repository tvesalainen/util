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

import java.util.Random;

/**
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
    
    public void add(double... value)
    {
        for (double v : value)
        {
            add(v);
        }
    }
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

    public double getAverage()
    {
        return average;
    }

    public double getDeviation()
    {
        return Math.sqrt(deviationSquare);
    }

    public double getMax()
    {
        return max;
    }

    public double getMin()
    {
        return min;
    }

    public double range()
    {
        return max-min;
    }

    public void reset()
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Average aa = new Average();
            double v = 0;
            double s = 0;
            for (int ii=0;ii<10;ii++)
            {
                v += 1;
                System.err.println(v);
                s+=v;
                aa.add(v);
            }
            System.err.println(s+" "+v+" "+aa.getAverage());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
