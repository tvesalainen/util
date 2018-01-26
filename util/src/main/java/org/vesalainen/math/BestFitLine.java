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
package org.vesalainen.math;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="http://math.tutorvista.com/algebra/line-of-best-fit.html">Line of Best Fit</a>
 */
public class BestFitLine implements XYModel
{
    private double sx;
    private double sy;
    private double sxy;
    private double sx2;
    private int n;

    public BestFitLine()
    {
    }
    
    /**
     * Creates a BestFitLine using x,y pairs from samples.
     * @param samples 
     */
    public BestFitLine(XYSamples samples)
    {
        add(samples);
    }
    
    /**
     * Removes all points
     */
    public void reset()
    {
        sx = 0;
        sy = 0;
        sxy = 0;
        sx2 = 0;
        n = 0;
    }
    /**
     * Add all x,y pairs from samples
     * @param samples 
     */
    public final void add(XYSamples samples)
    {
        samples.forEach(this::add);
    }
    /**
     * Adds a point
     * @param x
     * @param y 
     */
    public void add(double x, double y)
    {
        add(x, y, 1);
    }
    /**
     * Adds a point k times. k can be negative.
     * @param x
     * @param y
     * @param k 
     */
    public void add(double x, double y, int k)
    {
        sx +=x*k;
        sy +=y*k;
        sxy += x*y*k;
        sx2 += x*x*k;
        n+=k;
        if (n < 1)
        {
            throw new IllegalArgumentException("negative count");
        }
    }
    /**
     * Returns y-intercept
     * @param slope
     * @return 
     */
    public double getYIntercept(double slope)
    {
        return sy/n-slope*sx/n;
    }
    /**
     * Returns slope
     * @return 
     */
    public double getSlope()
    {
        return (sxy - sx*sy/n)/(sx2-sx*sx/n);
    }
    /**
     * Returns y-value for x
     * @param x
     * @return 
     */
    @Override
    public double getY(double x)
    {
        double slope = getSlope();
        double a = getYIntercept(slope);
        if (!Double.isInfinite(slope))
        {
            return slope*x + a;
        }
        else
        {
            if (x == a)
            {
                return Double.POSITIVE_INFINITY;
            }
            else
            {
                return Double.NaN;
            }
        }
    }
    /**
     * Return number of points
     * @return 
     */
    public int getCount()
    {
        return n;
    }
    
    /**
     * Returns best-fit-line
     * @return 
     */
    public AbstractLine getLine()
    {
        double slope = getSlope();
        double yIntercept = getYIntercept(slope);
        return new AbstractLine(slope, 0, yIntercept);
    }

    @Override
    public String toString()
    {
        double slope = getSlope();
        double yIntercept = getYIntercept(slope);
        return "BestFitLine{slope="+slope+", a="+yIntercept+")";
    }
}
