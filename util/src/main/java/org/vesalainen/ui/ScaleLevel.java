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
import java.util.Locale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface ScaleLevel extends Comparable<ScaleLevel>, Iterable<ScaleLevel>
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
     * Return next finer level
     * @return 
     */
    ScaleLevel next();
    /**
     * returns courser level
     * @return 
     */
    ScaleLevel prev();
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
     * Returns iterator starting with this and moving finer
     * @return 
     */
    @Override
    Iterator<ScaleLevel> iterator();
}
