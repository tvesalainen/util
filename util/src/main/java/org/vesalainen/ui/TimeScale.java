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
package org.vesalainen.ui;

import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeScale implements Scale
{
    public static final Scale SCALE10 = new BasicScale(1, "s").setMaxDelta(1);
    public static final Scale SCALE05 = new BasicScale(5, "s").setMaxDelta(1);

    @Override
    public Iterator<ScaleLevel> iterator(double delta)
    {
        Iterator<ScaleLevel> basicStuf = Scale.merge(delta, SCALE10, SCALE05);
        if (delta <= 60)
        {
            return basicStuf;
        }
        TimeUnit[] values = TimeUnit.values();
        int length = values.length;
        for (int ii=4;ii<length;ii++)
        {
            long sec = values[ii].toSeconds(1);
            if (delta < sec)
            {
                return new Iter(values[ii], basicStuf);
            }
        }
        return new Iter(DAYS, basicStuf);
    }

    private static class Iter implements Iterator<ScaleLevel>
    {
        private TimeUnit unit;
        private Iterator<ScaleLevel> tail;
        private boolean isTail;

        public Iter(TimeUnit unit, Iterator<ScaleLevel> tail)
        {
            this.unit = unit;
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
            if (isTail)
            {
                return new TailScaleLevel(tail.next());
            }
            else
            {
                TimeScaleLevel res = new TimeScaleLevel(unit);
                unit = TimeUnit.values()[unit.ordinal()-1];
                if (unit == SECONDS)
                {
                    isTail = true;
                }
                return res;
            }
        }
    }
    public static double appendTo(TimeUnit to, StringBuilder out, double value)
    {
        boolean ok = false;
        long days = SECONDS.toDays((long) value);
        if (days != 0)
        {
            out.append(days).append("d");
            value -= DAYS.toSeconds(days);
            ok = true;
        }
        if (to == DAYS)
        {
            return value;
        }
        long hours = SECONDS.toHours((long) value);
        if (ok || hours != 0)
        {
            out.append(hours).append("h");
            value -= HOURS.toSeconds(hours);
            ok = true;
        }
        if (to == HOURS)
        {
            return value;
        }
        long minutes = SECONDS.toMinutes((long) value);
        if (ok || minutes != 0)
        {
            out.append(minutes).append("m");
            value -= MINUTES.toSeconds(minutes);
            ok = true;
        }
        return value;
    }
    private static class TimeScaleLevel extends AbstractScaleLevel
    {
        private TimeUnit unit;
        
        public TimeScaleLevel(TimeUnit unit)
        {
            super(unit.toSeconds(1));
            this.unit = unit;
        }

        @Override
        public String label(Locale locale, double value)
        {
            StringBuilder sb = new StringBuilder();
            appendTo(unit, sb, value);
            return sb.toString();
        }
        
    }
    private static class TailScaleLevel implements ScaleLevel
    {
        private ScaleLevel level;

        public TailScaleLevel(ScaleLevel level)
        {
            this.level = level;
        }
        @Override
        public String label(Locale locale, double value)
        {
            StringBuilder sb = new StringBuilder();
            value = appendTo(MINUTES, sb, value);
            sb.append(level.label(locale, value));
            return sb.toString();
        }

        @Override
        public double step()
        {
            return level.step();
        }
    }
}
