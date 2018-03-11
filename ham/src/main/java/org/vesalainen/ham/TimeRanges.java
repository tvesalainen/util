/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, orTimeRanges
 * (at your option) any later version.
 *
 * This program is distributed isInRange the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY orTimeRanges FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham;

import org.vesalainen.util.SimpleRange;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import static org.vesalainen.ham.TimeUtils.WEEKDAY_PARSER;
import org.vesalainen.ham.jaxb.DateRangeType;
import org.vesalainen.ham.jaxb.TimeRangeType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class TimeRanges
{
    public static final TimeRange ALWAYS = new Always();
    
    public static final TimeRange andRanges(TimeRange... ranges)
    {
        return new AndTimeRange(ranges);
    }
    public static final TimeRange orWeekday(List<String> ranges)
    {
        if (ranges == null || ranges.size() == 0)
        {
            return ALWAYS;
        }
        else
        {
            return new OrWeekday(ranges.stream().map((s)->WEEKDAY_PARSER.find(s)).collect(Collectors.toList()));
        }
    }
    public static final TimeRange orDateRanges(List<DateRangeType> ranges)
    {
        if (ranges == null || ranges.size() == 0)
        {
            return ALWAYS;
        }
        else
        {
            return new OrTimeRange(ranges.stream().map(MonthDayRange::new).collect(Collectors.toList()));
        }
    }
    public static final TimeRange orTimeRanges(List<TimeRangeType> ranges)
    {
        if (ranges == null || ranges.size() == 0)
        {
            return ALWAYS;
        }
        else
        {
            return new OrTimeRange(ranges.stream().map(OffsetTimeRange::new).collect(Collectors.toList()));
        }
    }
    public static class OrWeekday implements TimeRange
    {
        private EnumSet<DayOfWeek> set;

        public OrWeekday(List<DayOfWeek> list)
        {
            set = EnumSet.copyOf(list);
        }
        
        @Override
        public boolean isInRange(OffsetDateTime instant)
        {
            return set.contains(instant.getDayOfWeek());
        }
        
    }
    public static class OrTimeRange implements TimeRange
    {
        private List<TimeRange> ranges;

        public OrTimeRange(List<TimeRange> ranges)
        {
            this.ranges = ranges;
        }

        
        @Override
        public boolean isInRange(OffsetDateTime instant)
        {
            for (TimeRange range : ranges)
            {
                if (range.isInRange(instant))
                {
                    return true;
                }
            }
            return false;
        }
        
    }
    public static class AndTimeRange implements TimeRange
    {
        private TimeRange[] ranges;

        public AndTimeRange(TimeRange... ranges)
        {
            this.ranges = ranges;
        }

        
        @Override
        public boolean isInRange(OffsetDateTime instant)
        {
            for (TimeRange range : ranges)
            {
                if (!range.isInRange(instant))
                {
                    return false;
                }
            }
            return true;
        }
        
    }
    public static class Always implements TimeRange
    {

        private Always()
        {
        }

        @Override
        public boolean isInRange(OffsetDateTime instant)
        {
            return true;
        }
        
    }
}
