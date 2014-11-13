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

package org.vesalainen.util.math;

import org.ejml.data.DenseMatrix64F;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.util.math.LevenbergMarquardt.Function;
import org.vesalainen.util.math.LevenbergMarquardt.JacobianFactory;

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
        DenseMatrix64F param = new DenseMatrix64F(3, 1, true, 1, 1, 1); 
        DenseMatrix64F x = new DenseMatrix64F(5, 1, true, input);
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        int index = 0;
        for (double xx : input)
        {
            y.data[index++] = expA*xx*xx+expB*xx+expC;
        }
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(expA, parameters.data[0], Epsilon);
        assertEquals(expB, parameters.data[1], Epsilon);
        assertEquals(expC, parameters.data[2], Epsilon);

    }

    @Test
    public void testOptimize2()
    {
        double expA = 2;
        double expB = 5;
        double expC = 1;
        double[] input = new double[] {-1, 0, 1, 2, 3};
        DenseMatrix64F param = new DenseMatrix64F(3, 1, true, 1, 1, 1); 
        DenseMatrix64F x = new DenseMatrix64F(5, 1, true, input);
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        int index = 0;
        for (double xx : input)
        {
            y.data[index++] = expA*xx*xx+expB*xx+expC;
        }
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost(), new JF());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(expA, parameters.data[0], Epsilon);
        assertEquals(expB, parameters.data[1], Epsilon);
        assertEquals(expC, parameters.data[2], Epsilon);

    }

    public class Cost implements Function
    {

        @Override
        public void compute(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F y)
        {
            double a = param.data[0];
            double b = param.data[1];
            double c = param.data[2];
            int index = 0;
            for (double xx : x.data)
            {
                y.data[index++] = a*xx*xx+b*xx+c;
            }
        }
        
    }
    public class JF implements JacobianFactory
    {
        private boolean done;
        
        @Override
        public void computeJacobian(DenseMatrix64F param, DenseMatrix64F pt, DenseMatrix64F deriv)
        {
            if (!done)
            {
                // a deriv = x2
                for (int col=0;col<pt.numRows;col++)
                {
                    double x = pt.get(col, 0);
                    deriv.set(0, col, x*x);
                }
                // b deriv = x
                for (int col=0;col<pt.numRows;col++)
                {
                    double x = pt.get(col, 0);
                    deriv.set(1, col, x);
                }
                // c deriv = 1
                for (int col=0;col<pt.numRows;col++)
                {
                    deriv.set(2, col, 1);
                }
                done = true;
            }
        }
        
    }
}
