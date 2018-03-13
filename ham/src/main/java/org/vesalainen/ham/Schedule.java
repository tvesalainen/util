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
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.util.SimpleRange;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Schedule<T extends ScheduleType> extends SimpleRange<OffsetTime> implements TimeRange
{
    
    protected Station station;
    protected T schedule;
    protected TimeRange andRanges;
    private int priority;

    public Schedule(Station station, T schedule)
    {
        super(getRange(schedule));
        this.station = station;
        this.schedule = schedule;
        this.andRanges = TimeRanges.andRanges(TimeRanges.orWeekday(schedule.getWeekdays()), TimeRanges.orDateRanges(schedule.getDate()));
        Short pri = schedule.getPriority();
        this.priority = pri != null ? pri : 0;
    }

    protected static <T extends ScheduleType> SimpleRange<OffsetTime> getRange(T schedule)
    {
        XMLGregorianCalendar t = schedule.getTime();
        OffsetTime start = OffsetTime.of(t.getHour(), t.getMinute(), t.getSecond(), 0, ZoneOffset.UTC);
        OffsetTime end = null;
        if (schedule.getDuration() != null)
        {
            Duration duration = Duration.parse(schedule.getDuration().toString());
            end = start.plusNanos(duration.toNanos());
        }
        else
        {
            end = start.plusMinutes(BroadcastStationsFile.DEF_DURATION_MINUTES);
        }
        return new SimpleRange(start, end);
    }

    public TypeOfTransmittedInformation getTypeOfTransmittedInformation()
    {
        return TypeOfTransmittedInformation.E;
    }
    public int getPriority()
    {
        return priority;
    }

    @Override
    public boolean isInRange(OffsetDateTime dateTime)
    {
        return andRanges.isInRange(dateTime);
    }

    public Station getStation()
    {
        return station;
    }

    public String getContent()
    {
        return schedule.getContent();
    }
    
}
