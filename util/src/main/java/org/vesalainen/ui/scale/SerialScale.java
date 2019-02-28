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
package org.vesalainen.ui.scale;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.vesalainen.util.OrderedList;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class SerialScale implements Scale
{
    
    protected OrderedList<AbstractScaleLevel> levels = new OrderedList<>();
    protected List<Scale> tail = new ArrayList<>();

    public SerialScale()
    {
    }

    protected final void addScaleLevel(AbstractScaleLevel level)
    {
        levels.add(level);
    }
    protected final void addTail(Scale scale)
    {
        tail.add(scale);
    }

    @Override
    public Iterator<ScaleLevel> iterator(double delta)
    {
        AbstractScaleLevel key = new AbstractScaleLevel(delta, null);
        Iterator<? extends ScaleLevel> headIterator = levels.tailIterator(key, true);
        Iterator<ScaleLevel> tailIterator = Scale.merge(delta, tail);
        return new Iter(headIterator, tailIterator);
    }
    protected String format(Locale locale, double value, AbstractScaleLevel until, String suffix)
    {
        StringBuilder out = new StringBuilder();
        Formatter formatter = new Formatter(out, locale);
        format(formatter, value, until, suffix);
        return formatter.toString();
    }
    protected double format(Formatter formatter, double value, AbstractScaleLevel until, String suffix)
    {
        value = formatPrefix(formatter, value);
        boolean always = false;
        for (AbstractScaleLevel level : levels)
        {
            double v = value / level.step();
            if (always || v >= 1.0)
            {
                level.format(formatter, Math.floor(v));
                always = true;
            }
            value %= level.step();
            if (level == until)
            {
                break;
            }
        }
        return value;
    }

    protected double formatPrefix(Formatter formatter, double value)
    {
        return value;
    }
    private class Iter implements Iterator<ScaleLevel>
    {
        private Iterator<? extends ScaleLevel> head;
        private Iterator<? extends ScaleLevel> tail;
        private boolean isTail;

        public Iter(Iterator<? extends ScaleLevel> head, Iterator<? extends ScaleLevel> tail)
        {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean hasNext()
        {
            if (isTail)
            {
                return tail.hasNext();
            }
            else
            {
                return true;
            }
        }

        @Override
        public ScaleLevel next()
        {
            if (!isTail)
            {
                if (head.hasNext())
                {
                    return head.next();
                }
                isTail = true;
            }
            return tail.next();
        }
    }

}
