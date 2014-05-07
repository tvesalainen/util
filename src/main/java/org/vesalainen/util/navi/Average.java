/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
