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
 *
 * @author tkv
 */
public class AverageMotion
{
    protected Average x = new Average();
    protected Average y = new Average();
    protected Average speed = new Average();
    protected AverageAngle angle = new AverageAngle();
    private int count;

    public AverageMotion()
    {
    }

    public AverageMotion(AverageMotion a)
    {
        x = new Average(a.x);
        y = new Average(a.y);
    }

    public AverageMotion(AverageMotion a1, AverageMotion a2)
    {
        x = new Average(a1.x, a2.x);
        y = new Average(a1.y, a2.y);
        speed = new Average(a1.speed, a2.speed);
        angle = new AverageAngle(a1.angle, a2.angle);
        count = a1.count + a2.count;
    }

    public void add(Motion... motion)
    {
        for (Motion m : motion)
        {
            add(m);
        }
    }
    public void add(Motion motion)
    {
        x.add(motion.getX());
        y.add(motion.getY());
        speed.add(motion.getMetersInSecond());
        angle.add(motion.getAngle());
        count++;
    }

    public Motion getAverage()
    {
        return new Motion(x.getAverage(), y.getAverage());
    }

    public void reset()
    {
        x.reset();
        y.reset();
        speed.reset();
        angle.reset();
        count = 0;
    }

    public int getCount()
    {
        return count;
    }

    public double getSpeedDeviation()
    {
        return speed.getDeviation();
    }

    public double getAngleDeviation()
    {
        return angle.getDeviation();
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
            AverageMotion aa = new AverageMotion();
            aa.add(new Motion(new Knots(6), new Degree(350)));
            aa.add(new Motion(new Knots(6), new Degree(170)));
            aa.add(new Motion(new Knots(6), new Degree(270)));
            System.err.println(aa.getAverage());
            System.err.println(aa.getSpeedDeviation());
            System.err.println(aa.getAngleDeviation());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
