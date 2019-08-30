/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongSupplier;

/**
 * TimeLimitIterator iterates milli seconds from epoch limit.
 * <p>Example: Now=0, gaps=1, 10, 100 produces 1, 10, 100, 200, 300,...
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeLimitIterator implements OfLong
{
    private final long[] gaps;
    private int index;
    private long begin;

    public TimeLimitIterator(long... gaps)
    {
        this(System::currentTimeMillis, gaps);
    }

    public TimeLimitIterator(LongSupplier now, long... gaps)
    {
        if (gaps.length == 0)
        {
            throw new IllegalArgumentException("no gaps");
        }
        this.gaps = gaps;
        this.begin = now.getAsLong();
    }
    
    @Override
    public long nextLong()
    {
        if (index < gaps.length)
        {
            return begin + gaps[index++];
        }
        else
        {
            return begin + gaps[gaps.length-1]*(2+index++-gaps.length);
        }
    }

    @Override
    public boolean hasNext()
    {
        return true;
    }
    
}
