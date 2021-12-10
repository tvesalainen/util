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

import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractFitter implements LevenbergMarquardt.Function, LevenbergMarquardt.JacobianFactory
{
    protected final ReadableDoubleMatrix points;
    protected final DoubleMatrix params;
    protected final ReadableDoubleMatrix result;
    protected final LevenbergMarquardt levenbergMarquardt = new LevenbergMarquardt(this, this);

    public AbstractFitter(int arguments, double... initialParams)
    {
        this(new DoubleMatrix(0, arguments), initialParams);
    }
    public AbstractFitter(ReadableDoubleMatrix points, double... initialParams)
    {
        this(points, new DoubleMatrix(points.rows(), 1), initialParams);
    }
    public AbstractFitter(ReadableDoubleMatrix points, ReadableDoubleMatrix result, double... initialParams)
    {
        this.points = points;
        this.result = result;
        this.params = new DoubleMatrix(initialParams.length, 1, initialParams);
    }

    public double fit()
    {
        if (levenbergMarquardt.optimize(params, points, result))
        {
            params.set(levenbergMarquardt.getParameters());
            return levenbergMarquardt.getFinalCost();
        }
        else
        {
            throw new IllegalArgumentException("Fit failed");
        }
    }
    public void addPoints(double... row)
    {
        if (points instanceof DoubleMatrix && result instanceof DoubleMatrix)
        {
            DoubleMatrix m = (DoubleMatrix) points;
            DoubleMatrix r = (DoubleMatrix) result;
            if (row.length != m.columns()+1)
            {
                throw new IllegalArgumentException("illegal number of arguments");
            }
            int rows = m.rows();
            int columns = m.columns();
            m.reshape(rows+1, columns, true);
            for (int ii=0;ii<columns;ii++)
            {
                m.set(rows, ii, row[ii]);
            }
            r.addRow(row[row.length-1]);
        }
        else
        {
            throw new UnsupportedOperationException("not supported with ReadableDoubleMatrix");
        }
    }

    public double[] getParams()
    {
        return params.data();
    }
    
    public int getPointCount()
    {
        return points.rows();
    }
    @Override
    public void computeJacobian(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix jacobian)
    {
        levenbergMarquardt.computeNumericalJacobian(param, x, jacobian);
    }
}
