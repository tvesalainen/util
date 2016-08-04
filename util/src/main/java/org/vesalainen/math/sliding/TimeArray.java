/*
 * Copyright (C) 2016 tkv
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

import java.util.stream.LongStream;

/**
 *
 * @author tkv
 */
public interface TimeArray extends ValueArray
{
    /**
     * Returns a stream of sample times
     * @return 
     */
    LongStream timeStream();
    /**
     * Returns time of first sample
     * @return 
     */
    long firstTime();
    /**
     * Returns time of last sample
     * @return 
     */
    long lastTime();
    /**
     * Returns time of previous sample
     * @return 
     */
    long previousTime();
}