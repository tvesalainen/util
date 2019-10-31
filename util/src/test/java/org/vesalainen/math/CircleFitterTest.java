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

import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.LevenbergMarquardt.Function;
import org.vesalainen.math.LevenbergMarquardt.JacobianFactory;
import org.vesalainen.math.matrix.DoubleMatrix;

/**
 *
 * @author Timo Vesalainen
 */
public class CircleFitterTest
{
    private static final double Epsilon = 1e-3;
    
    public CircleFitterTest()
    {
    }

    @Test
    public void testLimitDistance1()
    {
        DoubleMatrix p0 = new DoubleMatrix(2, 1, true, 1, 2);
        DoubleMatrix pr = new DoubleMatrix(2, 1, true, 5, 6);
        CircleFitter.limitDistance(p0, pr, 0, 1);
        double sq2 = Math.sqrt(2)/2.0;
        assertEquals(1+sq2, pr.data(0), Epsilon);
        assertEquals(2+sq2, pr.data(1), Epsilon);

    }

    @Test
    public void testLimitDistance2()
    {
        DoubleMatrix p0 = new DoubleMatrix(2, 1, true, 1, 2);
        DoubleMatrix pr = new DoubleMatrix(2, 1, true, 1.1, 2.1);
        CircleFitter.limitDistance(p0, pr, 1, 2);
        double sq2 = Math.sqrt(2)/2.0;
        assertEquals(1+sq2, pr.data(0), Epsilon);
        assertEquals(2+sq2, pr.data(1), Epsilon);

    }

    @Test
    public void testLimitDistance3()
    {
        DoubleMatrix p0 = new DoubleMatrix(2, 1, true, 1, 2);
        DoubleMatrix pr = new DoubleMatrix(2, 1, true, 1, 6);
        CircleFitter.limitDistance(p0, pr, 1, 2);
        assertEquals(1, pr.data(0), Epsilon);
        assertEquals(2+2, pr.data(1), Epsilon);

    }

    @Test
    public void testLimitDistance4()
    {
        DoubleMatrix p0 = new DoubleMatrix(2, 1, true, 1, 6);
        DoubleMatrix pr = new DoubleMatrix(2, 1, true, 1, 2);
        CircleFitter.limitDistance(p0, pr, 1, 2);
        assertEquals(1, pr.data(0), Epsilon);
        assertEquals(6-2, pr.data(1), Epsilon);

    }

