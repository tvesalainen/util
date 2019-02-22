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
import java.util.Iterator;
import java.util.List;
import org.vesalainen.util.Merger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Scale
{
    /**
     * Returns iterator starting with step lesser but closest to delta.
     * @return 
     */
    default Iterator<ScaleLevel> iterator(double min, double max)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        return iterator(max-min);
    }
    /**
     * Returns iterator starting with step lesser but closest to delta.
     * @return 
     */
    Iterator<ScaleLevel> iterator(double delta);
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
    public static Iterator<ScaleLevel> merge(double delta, Collection<Scale> scales)
    {
        return merge(delta, scales.toArray(new Scale[scales.size()]));
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
            if (iterator == null)
            {
                iterator = scales[ii].iterator(delta);
            }
            else
            {
                iterator = Merger.merge(scales[ii].iterator(delta), iterator);
            }
        }
        return iterator;
    }
}
