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

import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

/**
 * BasicScale provides support for basic scale 1, 2, 3, ...
 * <p>With multiplier other than 5.0 5, 10, 15, ... 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicScale implements Scale
{
    /**
     * 1, 2, 3, 
     */
    public static final Scale SCALE10 = new BasicScale();
    /**
     * 3, 6, 9, ...
     */
    public static final Scale SCALE03 = new BasicScale(3);
    /**
     * 5, 10, 15, ...
     */
    public static final Scale SCALE05 = new BasicScale(5);
    protected final double multiplier;
    protected final String unit;
    protected final DoubleUnaryOperator transform;
    protected double minDelta = Double.MIN_VALUE;
    protected double maxDelta = Double.MAX_VALUE;
    protected final int lowestExponent;
    /**
     * Creates BasicScale with 1.0 multiplier, no unit and no transform.
     */
    public BasicScale()
    {
        this(1.0, "", (d)->d);
    }
    /**
     * Creates BasicScale with given multiplier, no unit and no transform.
     * @param multiplier 
     */
    public BasicScale(double multiplier)
    {
        this(multiplier, "", (d)->d);
    }
    /**
     * Creates BasicScale with given multiplier, given unit and no transform.
     * @param multiplier
     * @param unit 
     */
    public BasicScale(double multiplier, String unit)
    {
        this(multiplier, unit, (d)->d);
    }
    /**
     * Creates BasicScale 
     * @param multiplier
     * @param unit
     * @param transform Transforms values to scale values
     */
    public BasicScale(double multiplier, String unit, DoubleUnaryOperator transform)
    {
        this.multiplier = multiplier;
        this.unit = unit.replace("%", "%%");
        this.transform = transform;
        this.lowestExponent = exponent(minDelta);
    }

    @Override
    public Iterator<ScaleLevel> iterator(double min, double max)
    {
        if (min >= max)
        {
            throw new IllegalArgumentException("min >= max");
        }
        return iterator(transform.applyAsDouble(max)-transform.applyAsDouble(min));
    }
    private Iterator<ScaleLevel> iterator(double delta)
    {
        if (delta > maxDelta)
        {
            return new Iter(exponent(maxDelta/multiplier));
        }
        else
        {
            if (delta < minDelta)
            {
                return Collections.emptyIterator();
            }
            else
            {
                return new Iter(exponent(delta/multiplier));
            }
        }
    }
    public BasicScale setMinDelta(double minDelta)
    {
        this.minDelta = transform.applyAsDouble(minDelta);
        return this;
    }

    public BasicScale setMaxDelta(double maxDelta)
    {
        this.maxDelta = transform.applyAsDouble(maxDelta);
        return this;
    }
    
    protected void format(Formatter formatter, double value, AbstractScaleLevel caller)
    {
        caller.format(formatter, value);
    }
    private static int exponent(double delta)
    {
        return (int) Math.floor(Math.log10(delta));
    }
    protected class BasicScaleLevel extends AbstractScaleLevel
    {

        BasicScaleLevel(int exponent, double multiplier, String unit)
        {
            super(multiplier * Math.pow(10, exponent), String.format("%%.%df%s", exponent < 0 ? (int) -exponent : 0, unit));
        }

        @Override
        public String label(Locale locale, double value)
        {
            StringBuilder out = new StringBuilder();
            Formatter formatter = new Formatter(out, locale);
            BasicScale.this.format(formatter, transform.applyAsDouble(value), this);
            return out.toString();
        }

    }
    public class Iter implements Iterator<ScaleLevel>
    {
        private int exponent;

        public Iter(int exponent)
        {
            this.exponent = exponent;
        }

        @Override
        public boolean hasNext()
        {
            return exponent >= lowestExponent;
        }

        @Override
        public ScaleLevel next()
        {
            return new BasicScaleLevel(exponent--, multiplier, unit);
        }

    }
}
