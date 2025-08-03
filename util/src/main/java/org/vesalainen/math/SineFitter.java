/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import static java.lang.Math.*;
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SineFitter extends FunctionAfxBFitter
{

    public SineFitter()
    {
        super(Math::sin);
    }

    public SineFitter(DoubleMatrix points)
    {
        super(Math::sin, points);
    }

    public SineFitter(ReadableDoubleMatrix points, ReadableDoubleMatrix result, double... initialParams)
    {
        super(Math::sin, points, result, initialParams);
    }
    
    public MathFunction getDerivative()
    {
        double[] param = getParams();
        double coef = param[0];
        double phase = param[1];
        return (x)->coef*cos(x+phase);
    }
    public MathFunction getAntiderivative()
    {
        double[] param = getParams();
        double coef = param[0];
        double phase = param[1];
        return (x)->-coef*cos(x+phase);
    }
    @Override
    public void computeJacobian(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix jacobian)
    {
        double coef = param.get(0, 0);
        double phase = param.get(1, 0);
        int rows = x.rows();
        for (int ii=0;ii<rows;ii++)
        {
            double xx = x.get(ii, 0);
            double ya = sin(xx+phase);
            double yb = coef*cos(xx+phase);
            jacobian.set(0, ii, ya);
            jacobian.set(1, ii, yb);
        }
    }
}
