/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.itshfbc;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetTimeRange
{
    private OffsetTime from;
    private OffsetTime to;

    public OffsetTimeRange(OffsetTime from, OffsetTime to)
    {
        this.from = from;
        this.to = to;
    }

    public OffsetTimeRange(int from, int to)
    {
        this(from/100, from%100, to/100, to%100);
    }

    public OffsetTimeRange(int hourFrom, int minuteFrom, int hourTo, int minuteTo)
    {
        this.from = OffsetTime.of(hourFrom, minuteFrom, 0, 0, ZoneOffset.UTC);
        this.to = OffsetTime.of(hourTo, minuteTo, 0, 0, ZoneOffset.UTC);
    }

    public OffsetTimeRange(String text)
    {
        String[] split = text.split("\\-");
        if (split.length != 2)
        {
            throw new IllegalArgumentException(text);
        }
        this.from = parse(split[0]);
        this.to = parse(split[1]);
    }

    public OffsetTime getFrom()
    {
        return from;
    }

    public OffsetTime getTo()
    {
        return to;
    }
    
    public boolean isInside(OffsetTime time)
    {
        if (from.isBefore(to))
        {
            if (from.compareTo(time) <= 0)
            {
                return to.compareTo(time) >= 0;
            }
            return false;
        }
        else
        {
            return from.compareTo(time) <= 0 || to.compareTo(time) >= 0;
        }
    }
    public static final OffsetTime parse(String text)
    {
        if (text.length() != 4)
        {
            throw new IllegalArgumentException(text);
        }
        return OffsetTime.of(Primitives.parseInt(text, 0, 2), Primitives.parseInt(text, 2, 4), 0, 0, ZoneOffset.UTC);
    }

    @Override
    public String toString()
    {
        return String.format("%02d%02d-%02d%02d", from.getHour(), from.getMinute(), to.getHour(), to.getMinute());
    }
    
}
