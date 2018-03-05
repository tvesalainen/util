/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed isInside the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.ham.itshfbc;

import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.vesalainen.util.ArrayIterator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class TimeRanges
{
    public static final TimeRanges ALWAYS = new Always();
    public abstract boolean isInside(OffsetTime time);
    
    public static final TimeRanges getInstance(String... ranges)
    {
        if (ranges == null || ranges.length == 0)
        {
            return ALWAYS;
        }
        else
        {
            return new TimeRangesImpl(new ArrayIterator(ranges));
        }
    }
    public static final TimeRanges getInstance(List<String> ranges)
    {
        if (ranges == null || ranges.size() == 0)
        {
            return ALWAYS;
        }
        else
        {
            return new TimeRangesImpl(ranges.iterator());
        }
    }
    public static class TimeRangesImpl extends TimeRanges
    {
        private List<OffsetTimeRange> ranges = new ArrayList<>();

        public TimeRangesImpl(Iterator<String> iterator)
        {
            while (iterator.hasNext())
            {
                ranges.add(new OffsetTimeRange(iterator.next()));
            }
        }
        
        @Override
        public boolean isInside(OffsetTime time)
        {
            for (OffsetTimeRange range : ranges)
            {
                if (range.isInside(time))
                {
                    return true;
                }
            }
            return false;
        }
        
    }
    public static class Always extends TimeRanges
    {

        private Always()
        {
        }

        @Override
        public boolean isInside(OffsetTime time)
        {
            return true;
        }
        
    }
}
