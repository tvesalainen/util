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
package org.vesalainen.math.matrix;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.DoubleBinaryOperators;
import org.vesalainen.math.DoubleBinaryOperators.MultiplyBuilder;
import org.vesalainen.math.DoubleBinaryOperators.SumBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleBinaryMatrix extends Matrix<DoubleBinaryOperator>
{
    private static final DoubleBinaryOperator ZERO = (x,y)->0;
    
    public DoubleBinaryMatrix(int rows, int cols)
    {
        super(rows, cols, DoubleBinaryOperator.class);
        Arrays.fill((Object[]) array, ZERO);
    }

    public DoubleBinaryMatrix(int rows, DoubleBinaryOperator... array)
    {
        super(rows, array);
    }
    public double eval(int i, int j, double x, double y)
    {
        return get(i, j).applyAsDouble(x, y);
    }
    public double hypot(double x, double y)
    {
        if ((rows < 1 || cols < 1) || (rows > 1 && cols > 1))
        {
            throw new IllegalArgumentException("not a vector");
        }
        double sum = 0;
        if (cols == 1)
        {
            for (int i=0;i<rows;i++)
            {
                double v = eval(i, 0, x, y);
                sum += v*v;
            }
        }
        else
        {
            for (int j=0;j<cols;j++)
            {
                double v = eval(0, j, x, y);
                sum += v*v;
            }
        }
        return Math.sqrt(sum);
    }
    public static DoubleBinaryMatrix getInstance(int rows, final double... constants)
    {
        if (rows < 1)
        {
            throw new IllegalArgumentException("rows");
        }
        if (constants.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        DoubleBinaryOperator[] array = new DoubleBinaryOperator[constants.length];
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            double c = constants[ii];
            array[ii] = (x,y)->c;
        }
        return new DoubleBinaryMatrix(rows, array);
    }
    public DoubleBinaryMatrix multiply(DoubleBinaryMatrix m)
    {
        return DoubleBinaryMatrix.multiply(this, m);
    }
    public static DoubleBinaryMatrix multiply(DoubleBinaryMatrix m1, DoubleBinaryMatrix m2)
    {
        if (m1.cols != m2.rows)
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows;
        int n = m1.cols;
        int p = m2.cols;
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        DoubleBinaryMatrix mr = new DoubleBinaryMatrix(m, p);
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < p; j++)
            {
                SumBuilder sum = DoubleBinaryOperators.sumBuilder();
                for (int r = 0; r < n; r++)
                {
                    DoubleBinaryOperator f1 = (DoubleBinaryOperator) s1.get(i, r);
                    DoubleBinaryOperator f2 = (DoubleBinaryOperator) s2.get(r, j);
                    sum.add((x,y)->f1.applyAsDouble(x,y)*f2.applyAsDouble(x,y));
                }
                c.set(i, j, sum.build());
            }
        }
        return mr;
    }
   /**
     * Composes determinant by using permutations
     * @return 
     */
    public DoubleBinaryOperator determinant()
    {
        int sign = 1;
        SumBuilder sum = DoubleBinaryOperators.sumBuilder();
        PermutationMatrix pm = PermutationMatrix.getInstance(rows);
        int perms = pm.rows;
        for (int p=0;p<perms;p++)
        {
            MultiplyBuilder mul = DoubleBinaryOperators.multiplyBuilder();
            for (int i=0;i<rows;i++)
            {
                mul.add(get(i, pm.get(p, i)));
            }
            sum.add(DoubleBinaryOperators.sign(sign, mul.build()));
            sign = -sign;
        }
        return sum.build();
    }
    public boolean equals(DoubleBinaryMatrix other, double x, double y, double delta)
    {
        if (rows != other.rows || cols != other.cols)
        {
            return false;
        }
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                double e1 = eval(i, j, x, y);
                double e2 = other.eval(i, j, x, y);
                if (Math.abs(e1-e2) > delta)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public DoubleMatrix snapshot(double x, double y)
    {
        DoubleMatrix m = new DoubleMatrix(rows, cols);
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                m.set(i, j, eval(i, j, x, y));
            }
        }
        return m;
    }
}
