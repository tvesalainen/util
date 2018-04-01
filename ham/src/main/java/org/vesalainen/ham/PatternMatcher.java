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

import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PatternMatcher
{
    private long[] times;
    protected PatternPredicate predicate;
    private long span;
    private int size;
    private int[] errors;
    private int index;
    private int minIndex = -1;

    public PatternMatcher(PatternPredicate predicate, long span, int size)
    {
        this.predicate = predicate;
        this.span = span;
        this.size = size;
        this.times = new long[size];
        this.errors = new int[size];
        reset();
    }
    public void reset()
    {
        index = 0;
        minIndex = -1;
        Arrays.fill(errors, Integer.MAX_VALUE);
        Arrays.fill(times, Integer.MIN_VALUE);
    }
    public int match(boolean b, long time)
    {
        errors[index%size] = 0;
        times[index%size] = time;
        minIndex = -1;
        for (int ii=0;ii<size;ii++)
        {
            int mi = (index+size-ii)%size;
            if (time-times[mi]<span)
            {
                if (!predicate.test(b, time, times[mi]))
                {
                    errors[mi]++;
                }
            }
            else
            {
                minIndex = mi;
                break;
            }
        }
        index++;
        if (minIndex != -1)
        {
            return errors[minIndex];
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }
    public long getTime()
    {
        return times[minIndex];
    }
}
