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

import org.vesalainen.util.navi.Degree;
import org.vesalainen.util.navi.Angle;

/**
 *
 * @author tkv
 */
public class AverageAngle
{
    private Average sin = new Average();
    private Average cos = new Average();
    private Average angleRange = new Average();
    private double deviationSquare;
    private int count;

    public AverageAngle()
    {
    }

    public AverageAngle(AverageAngle a)
    {
        sin = new Average(a.sin);
        cos = new Average(a.cos);
    }

    public AverageAngle(AverageAngle a1, AverageAngle a2)
    {
        sin = new Average(a1.sin, a2.sin);
        cos = new Average(a1.cos, a2.cos);
        angleRange = new Average(a1.angleRange, a2.angleRange);
        count = a1.count + a2.count;
        deviationSquare = (a1.count*a1.deviationSquare+a2.count*a2.deviationSquare)/count;
    }

    public void add(Angle... angles)
    {
        for (Angle angle : angles)
        {
            add(angle);
        }
    }
    public void add(Angle angle)
    {
        sin.add(angle.sin());
        cos.add(angle.cos());
        angleRange.add(angle.getRadians());
        deviationSquare = count*deviationSquare/(count+1) + Math.pow(Angle.angleDiff(angle.getRadians(), getAverage().getRadians()), 2)/(count+1);
        count++;
    }

    public Angle getAverage()
    {
        return new Angle(cos.getAverage(), sin.getAverage());
    }

    public Angle getRange()
    {
        return new Angle(angleRange.range());
    }

    public Angle getMax()
    {
        return new Angle(angleRange.getMax());
    }

    public Angle getMin()
    {
        return new Angle(angleRange.getMin());
    }

    public double getDeviation()
    {
        return Math.sqrt(deviationSquare);
    }

    public void reset()
    {
        sin.reset();
        cos.reset();
        count = 0;
        deviationSquare = 0;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return getAverage().toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            AverageAngle aa = new AverageAngle();
            Angle a1 = new Degree(1);
            Angle a2 = new Degree(2);
            Angle a3 = new Degree(3);
            for (int ii=0;ii<100;ii++)
            {
                aa.add(a1, a2, a3);
                System.err.println(aa.getDeviation()+" "+aa.getAverage());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
