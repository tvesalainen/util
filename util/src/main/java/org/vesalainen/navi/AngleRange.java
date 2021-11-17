/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import static org.vesalainen.navi.Navis.*;

/**
 * CoveringSector calculates range of angles. It is mean to use with maximum range 
 * of 180. Larger ranges will grow unpredictable.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleRange
{
    private double right = Double.NaN;
    private double left = Double.NaN;
    /**
     * Add new angle
     * @param deg
     * @return 
     */
    public boolean add(double deg)
    {
        if (Double.isNaN(right))
        {
            right = deg;
            left = deg;
            return true;
        }
        if (clockwise(right, deg))
        {
            right = deg;
            return true;
        }
        if (!clockwise(left, deg))
        {
            left = deg;
            return true;
        }
        return false;
    }
    public void reset()
    {
        right = Double.NaN;
        left = Double.NaN;
    }
    /**
     * Returns left limit of range
     * @return 
     */
    public double getLeft()
    {
        return left;
    }
    /**
     * Returns right limit of range
     * @return 
     */
    public double getRight()
    {
        return right;
    }
    /**
     * Returns range in degrees.
     * @return 
     */
    public double getRange()
    {
        if (Double.isNaN(right))
        {
            return 0;
        }
        return angleDistance(left, right);
    }
}
