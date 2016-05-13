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
package org.vesalainen.time;

import java.time.Clock;

/**
 * This class supplies relative milliseconds from near past. It can be used when 
 * only time differences count. All instances have the same reference point which is
 * the time of first call to millis.
 * @author tkv
 */
public class RelativeMillis
{
    private static long offset;
    private Clock clock;

    public RelativeMillis()
    {
        this(Clock.systemUTC());
    }
    
    public RelativeMillis(Clock clock)
    {
        this.clock = clock;
    }
    
    public long millis()
    {
        if (offset == 0)
        {
            offset = clock.millis();
        }
        return clock.millis() - offset;
    }
}
