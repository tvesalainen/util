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

import java.util.Formatter;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractScaleLevel implements ScaleLevel
{
    protected double step;
    protected String format;

    protected AbstractScaleLevel(double step, String format)
    {
        this.step = step;
        this.format = format;
    }

    @Override
    public double step()
    {
        return step;
    }

    @Override
    public String label(Locale locale, double value)
    {
        StringBuilder out = new StringBuilder();
        Formatter formatter = new Formatter(out, locale);
        format(formatter, value);
        return out.toString();
    }

    protected void format(Formatter formatter, double value)
    {
        formatter.format(format, value);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.step) ^ (Double.doubleToLongBits(this.step) >>> 32));
        hash = 79 * hash + Objects.hashCode(this.format);
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
        final AbstractScaleLevel other = (AbstractScaleLevel) obj;
        if (Double.doubleToLongBits(this.step) != Double.doubleToLongBits(other.step))
        {
            return false;
        }
        if (!Objects.equals(this.format, other.format))
        {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        return "ScaleLevel{" + "step=" + step + '}';
    }

    @Override
    public PrimitiveIterator.OfDouble iterator(double min, double max)
    {
        return new Iter(min, max);
    }
    private class Iter implements PrimitiveIterator.OfDouble
    {
        private double next;
        private double end;

        public Iter(double min, double max)
        {
            double start = Math.floor(min/step)*step;
            if (start != min)
            {
                start += step;
            }
            this.next = start;
            this.end = max;
        }

        @Override
        public double nextDouble()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            double res = next;
            next += step;
            return res;
        }

        @Override
        public boolean hasNext()
        {
            return next <= end;
        }
        
    }
}
