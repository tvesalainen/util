/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.concurrent;

/**
 * @deprecated Not used at all
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingSpan
{
    private int modulo;
    private int start;
    private int end;

    public RingSpan(int modulo)
    {
        if (modulo <= 1)
        {
            throw new IllegalArgumentException("illegal modulo "+modulo);
        }
        this.modulo = modulo;
    }
    public void addStart(int count)
    {
        if (count > length())
        {
            throw new IllegalArgumentException("illegal count "+count);
        }
        start += count;
    }
    public int increment()
    {
        return increment(1);
    }
    public int increment(int count)
    {
        if (count < 0 || count > modulo)
        {
            throw new IllegalArgumentException("illegal count "+count);
        }
        if (length() > modulo-count)
        {
            throw new IllegalArgumentException("buffer underflow");
        }
        end+=count;
        return end % modulo;
    }
    public int length()
    {
        return end-start;
    }
    public int length(RingSpan oth)
    {
        return oth.start - start;
    }
    public void clear()
    {
        //end %= modulo; TODO 
        start = end;
    }

    public int start()
    {
        return start % modulo;
    }

    public int end()
    {
        return end % modulo;
    }
    
}
