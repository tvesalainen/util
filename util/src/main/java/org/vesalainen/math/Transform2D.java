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
package org.vesalainen.math;

import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.matrix.FunctionMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Transform2D implements DoubleTransform
{
    private DoubleBinaryOperator f1;
    private DoubleBinaryOperator f2;

    public Transform2D(DoubleBinaryOperator f1, DoubleBinaryOperator f2)
    {
        this.f1 = f1;
        this.f2 = f2;
    }

    @Override
    public void transform(double x, double y, DoubleBiConsumer term)
    {
        term.accept(f1.applyAsDouble(x, y), f2.applyAsDouble(x, y));
    }
    
    public double getX(double x, double y)
    {
        return f1.applyAsDouble(x, y);
    }
    public double getY(double x, double y)
    {
        return f2.applyAsDouble(x, y);
    }
    public FunctionMatrix gradient()
    {
        return new FunctionMatrix(2, 2,
                MoreMath.dx(f1),
                MoreMath.dy(f1),
                MoreMath.dx(f2),
                MoreMath.dy(f2)
        );
    }
}
