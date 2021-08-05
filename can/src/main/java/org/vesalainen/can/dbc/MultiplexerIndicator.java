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
package org.vesalainen.can.dbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.SimpleIntRange;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiplexerIndicator
{
    private List<SimpleIntRange> ranges = new ArrayList<>();
    private final boolean extended;

    public MultiplexerIndicator()
    {
        this.extended = false;
    }

    public MultiplexerIndicator(int value)
    {
        this(value, false);
    }

    public MultiplexerIndicator(int value, boolean extended)
    {
        this.ranges.add((SimpleIntRange) SimpleIntRange.getInstance(value));
        this.extended = extended;
    }

    public boolean isMultiplexor()
    {
        return !ranges.isEmpty() || extended;
    }

    public boolean isExtended()
    {
        return extended;
    }

    public IntStream getValues()
    {
        if (ranges.isEmpty())
        {
            return IntStream.empty();
        }
        IntStream is = ranges.get(0).stream();
        for (int ii=1;ii<ranges.size();ii++)
        {
            is = IntStream.concat(is, ranges.get(ii).stream());
        }
        return is;
    }

    public void setValue(int value)
    {
        this.ranges.add((SimpleIntRange) SimpleIntRange.getInstance(value));
    }

    public void setRange(int from, int to)
    {
        this.ranges.add(new SimpleIntRange(from, to+1));
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.ranges);
        hash = 37 * hash + (this.extended ? 1 : 0);
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
        final MultiplexerIndicator other = (MultiplexerIndicator) obj;
        if (this.extended != other.extended)
        {
            return false;
        }
        if (!Objects.equals(this.ranges, other.ranges))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return ranges.isEmpty() ? "M" : extended ? "m"+value()+"M" : "m"+value();
    }

    private String value()
    {
        return getValues().findFirst().getAsInt()+"";
    }
    
}
