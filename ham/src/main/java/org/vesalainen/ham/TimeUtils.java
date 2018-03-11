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
package org.vesalainen.ham;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import static org.vesalainen.regex.Regex.Option.CASE_INSENSITIVE;
import org.vesalainen.regex.SynchronizedEnumPrefixFinder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeUtils
{
    public static final SynchronizedEnumPrefixFinder<DayOfWeek> WEEKDAY_PARSER = new SynchronizedEnumPrefixFinder(DayOfWeek.class, CASE_INSENSITIVE);
    public static final SynchronizedEnumPrefixFinder<Month> MONTH_PARSER = new SynchronizedEnumPrefixFinder(Month.class, CASE_INSENSITIVE);
    public static final OffsetDateTime next(OffsetDateTime date, OffsetTime time)
    {
        OffsetDateTime newDate = date.with(time);
        if (newDate.isBefore(date))
        {
            return newDate.plusDays(1);
        }
        return newDate;
    }
}
