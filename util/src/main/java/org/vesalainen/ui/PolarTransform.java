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

import java.util.function.DoubleUnaryOperator;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarTransform implements DoubleTransform
{
    private static final double PI2 = 2*Math.PI;
    private DoubleUnaryOperator toRadians;
    private DoubleUnaryOperator fromRadians;

    public PolarTransform()
    {
        this(false);
    }
    public PolarTransform(boolean useRadians)
    {
        if (useRadians)
        {
            this.toRadians = (x)->x;
            this.fromRadians = (x)->x;
        }
        else
        {
            this.toRadians = Math::toRadians;
            this.fromRadians = Math::toDegrees;
        }
    }
    
    @Override
    public void transform(double x, double y, DoubleBiConsumer term)
    {
        if (y < 0)
        {
            throw new IllegalArgumentException("not defined for negative values");
        }
        double a = toRadians.applyAsDouble(x);
        term.accept(y*Math.sin(a), y*Math.cos(a));
    }

    @Override
    public DoubleTransform inverse()
    {
        return (x,y, c)->c.accept(fromRadians.applyAsDouble((PI2+Math.atan2(x, y))%PI2), Math.hypot(x, y));
    }

    @Override
    public DoubleTransform derivative()
    {
        return (x,y, c)->
        {
            double a = toRadians.applyAsDouble(x);
            c.accept(Math.sin(a)+y*Math.cos(a), Math.cos(a)-y*Math.sin(a));
        };
    }

}
