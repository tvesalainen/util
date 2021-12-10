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

import java.util.function.DoubleUnaryOperator;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;

/**
 * Fitter for a*f(x+b) type function
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionAfxBFitter extends AbstractFitter
{
    private final DoubleUnaryOperator f;
    
    public FunctionAfxBFitter(DoubleUnaryOperator f)
    {
        super(1, 1, 0);
        this.f = f;
    }

    public FunctionAfxBFitter(ReadableDoubleMatrix points, DoubleUnaryOperator f)
    {
        super(points, 1, 0);
        this.f = f;
    }

    @Override
    public void compute(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix y)
    {
        double coef = param.get(0, 0);
        double phase = param.get(1, 0);
        int rows = x.rows();
        for (int ii=0;ii<rows;ii++)
        {
            double xx = x.get(ii, 0);
            double yy = coef*f.applyAsDouble(xx+phase);
            y.set(ii, 0, yy);
        }
    }
    
    public double getParamA()
    {
        double[] param = getParams();
        return param[0];
    }
    public double getParamB()
    {
        double[] param = getParams();
        return param[1];
    }
    
    public MathFunction getFunction()
    {
        double[] param = getParams();
        double coef = param[0];
        double phase = param[1];
        return (x)->coef*f.applyAsDouble(x+phase);
    }
}
