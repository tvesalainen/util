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

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.function.Predicate;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeFilter extends JavaLogging implements Predicate<Schedule>
{
    
    private OffsetDateTime utc;

    public TimeFilter(OffsetDateTime utc)
    {
        super(TimeFilter.class);
        this.utc = utc;
    }

    @Override
    public boolean test(Schedule schedule)
    {
        OffsetDateTime with = TimeUtils.next(utc, (OffsetTime)schedule.getFrom());
        boolean b = schedule.isInRange(with) && schedule.getStation().isInRange(with);
        if (!b)
        {
            finer("dropping %s because of time-range", schedule);
        }
        return b;
    }
    
}
