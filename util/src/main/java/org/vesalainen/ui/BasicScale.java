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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicScale implements Scale
{
    private double multiplier;
    private double minDelta;
    private double maxDelta;

    public BasicScale()
    {
        this(1.0, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public BasicScale(double multiplier)
    {
        this(multiplier, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public BasicScale(double multiplier, double minDelta, double maxDelta)
    {
        this.multiplier = multiplier;
        this.minDelta = minDelta;
        this.maxDelta = maxDelta;
    }

    @Override
    public ScaleLevel closest(double delta)
    {
        double log10 = Math.log10(delta/multiplier);
        return new BasicScaleLevel(Math.floor(log10));
    }

    @Override
    public boolean inRange(double delta)
    {
        return delta >= minDelta && delta <= maxDelta;
    }
    
    public class BasicScaleLevel implements ScaleLevel
    {
        private final double exponent;
        private final String format;

        public BasicScaleLevel(double exponent)
        {
            this.exponent = exponent;
            this.format = String.format("%%.%df", exponent < 0 ? (int)-exponent : 0);
        }
        
        @Override
        public double step()
        {
            return multiplier*Math.pow(10, exponent);
        }

        @Override
        public String label(Locale locale, double value)
        {
            return String.format(locale, format, value);
        }

        @Override
        public ScaleLevel next()
        {
            return new BasicScaleLevel(Math.rint(exponent-1));
        }

        @Override
        public ScaleLevel prev()
        {
            return new BasicScaleLevel(Math.rint(exponent+1));
        }

        @Override
        public String toString()
        {
            return "BasicScaleLevel{" + "step=" + step() + '}';
        }

        @Override
        public Iterator<ScaleLevel> iterator()
        {
            return new Iter(this);
        }
        
    }
    public class Iter implements Iterator<ScaleLevel>
    {
        private ScaleLevel level;

        public Iter(ScaleLevel level)
        {
            this.level = level;
        }
        
        @Override
        public boolean hasNext()
        {
            return level.step() > minDelta;
        }

        @Override
        public ScaleLevel next()
        {
            ScaleLevel next = level;
            level = level.next();
            return next;
        }
        
    }
}
