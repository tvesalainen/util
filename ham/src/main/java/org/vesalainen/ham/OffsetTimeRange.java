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

import org.vesalainen.util.Range;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import static org.vesalainen.ham.BroadcastStationsFile.DATA_TYPE_FACTORY;
import static org.vesalainen.ham.BroadcastStationsFile.OBJECT_FACTORY;
import org.vesalainen.ham.jaxb.TimeRangeType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OffsetTimeRange extends Range<OffsetTime> implements TimeRange
{
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
        super(from, to);
    }

    @Override
    public boolean isInRange(OffsetDateTime instant)
    {
        return isInRange(OffsetTime.from(instant));
    }
    public TimeRangeType toTimeRangeType()
    {
        TimeRangeType range = OBJECT_FACTORY.createTimeRangeType();
        range.setFrom(DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(from.getHour(), from.getMinute(), 0, 0));
        range.setTo(DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(to.getHour(), to.getMinute(), 0, 0));
        return range;
    }

    @Override
    public String toString()
    {
        return "OffsetTimeRange{" + "from=" + from + ", to=" + to + '}';
    }
}
