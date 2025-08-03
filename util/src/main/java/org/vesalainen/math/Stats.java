/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 */
public final class Stats
{
    /**
     * Returns maximum of arguments
     * @param x
     * @return 
     */
    public static int max(int... x)
    {
        int r = Integer.MIN_VALUE;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            if (x[ii] > r)
            {
                r = x[ii];
            }
        }
        return r;
    }
    /**
     * Returns minimum of arguments
     * @param x
     * @return 
     */
    public static int min(int... x)
    {
        int r = Integer.MAX_VALUE;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            if (x[ii] < r)
            {
                r = x[ii];
            }
        }
        return r;
    }
    /**
     * Returns average of arguments
     * @param x
     * @return 
     */
    public static int avg(int... x)
    {
        return Math.toIntExact(MoreMath.longSum(x)/x.length);
    }
    /**
     * Returns minimum of arguments
     * @param x
     * @return 
     */
    public static long max(long... x)
    {
        long r = Long.MIN_VALUE;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            if (x[ii] > r)
            {
                r = x[ii];
            }
        }
        return r;
    }
    /**
     * Returns maximum of arguments
     * @param x
     * @return 
     */
    public static long min(long... x)
    {
        long r = Long.MAX_VALUE;
        int length = x.length;
        for (int ii=0;ii<length;ii++)
        {
            if (x[ii] < r)
            {
                r = x[ii];
            }
        }
        return r;
    }
    /**
     * Returns average of arguments
     * @param x
     * @return 
     */
    public static long avg(long... x)
    {
        return MoreMath.sum(x)/x.length;
    }
}
