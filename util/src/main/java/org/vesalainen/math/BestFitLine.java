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
public class BestFitLine
{
    private double sx;
    private double sy;
    private double sxy;
    private double sx2;
    private int n;
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
     * Adds a point
     * @param x
     * @param y 
     */
    public void add(double x, double y)
    {
        sx +=x;
        sy +=y;
        sxy += x*y;
        sx2 += x*x;
        n++;
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
    public double getY(double x)
    {
        double slope = getSlope();
        double yIntercept = getYIntercept(slope);
        return slope*x+yIntercept;
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
}