    @Test
    public void testMeanCenter()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1);
        DoubleMatrix points = new DoubleMatrix(4, 2, true,
                1, 1,
                2, 1,
                2, 2,
                1, 2
        );
        CircleFitter.meanCenter(points, center);
        assertEquals(1.5, center.data(0), Epsilon);
        assertEquals(1.5, center.data(1), Epsilon);

    }

    /**
     * Test of optimize method, of class LevenbergMarquardt.
     */
    @Test
    public void testOptimize1()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1, true, 60, 30);
        DoubleMatrix points = new DoubleMatrix(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        DoubleMatrix y = new DoubleMatrix(points.rows(), 1);
        
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(center, points, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(96.076, parameters.data(0), Epsilon);
        assertEquals(48.135, parameters.data(1), Epsilon);

    }

    @Test
    public void testOptimize2()
    {
        DoubleMatrix param = new DoubleMatrix(2, 1, true, 0.5, 0.5);
        DoubleMatrix x = new DoubleMatrix(20, 2);
        for (int ii=0;ii<x.rows();ii++)
        {
            x.set(ii, 0, Math.cos(Math.toRadians(ii)));
            x.set(ii, 1, Math.sin(Math.toRadians(ii)));
        }
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        
        LevenbergMarquardt lm = new LevenbergMarquardt(new Cost());
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(0, parameters.data(0), Epsilon);
        assertEquals(0, parameters.data(1), Epsilon);

    }

    @Test
    public void testOptimize3()
    {
        DoubleMatrix param = new DoubleMatrix(2, 1, true, 60, 30);    //98.680, 47.345); 
        DoubleMatrix x = new DoubleMatrix(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        
        Cost cost = new Cost();
        LevenbergMarquardt lm = new LevenbergMarquardt(cost, cost);
        boolean ok = lm.optimize(param, x, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(96.076, parameters.data(0), Epsilon);
        assertEquals(48.135, parameters.data(1), Epsilon);

    }

    @Test
    public void testOptimize4()
    {
        DoubleMatrix initParam = new DoubleMatrix(2, 1, true, 0.5, 0.5);
        DoubleMatrix expParam = new DoubleMatrix(2, 1, true, 0.0, 0.0);
        DoubleMatrix x = new DoubleMatrix(20, 2);
        Random rand = new Random(1234567L);
        for (int ii=0;ii<x.rows();ii++)
        {
            double r = 100-rand.nextDouble();
            x.set(ii, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(ii, 1, r*Math.sin(Math.toRadians(ii)));
        }
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        
        Cost cost = new Cost();
        LevenbergMarquardt lm = new LevenbergMarquardt(cost, cost);
        lm.setMaxDifference(1e-20);
        boolean ok = lm.optimize(initParam, x, y);
        assertTrue(ok);
        DoubleMatrix parameters = lm.getParameters();
        assertEquals(-0.5069, parameters.data(0), Epsilon);
        assertEquals(0.26379, parameters.data(1), Epsilon);

    }

    @Test
    public void testOptimize5()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1);
        DoubleMatrix x = new DoubleMatrix(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        assertEquals(98.680, center.data(0), Epsilon);
        assertEquals(47.345, center.data(1), Epsilon);
    }

    @Test
    public void testOptimize6()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1);
        DoubleMatrix x = new DoubleMatrix(5, 2, true,
                30, 68,
                50, -6,
                110, -20,
                35, 15,
                45, 97
        );
        
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter();
        cf.fit(center, x);
        assertEquals(96.076, cf.getX(), Epsilon);
        assertEquals(48.135, cf.getY(), Epsilon);
        assertEquals(69.960, cf.getRadius(), Epsilon);

    }

    @Test
    public void testOptimize7()
    {
        DoubleMatrix x = new DoubleMatrix(40, 2);
        Random rand = new Random(1234567L);
        for (int ii=0;ii<x.rows()/2;ii++)
        {
            double r = 10;
            x.set(2*ii, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(2*ii, 1, r*Math.sin(Math.toRadians(ii)));
            r = 10-rand.nextDouble();
            x.set(2*ii+1, 0, r*Math.cos(Math.toRadians(ii)));
            x.set(2*ii+1, 1, r*Math.sin(Math.toRadians(ii)));
        }
        DoubleMatrix y = new DoubleMatrix(x.rows(), 1);
        
        DoubleMatrix center = new DoubleMatrix(2, 1);
        double radius = CircleFitter.initialCenter(x, center);
        assertFalse(Double.isNaN(radius));
        CircleFitter cf = new CircleFitter();
        cf.fit(center, x);
        center.data(0, cf.getX());
        center.data(1, cf.getY());
        CircleFitter.filterInnerPoints(x, center, x.rows()/2, 0.95);
        cf.fit(center, x);
        assertEquals(0, cf.getX(), 1e-2);
        assertEquals(0, cf.getY(), 1e-2);
        assertEquals(10, cf.getRadius(), 1e-1);

    }

    @Test
    public void testOptimize8()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1);
        DoubleMatrix x = new DoubleMatrix(9, 2, true,
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
        CircleFitter cf = new CircleFitter();
        cf.fit(center, x);
        assertEquals(-13.603, cf.getX(), Epsilon);
        assertEquals(28.131, cf.getY(), Epsilon);
        assertEquals(3.76021e-5, cf.getRadius(), Epsilon);

    }

    @Test
    public void testOptimize9()
    {
        DoubleMatrix center = new DoubleMatrix(2, 1);
        DoubleMatrix points = new DoubleMatrix(20, 2, true,
            -13.602771764692406, 28.130542667469683,
            -13.602771764692406, 28.130542667469683,
            -13.602771764692406, 28.130542667469683,
            -13.602781148342935, 28.13057796563436,
            -13.602781965204658, 28.130588268982418,
            -13.602784003797742, 28.13056873625168,
            -13.602791633675045, 28.13058576084586,
            -13.6027966886659, 28.130583818918435,
            -13.602796723045554, 28.130546732492093,
            -13.602799001326073, 28.13057767492597,
            -13.602802252281942, 28.130566492657344,
            -13.602802328189087, 28.130544532776668,
            -13.60280445717498, 28.130553826085254,
            -13.602805542032861, 28.13055697898711,
            -13.602856488497137, 28.130595470661465,
            -13.602856488497137, 28.130595470661465,
            -13.602838249356934, 28.130534710976608,
            -13.602806239495267, 28.130476943469457,
            -13.602783005182733, 28.130513254790376,
            -13.602773066740017, 28.130531422744827
        );
        center.data(0, -13.602787648626254);
        center.data(1, 28.130571761975013);
        CircleFitter.filterInnerPoints(points, center, points.rows()/3, 0.8);
        CircleFitter cf = new CircleFitter();
        cf.fit(center, points);
        assertEquals(-13.603, cf.getX(), Epsilon);
        assertEquals(28.131, cf.getY(), Epsilon);
        assertEquals(3.76021e-5, cf.getRadius(), Epsilon);

    }

    public class Cost implements Function, JacobianFactory
    {
        private DoubleMatrix di = new DoubleMatrix(1, 1);
        
        private void computeDi(DoubleMatrix param, DoubleMatrix x)
        {
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            if (di.rows() != x.rows())
            {
                di.reshape(x.rows(), 1);
            }
            
            for (int row=0;row<x.rows();row++)
            {
                double xd = xx - x.get(row, 0);
                double yd = yy - x.get(row, 1);
                double r = Math.sqrt(xd*xd+yd*yd);
                di.set(row, 0, r);
            }
        }
        @Override
        public void compute(DoubleMatrix param, DoubleMatrix x, DoubleMatrix y)
        {
            computeDi(param, x);
            double r = DoubleMatrix.elementSum(di) / (double)x.rows();
            for (int row=0;row<x.rows();row++)
            {
                y.data(row, di.data(row) - r);
            }
        }

        @Override
        public void computeJacobian(DoubleMatrix param, DoubleMatrix x, DoubleMatrix jacobian)
        {
            computeDi(param, x);
            double xx = param.get(0, 0);
            double yy = param.get(1, 0);
            double sumXDk = 0;
            double sumYDk = 0;
            int n = x.rows();
            for (int i=0;i<n;i++)
            {
                sumXDk += (xx - x.get(i, 0))/di.data(i);
            }
            double xDk = sumXDk / n;
            for (int i=0;i<n;i++)
            {
                sumYDk += (yy - x.get(i, 1))/di.data(i);
            }
            double yDk = sumYDk / n;
            for (int i=0;i<n;i++)
            {
                jacobian.set(0, i, (xx - x.get(i, 0))/di.data(i) - xDk);
                jacobian.set(1, i, (yy - x.get(i, 1))/di.data(i) - yDk);
            }
        }
        
    }
}
