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

import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarTransform implements DoubleTransform
{
    private static final double PI2 = 2*Math.PI;
    private double C;

    public PolarTransform()
    {
        this(false);
    }
    public PolarTransform(boolean useRadians)
    {
        if (useRadians)
        {
            C = 1;
        }
        else
        {
            C = Math.PI/180;
        }
    }
    
    @Override
    public void transform(double x, double y, DoubleBiConsumer term)
    {
        if (y < 0)
        {
            throw new IllegalArgumentException("not defined for negative values");
        }
        term.accept(y*Math.sin(C*x), y*Math.cos(C*x));
    }

    @Override
    public DoubleTransform inverse()
    {
        return (x,y, c)->c.accept(((PI2+Math.atan2(x, y))%PI2)/C, Math.hypot(x, y));
    }

    @Override
    public DoubleBinaryOperator fx()
    {
        return (x,y)->y*Math.sin(C*x);
    }

    @Override
    public DoubleBinaryOperator fy()
    {
        return (x,y)->y*Math.cos(C*x);
    }

    @Override
    public DoubleBinaryMatrix gradient()
    {
        return new DoubleBinaryMatrix(2, 
                (x,y)->y*Math.cos(C*x)*C, 
                (x,y)->Math.sin(C*x), 
                (x,y)->-y*Math.sin(C*x)*C,
                (x,y)->Math.cos(C*x)
        );
    }

}
