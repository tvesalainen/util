/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.PrimitiveIterator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleIntRange implements IntRange
{
    public static final IntRange EMPTY_RANGE = new EmptyRange();
    
    private static IntRange[] SINGLES = new IntRange[256];
    static
    {
        for (int ii=0;ii<256;ii++)
        {
            SINGLES[ii] = new SimpleIntRange(ii);
        }
    }
    private int from;
    private int to;

    public SimpleIntRange(int cc)
    {
        this(cc, cc+1);
    }

    public SimpleIntRange(int from, int to)
    {
        if (from >= to)
        {
            throw new IllegalArgumentException("from > to");
        }
        this.from = from;
        this.to = to;
    }

    public static IntRange getInstance(int cc)
    {
        if (cc >= 0 && cc <= 255)
        {
            return SINGLES[cc];
        }
        else
        {
            return new SimpleIntRange(cc);
        }
    }
    @Override
    public int getFrom()
    {
        return from;
    }

    @Override
    public int getTo()
    {
        return to;
    }

    @Override
    public String toString()
    {
        int t = to-1;
        return "["+from+'-'+t+']';
    }

    public static class EmptyRange implements IntRange
    {

        @Override
        public int getFrom()
        {
            return 0;
        }

        @Override
        public int getTo()
        {
            return 0;
        }

        @Override
        public String toString()
        {
            return "[]";
        }
    }

}
