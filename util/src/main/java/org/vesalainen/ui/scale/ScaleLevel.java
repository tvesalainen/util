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

import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface ScaleLevel extends Comparable<ScaleLevel>
{
    /**
     * Returns step
     * @return 
     */
    double step();
    /**
     * Returns formatted string for value using default locale
     * @param value
     * @return 
     */
    default String label(double value)
    {
        return label(Locale.getDefault(), value);
    }
    /**
     * Returns formatted string for value
     * @param locale
     * @param value
     * @return 
     */
    String label(Locale locale, double value);
    /**
     * Compares step's in descending order
     * @param o
     * @return 
     */
    @Override
    default int compareTo(ScaleLevel o)
    {
        return Double.compare(o.step(), step());
    }
    /**
     * Returns number of markers for given level.
     * @param min
     * @param max
     * @return
     */
    default int count(double min, double max)
    {
        double step = step();
        double start = Math.floor(min/step)*step;
        if (start != min)
        {
            start += step;
        }
        return (int) ((max-start)/step)+1;
    }
    /**
     * Calls op for each value and label.
     * @param min
     * @param max
     * @param locale
     * @param op 
     */
    default void forEach(double min, double max, Locale locale, ScalerOperator op)
    {
        PrimitiveIterator.OfDouble iterator = iterator(min, max);
        while (iterator.hasNext())
        {
            double value = iterator.nextDouble();
            op.apply(value, label(locale, value));
        }
    }
    /**
     * Returns iterator for markers between min and max.
     * @param min
     * @param max
     * @return 
     */
    PrimitiveIterator.OfDouble iterator(double min, double max);
    /**
     * Returns stream for markers between min and max.
     * @param min
     * @param max
     * @return 
     */
    default DoubleStream stream(double min, double max)
    {
        return StreamSupport.doubleStream(spliterator(min, max), false);
    }
    

    /**
     * Returns Spliterator for markers between min and max.
     * @param min
     * @param max
     * @return
     */
    default Spliterator.OfDouble spliterator(double min, double max)
    {
        return Spliterators.spliteratorUnknownSize(iterator(min, max), 0);
    }
}
