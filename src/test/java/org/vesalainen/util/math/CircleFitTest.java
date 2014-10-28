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

import java.util.Random;
import org.ejml.data.DenseMatrix64F;
import static org.ejml.ops.CommonOps.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.util.math.LevenbergMarquardt.Function;
import org.vesalainen.util.math.LevenbergMarquardt.JacobianFactory;

/**
 *
 * @author Timo Vesalainen
 */
public class CircleFitTest
{
    private static final double Epsilon = 1e-3;
    
    public CircleFitTest()
    {
    }

    /**
     * Test of optimize method, of class LevenbergMarquardt.
     */
    @Test
    public void testOptimize1()
    {
        DenseMatrix64F param = new DenseMatrix64F(2, 1, true, 60, 30);    //98.680, 47.345); 
        DenseMatrix64F x = new DenseMatrix64F(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(96.076, parameters.data[0], Epsilon);
        assertEquals(48.135, parameters.data[1], Epsilon);

    }

    @Test
    public void testOptimize2()
    {
        DenseMatrix64F param = new DenseMatrix64F(2, 1, true, 0.5, 0.5);
        DenseMatrix64F x = new DenseMatrix64F(20, 2);
        for (int ii=0;ii<x.numRows;ii++)
        {
            x.set(ii, 0, Math.cos(Math.toRadians(ii)));
            x.set(ii, 1, Math.sin(Math.toRadians(ii)));
        }
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(0, parameters.data[0], Epsilon);
        assertEquals(0, parameters.data[1], Epsilon);

    }

    @Test
    public void testOptimize3()
    {
        DenseMatrix64F param = new DenseMatrix64F(2, 1, true, 60, 30);    //98.680, 47.345); 
        DenseMatrix64F x = new DenseMatrix64F(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        
        Cost cost = new Cost();
        LevenbergMarquardt lm = new LevenbergMarquardt(cost, cost);
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(96.076, parameters.data[0], Epsilon);
        assertEquals(48.135, parameters.data[1], Epsilon);

    }

    @Test
    public void testOptimize4()
    {
        DenseMatrix64F initParam = new DenseMatrix64F(2, 1, true, 0.5, 0.5);
        DenseMatrix64F expParam = new DenseMatrix64F(2, 1, true, 0.0, 0.0);
        DenseMatrix64F x = new DenseMatrix64F(20, 2);
        Random rand = new Random(1234567L);
        for (int ii=0;ii<x.numRows;ii++)
        {
            double r = 100-rand.nextDouble();
            x.set(ii, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(ii, 1, r*Math.sin(Math.toRadians(ii)));
        }
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        
        Cost cost = new Cost();
        LevenbergMarquardt lm = new LevenbergMarquardt(cost, cost);
        lm.setMaxDifference(1e-20);
        boolean ok = lm.optimize(initParam, x, y);
        assertTrue(ok);
        DenseMatrix64F parameters = lm.getParameters();
        assertEquals(-0.5069, parameters.data[0], Epsilon);
        assertEquals(0.26379, parameters.data[1], Epsilon);

    }

    @Test
    public void testOptimize5()
    {
        DenseMatrix64F center = new DenseMatrix64F(2, 1);
        DenseMatrix64F x = new DenseMatrix64F(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter(center);
        assertEquals(98.680, center.data[0], Epsilon);
        assertEquals(47.345, center.data[1], Epsilon);
    }

    @Test
    public void testOptimize6()
    {
        DenseMatrix64F center = new DenseMatrix64F(2, 1);
        DenseMatrix64F x = new DenseMatrix64F(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter(center);
        cf.fit(x);
        center = cf.getCenter();
        assertEquals(96.076, center.data[0], Epsilon);
        assertEquals(48.135, center.data[1], Epsilon);
        assertEquals(69.960, cf.getRadius(), Epsilon);

    }

    @Test
    public void testOptimize7()
    {
        DenseMatrix64F x = new DenseMatrix64F(40, 2);
        Random rand = new Random(1234567L);
        for (int ii=0;ii<x.numRows/2;ii++)
        {
            double r = 10;
            x.set(2*ii, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(2*ii, 1, r*Math.sin(Math.toRadians(ii)));
            r = 10-rand.nextDouble();
            x.set(2*ii+1, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(2*ii+1, 1, r*Math.sin(Math.toRadians(ii)));
        }
        DenseMatrix64F y = new DenseMatrix64F(x.numRows, 1);
        
        DenseMatrix64F center = new DenseMatrix64F(2, 1);
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter(center);
        cf.fit(x);
        CircleFitter.filterInnerPoints(x, center);
        cf.fit(x);
        center = cf.getCenter();
        assertEquals(0, center.data[0], 1e-2);
        assertEquals(0, center.data[1], 1e-2);
        assertEquals(10, cf.getRadius(), 1e-1);

    }

    @Test
    public void testOptimize8()
    {
        DenseMatrix64F center = new DenseMatrix64F(2, 1);
        DenseMatrix64F x = new DenseMatrix64F(9, 2, true,
            -13.602773018220677,28.130834508313097,
            -13.602773044502694,28.130821250670042,
            -13.602774348328497,28.130810813854385,
            -13.602762318310992,28.130817362398876,
            -13.602747670728927,28.13082731668651,
            -13.602738195918603,28.130832434482198,
            -13.602728476575306,28.130836622251906,
            -13.602719408232623,28.13084125968372,
            -13.602719038439805,28.13085215668995        
        );
        
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter(center);
        cf.fit(x);
        center = cf.getCenter();
        assertEquals(-13.603, center.data[0], Epsilon);
        assertEquals(28.131, center.data[1], Epsilon);
        assertEquals(3.76021e-5, cf.getRadius(), Epsilon);

    }

    public class Cost implements Function, JacobianFactory
    {
        private DenseMatrix64F di = new DenseMatrix64F(1);
        
        private void computeDi(DenseMatrix64F param, DenseMatrix64F x)
        {
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            if (di.numRows != x.numRows)
            {
                di.reshape(x.numRows, 1);
            }
            
            for (int row=0;row<x.numRows;row++)
            {
                double xd = xx - x.get(row, 0);
                double yd = yy - x.get(row, 1);
                double r = Math.sqrt(xd*xd+yd*yd);
                di.set(row, 0, r);
            }
        }
        @Override
        public void compute(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F y)
        {
            computeDi(param, x);
            double r = elementSum(di) / (double)x.numRows;
            for (int row=0;row<x.numRows;row++)
            {
                y.data[row] = di.data[row] - r;
            }
        }

        @Override
        public void computeJacobian(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F jacobian)
        {
            computeDi(param, x);
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            double sumXDk = 0;
            double sumYDk = 0;
            int n = x.numRows;
            for (int i=0;i<n;i++)
            {
                sumXDk += (xx - x.get(i, 0))/di.data[i];
            }
            double xDk = sumXDk / n;
            for (int i=0;i<n;i++)
            {
                sumYDk += (yy - x.get(i, 1))/di.data[i];
            }
            double yDk = sumYDk / n;
            for (int i=0;i<n;i++)
            {
                jacobian.set(0, i, (xx - x.get(i, 0))/di.data[i] - xDk);
                jacobian.set(1, i, (yy - x.get(i, 1))/di.data[i] - yDk);
            }
        }
        
    }
}
