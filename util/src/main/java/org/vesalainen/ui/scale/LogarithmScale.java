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
import java.util.Iterator;
import org.vesalainen.math.Logarithm;
import org.vesalainen.math.MathFunction;
import org.vesalainen.text.Unicodes;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LogarithmScale extends BasicScale
{

    private final double base;

    public LogarithmScale(double base)
    {
        this(base, "");
    }
    public LogarithmScale(double base, String unit)
    {
        super(1, unit, new Logarithm(base));
        this.base = base;
    }

    @Override
    public Iterator<ScaleLevel> iterator(double min, double max)
    {
        if (min <= 0.0)
        {
            throw new IllegalArgumentException("not defined <= 0");
        }
        return super.iterator(min, max);
    }
    
    @Override
    protected void format(Formatter formatter, double value, AbstractScaleLevel caller)
    {
        int exp = (int) floor(function.applyAsDouble(value));
        double coef = value/inverse.applyAsDouble(exp);
        if (coef > 1.1 || coef < 0.9)
        {
            formatter.format("%.1fx", coef);
        }
        if (base == Math.E)
        {
            formatter.format("e");
        }
        else
        {
            formatter.format("%.0f", base);
        }
        StringBuilder out = (StringBuilder) formatter.out();
        Unicodes.toSuperScript(String.valueOf(exp), out);
    }
    private double floor(double v)
    {
        double rint = Math.rint(v);
        if (Math.abs(v-rint) <= Math.ulp(v))
        {
            return rint;
        }
        else
        {
            return Math.floor(v);
        }
    }
}
