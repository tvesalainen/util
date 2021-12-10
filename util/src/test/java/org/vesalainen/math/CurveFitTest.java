/*
 * Copyright (C) 2014 Timo Vesalainen
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

import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.LevenbergMarquardt.Function;
import org.vesalainen.math.LevenbergMarquardt.JacobianFactory;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;

/**
 *
 * @author Timo Vesalainen
 */
public class CurveFitTest
{
    private static final double Epsilon = 1e-3;
    
    public CurveFitTest()
    {
    }

    /**
     * Test of optimize method, of class LevenbergMarquardt.
     */
    @Test
    public void testOptimize1()
    {
        double expA = 2;
        double expB = 5;
        double expC = 1;
        double[] input = new double[] {-1, 0, 1, 2, 3};
        DoubleMatrix param = new DoubleMatrix(3, 1, 1, 1, 1); 
        DoubleMatrix x = new DoubleMatrix(5, 1, input);
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        int index = 0;
        for (double xx : input)
        {
            y.set(index++, 0, expA*xx*xx+expB*xx+expC);
        }
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(expA, parameters.get(0, 0), Epsilon);
        assertEquals(expB, parameters.get(1, 0), Epsilon);
        assertEquals(expC, parameters.get(2, 0), Epsilon);

    }

    @Test
    public void testOptimize2()
    {
        double expA = 2;
        double expB = 5;
        double expC = 1;
        double[] input = new double[] {-1, 0, 1, 2, 3};
        DoubleMatrix param = new DoubleMatrix(3, 1, 1, 1, 1); 
        DoubleMatrix x = new DoubleMatrix(5, 1, input);
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        int index = 0;
        for (double xx : input)
        {
            y.set(index++, 0, expA*xx*xx+expB*xx+expC);
        }
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost(), new JF());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(expA, parameters.get(0, 0), Epsilon);
        assertEquals(expB, parameters.get(1, 0), Epsilon);
        assertEquals(expC, parameters.get(2, 0), Epsilon);

    }

    public class Cost implements Function
    {

        @Override
        public void compute(DoubleMatrix param, ReadableDoubleMatrix x, DoubleMatrix y)
        {
            double a = param.get(0, 0);
            double b = param.get(1, 0);
            double c = param.get(2, 0);
            int index = 0;
            for (int r=0;r<x.rows();r++)
            {
                double xx = x.get(r, 0);
                y.set(index++, 0, a*xx*xx+b*xx+c);
            }
        }
        
    }
    public class JF implements JacobianFactory
    {
        private boolean done;
        
        @Override
        public void computeJacobian(DoubleMatrix param, ReadableDoubleMatrix pt, DoubleMatrix deriv)
        {
            if (!done)
            {
                // a deriv = x2
                for (int col=0;col<pt.rows();col++)
                {
                    double x = pt.get(col, 0);
                    deriv.set(0, col, x*x);
                }
                // b deriv = x
                for (int col=0;col<pt.rows();col++)
                {
                    double x = pt.get(col, 0);
                    deriv.set(1, col, x);
                }
                // c deriv = 1
                for (int col=0;col<pt.rows();col++)
                {
                    deriv.set(2, col, 1);
                }
                done = true;
            }
        }
        
    }
}
