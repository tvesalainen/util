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
package org.vesalainen.ui.scale;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.vesalainen.math.MathFunction;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Scale
{
    /**
     * Returns iterator starting with step lesser but closest to delta.
     * @param min
     * @param max
     * @return 
     */
    Iterator<ScaleLevel> iterator(double min, double max);
    /**
     * Returns scaling function
     * @return 
     */
    default MathFunction function()
    {
        return MathFunction.identity();
    }
    /**
     * Returns merged closest ScaleLevel iterators
     * @param min
     * @param max
     * @param scales
     * @return 
     */
    public static Iterator<ScaleLevel> merge(double min, double max, Collection<Scale> scales)
    {
        return merge(min, max, scales.toArray(new Scale[scales.size()]));
    }
    /**
     * Returns merged closest ScaleLevel iterators
     * @param delta
     * @param scales
     * @return 
     */
    public static Iterator<ScaleLevel> merge(double min, double max, Scale... scales)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        int length = scales.length;
        Iterator<ScaleLevel> iterator = null;
        for (int ii=0;ii<length;ii++)
        {
            Iterator<ScaleLevel> di = scales[ii].iterator(min, max);
            if (di.hasNext())
            {
                if (iterator == null)
                {
                    iterator = di;
                }
                else
                {
                    iterator = Merger.merge(di, iterator);
                }
            }
        }
        if (iterator != null)
        {
            return iterator;
        }
        else
        {
            return Collections.emptyIterator();
        }
    }
}
