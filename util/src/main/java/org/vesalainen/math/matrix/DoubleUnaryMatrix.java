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
import java.util.function.DoubleUnaryOperator;
import org.vesalainen.math.DoubleUnaryOperators;
import org.vesalainen.math.DoubleUnaryOperators.MultiplyBuilder;
import org.vesalainen.math.DoubleUnaryOperators.SumBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleUnaryMatrix extends Matrix<DoubleUnaryOperator>
{
    private static final DoubleUnaryOperator ZERO = (x)->0;
    
    public DoubleUnaryMatrix(int rows, int cols)
    {
        super(rows, cols, DoubleUnaryOperator.class);
        Arrays.fill((Object[]) array, ZERO);
    }

    public DoubleUnaryMatrix(int rows, DoubleUnaryOperator... array)
    {
        super(rows, array);
    }
    public double eval(int i, int j, double t)
    {
        return get(i, j).applyAsDouble(t);
    }
    public double hypot(double t)
    {
        if ((rows() < 1 || columns() < 1) || (rows() > 1 && columns() > 1))
        {
            throw new IllegalArgumentException("not a vector");
        }
        double sum = 0;
        if (columns() == 1)
        {
            for (int i=0;i<rows();i++)
            {
                double v = eval(i, 0, t);
                sum += v*v;
            }
        }
        else
        {
            for (int j=0;j<columns();j++)
            {
                double v = eval(0, j, t);
                sum += v*v;
            }
        }
        return Math.sqrt(sum);
    }
    public static DoubleUnaryMatrix getInstance(int rows, final double... constants)
    {
        if (rows < 1)
        {
            throw new IllegalArgumentException("rows");
        }
        if (constants.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        DoubleUnaryOperator[] array = new DoubleUnaryOperator[constants.length];
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            double c = constants[ii];
            array[ii] = (x)->c;
        }
        return new DoubleUnaryMatrix(rows, array);
    }
    public DoubleUnaryMatrix multiply(DoubleUnaryMatrix m)
    {
        return DoubleUnaryMatrix.multiply(this, m);
    }
    public static DoubleUnaryMatrix multiply(DoubleUnaryMatrix m1, DoubleUnaryMatrix m2)
    {
        if (m1.columns() != m2.rows())
        {
            throw new IllegalArgumentException("Matrices not comfortable");
        }
        int m = m1.rows();
        int n = m1.columns();
        int p = m2.columns();
        ItemSupplier s1 = m1.supplier;
        ItemSupplier s2 = m2.supplier;
        DoubleUnaryMatrix mr = new DoubleUnaryMatrix(m, p);
        ItemConsumer c = mr.consumer;
        for (int i = 0; i < m; i++)
        {
            for (int j = 0; j < p; j++)
            {
                SumBuilder sum = DoubleUnaryOperators.sumBuilder();
                for (int r = 0; r < n; r++)
                {
                    DoubleUnaryOperator f1 = (DoubleUnaryOperator) s1.get(i, r);
                    DoubleUnaryOperator f2 = (DoubleUnaryOperator) s2.get(r, j);
                    sum.add((x)->f1.applyAsDouble(x)*f2.applyAsDouble(x));
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
    public DoubleUnaryOperator determinant()
    {
        int sign = 1;
        SumBuilder sum = DoubleUnaryOperators.sumBuilder();
        PermutationMatrix pm = PermutationMatrix.getInstance(rows());
        int perms = pm.rows();
        for (int p=0;p<perms;p++)
        {
            MultiplyBuilder mul = DoubleUnaryOperators.multiplyBuilder();
            for (int i=0;i<rows();i++)
            {
                mul.add(get(i, pm.get(p, i)));
            }
            sum.add(DoubleUnaryOperators.sign(sign, mul.build()));
            sign = -sign;
        }
        return sum.build();
    }
    public boolean equals(DoubleUnaryMatrix other, double t, double delta)
    {
        if (rows() != other.rows() || columns() != other.columns())
        {
            return false;
        }
        for (int i = 0; i < rows(); i++)
        {
            for (int j = 0; j < columns(); j++)
            {
                double e1 = eval(i, j, t);
                double e2 = other.eval(i, j, t);
                if (Math.abs(e1-e2) > delta)
                {
                    return false;
                }
            }
        }
        return true;
    }
    public DoubleMatrix snapshot(double t)
    {
        DoubleMatrix m = new DoubleMatrix(rows(), columns());
        for (int i = 0; i < rows(); i++)
        {
            for (int j = 0; j < columns(); j++)
            {
                m.set(i, j, eval(i, j, t));
            }
        }
        return m;
    }
}
