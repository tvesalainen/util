/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.sliding;

import java.util.stream.DoubleStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface DoubleValueArray
{
    /**
     * Returns last sample
     * @return 
     */
    double last();
    /**
     * Returns previous sample value
     * @return 
     */
    double previous();
    /**
     * Returns values as stream in the same order as entered
     * @return 
     */
    DoubleStream stream();
}
