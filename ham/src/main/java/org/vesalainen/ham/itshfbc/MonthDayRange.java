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

import java.time.MonthDay;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import static org.vesalainen.ham.itshfbc.OffsetTimeRange.parse;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MonthDayRange
{
    private MonthDay from;
    private MonthDay to;

    public MonthDayRange(String text)
    {
        String[] split = text.split("\\-");
        if (split.length != 2)
        {
            throw new IllegalArgumentException(text);
        }
        this.from = parse(split[0]);
        this.to = parse(split[1]);
    }

    public MonthDayRange(MonthDay from, MonthDay to)
    {
        this.from = from;
        this.to = to;
    }
    
    public boolean isInside(MonthDay day)
    {
        if (from.isBefore(to))
        {
            if (from.compareTo(day) <= 0)
            {
                return to.compareTo(day) >= 0;
            }
            return false;
        }
        else
        {
            return from.compareTo(day) <= 0 || to.compareTo(day) >= 0;
        }
    }
    public static final MonthDay parse(String text)
    {
        if (text.length() != 4)
        {
            throw new IllegalArgumentException(text);
        }
        return MonthDay.of(Primitives.parseInt(text, 0, 2), Primitives.parseInt(text, 2, 4));
    }

    @Override
    public String toString()
    {
        return String.format("%02d%02d-%02d%02d", from.getMonthValue(), from.getDayOfMonth(), to.getMonthValue(), to.getDayOfMonth());
    }
    
}
