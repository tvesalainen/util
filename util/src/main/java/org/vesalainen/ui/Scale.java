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
package org.vesalainen.ui;

import java.util.Iterator;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Scale
{
    /**
     * Returns ScaleLevel having step lesser but closest to delta.
     * @param min
     * @param max
     * @return 
     */
    default ScaleLevel closest(double min, double max)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        return closest(max-min);
    }
    /**
     * Returns ScaleLevel having step lesser but closest to delta.
     * @param delta
     * @return 
     */
    ScaleLevel closest(double delta);
    /**
     * Returns true if delta is in valid range for this scale
     * @param min
     * @param max
     * @return 
     */
    default boolean inRange(double min, double max)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        return inRange(max-min);
    }
    /**
     * Returns true if delta is in valid range for this scale
     * @param delta
     * @return 
     */
    boolean inRange(double delta);
    public static Iterator<ScaleLevel> merge(double min, double max, Scale... scales)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        return merge(max-min, scales);
    }
    /**
     * Returns merged closest ScaleLevel iterators
     * @param delta
     * @param scales
     * @return 
     */
    public static Iterator<ScaleLevel> merge(double delta, Scale... scales)
    {
        if (delta <= 0)
        {
            throw new IllegalArgumentException(delta+" is illegal delta");
        }
        int length = scales.length;
        Iterator<ScaleLevel> iterator = null;
        for (int ii=0;ii<length;ii++)
        {
            if (scales[ii].inRange(delta))
            {
                ScaleLevel closest = scales[ii].closest(delta);
                if (iterator == null)
                {
                    iterator = closest.iterator();
                }
                else
                {
                    iterator = Merger.merge(closest.iterator(), iterator);
                }
            }
        }
        return iterator;
    }
}
