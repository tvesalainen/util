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

import java.time.Duration;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import static org.vesalainen.ham.BroadcastStationsFile.DATA_TYPE_FACTORY;
import static org.vesalainen.ham.BroadcastStationsFile.OBJECT_FACTORY;
import org.vesalainen.ham.jaxb.TimeRangeType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetTimeRange implements TimeRange
{
    private OffsetTime from;
    private OffsetTime to;

    public OffsetTimeRange(TimeRangeType range)
    {
        this(range.getFrom().getHour(), range.getFrom().getMinute(), range.getTo().getHour(), range.getTo().getMinute());
    }

    public OffsetTimeRange(int from, int to)
    {
        this(from/100, from%100, to/100, to%100);
    }

    public OffsetTimeRange(int hourFrom, int minuteFrom, int hourTo, int minuteTo)
    {
        this(OffsetTime.of(hourFrom, minuteFrom, 0, 0, ZoneOffset.UTC), OffsetTime.of(hourTo, minuteTo, 0, 0, ZoneOffset.UTC));
    }

    public OffsetTimeRange(OffsetTime from, Duration duration)
    {
        this(from, from.plus(duration));
    }

    public OffsetTimeRange(OffsetTime from, OffsetTime to)
    {
        this.from = from;
        this.to = to;
    }

    public OffsetTime getFrom()
    {
        return from;
    }

    public OffsetTime getTo()
    {
        return to;
    }
    
    @Override
    public boolean isInside(ZonedDateTime dateTime)
    {
        return isInside(OffsetTime.from(dateTime));
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
    public TimeRangeType toTimeRangeType()
    {
        TimeRangeType range = OBJECT_FACTORY.createTimeRangeType();
        range.setFrom(DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(from.getHour(), from.getMinute(), 0, 0));
        range.setTo(DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(to.getHour(), to.getMinute(), 0, 0));
        return range;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.from);
        hash = 41 * hash + Objects.hashCode(this.to);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final OffsetTimeRange other = (OffsetTimeRange) obj;
        if (!Objects.equals(this.from, other.from))
        {
            return false;
        }
        if (!Objects.equals(this.to, other.to))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "OffsetTimeRange{" + "from=" + from + ", to=" + to + '}';
    }
}
