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
public abstract class AbstractScale implements Scale
{
    private OrderedList<HeadScaleLevel> levels = new OrderedList<>();
    private List<Scale> tail = new ArrayList<>();

    protected AbstractScale()
    {
    }
    
    protected void addLevel(double step, String format)
    {
        levels.add(new HeadScaleLevel(step, format));
    }
    protected void addTail(double maxDelta, String unit, double... multipliers)
    {
        for (double multiplier : multipliers)
        {
            tail.add(new BasicScale(multiplier, unit).setMaxDelta(maxDelta));
        }
    }
    @Override
    public Iterator<ScaleLevel> iterator(double delta)
    {
        HeadScaleLevel key = new HeadScaleLevel(delta, null);
        Iterator<HeadScaleLevel> headIterator = levels.tailIterator(key, true);
        Iterator<ScaleLevel> tailIterator = Scale.merge(delta, tail);
        return new Iter(headIterator, tailIterator);
    }

    protected double formatPrefix(Formatter formatter, double value)
    {
        return value;
    }
    
    private class Iter implements Iterator<ScaleLevel>
    {
        private Iterator<HeadScaleLevel> head;
        private Iterator<ScaleLevel> tail;
        private boolean isTail;

        public Iter(Iterator<HeadScaleLevel> head, Iterator<ScaleLevel> tail)
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
            return new TailScaleLevel(tail.next());
        }
    }
    private String format(Locale locale, double value, ScaleLevel until, String suffix)
    {
        StringBuilder out = new StringBuilder();
        Formatter formatter = new Formatter(out, locale);
        value = formatPrefix(formatter, value);
        boolean always = false;
        for (AbstractScaleLevel level : levels)
        {
            double v = value / level.step;
            if (always || v >= 1.0)
            {
                formatter.format(level.format, Math.floor(v));
                always = true;
            }
            value %= level.step;
            if (level == until)
            {
                break;
            }
        }
        if (suffix != null)
        {
            formatter.format(suffix, value);
        }
        formatter.flush();
        return out.toString();
    }
    private class HeadScaleLevel extends AbstractScaleLevel
    {

        public HeadScaleLevel(double step, String format)
        {
            super(step, format);
        }
        @Override
        public String label(Locale locale, double value)
        {
            return AbstractScale.this.format(locale, value, this, null);
        }

    }
    private class TailScaleLevel implements ScaleLevel
    {
        private ScaleLevel level;

        public TailScaleLevel(ScaleLevel level)
        {
            this.level = level;
        }
        @Override
        public String label(Locale locale, double value)
        {
            return AbstractScale.this.format(locale, value, null, format());
         }

        @Override
        public double step()
        {
            return level.step();
        }

        @Override
        public String format()
        {
            return level.format();
        }
    }
}
