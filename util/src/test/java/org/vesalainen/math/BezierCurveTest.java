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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.math.BezierCurve.LINE;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.math.matrix.DoubleUnaryMatrix;
import org.vesalainen.ui.Points;
import org.vesalainen.ui.Transforms;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BezierCurveTest
{
    
    public BezierCurveTest()
    {
    }

    @Test
    public void test0()
    {
        double[] cp = new double[]{1,2,3,4};
        ParameterizedOperator op1 = LINE.operator(cp[0], cp[1], cp[2], cp[3]);
        ParameterizedOperator op2 = LINE.operator(cp);
        Point2D.Double exp = new Point2D.Double();
        op1.calc(0.5, exp::setLocation);
        Point2D.Double got = new Point2D.Double();
        cp[1] = 5;
        op1.calc(0.5, got::setLocation);
        assertEquals(exp, got);
        op2.calc(0.5, got::setLocation);
        assertNotEquals(exp, got);
    }
    @Test
    public void test1()
    {
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        Point2D.Double p2 = new Point2D.Double(-1,-2);
        Point2D.Double p3 = new Point2D.Double(2,1);
        BezierCurve bc = BezierCurve.getInstance(3);;
        assertEquals(p0, bc.calc(0, p0, p1, p2, p3));
        assertEquals(p3, bc.calc(1, p0, p1, p2, p3));
        assertTrue(bc.pathLength(0.1, p0, p1, p2, p3) < bc.pathLengthEstimate(p0, p1, p2, p3));
    }
    @Test
    public void testDerivate()
    {
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        Point2D.Double p2 = new Point2D.Double(-1,-2);
        Point2D.Double p3 = new Point2D.Double(2,1);
        BezierCurve bc = BezierCurve.getInstance(3);
        ParameterizedOperator op = bc.operator(p0, p1, p2, p3);
        DoubleUnaryMatrix J = op.derivative();
        Point2D.Double exp1 = Points.mul(3, Points.sub(p1, p0));
        Point2D.Double exp2 = Points.mul(3, Points.sub(p3, p2));
        assertEquals(exp1.x, J.eval(0, 0, 0), 1e-10);
        assertEquals(exp1.y, J.eval(1, 0, 0), 1e-10);
        assertEquals(exp2.x, J.eval(0, 0, 1), 1e-10);
        assertEquals(exp2.y, J.eval(1, 0, 1), 1e-10);
    }
    @Test
    public void testSecondDerivate()
    {
        Point2D.Double p0 = new Point2D.Double(2,3);
        Point2D.Double p1 = new Point2D.Double(0,5);
        Point2D.Double p2 = new Point2D.Double(-1,-2);
        Point2D.Double p3 = new Point2D.Double(2,1);
        BezierCurve bc = BezierCurve.getInstance(3);
        ParameterizedOperator op = bc.operator(p0, p1, p2, p3);
        DoubleUnaryMatrix J = op.secondDerivative();
        Point2D.Double exp1 = Points.mul(6, Points.add(p0, Points.mul(-2, p1), p2));
        Point2D.Double exp2 = Points.mul(6, Points.add(p1, Points.mul(-2, p2), p3));
        assertEquals(exp1.x, J.eval(0, 0, 0), 1e-10);
        assertEquals(exp1.y, J.eval(1, 0, 0), 1e-10);
        assertEquals(exp2.x, J.eval(0, 0, 1), 1e-10);
        assertEquals(exp2.y, J.eval(1, 0, 1), 1e-10);
    }
    @Test
    public void testLineDerivate()
    {
        Point2D.Double p0 = new Point2D.Double();
        Point2D.Double p1 = new Point2D.Double(1,1);
        DoubleUnaryMatrix J = LINE.operator(p0, p1).derivative();
        assertEquals(1, J.eval(0, 0, 0.5), 1e-10);
        assertEquals(1, J.eval(1, 0, 0.5), 1e-10);
    }
    @Test
    public void testPathLength1()
    {
        testPathLength(1);
    }
    @Test
    public void testPathLength2()
    {
        testPathLength(2);
    }
    @Test
    public void testPathLength3()
    {
        testPathLength(3);
    }
    public void testPathLength(int degree)
    {
        Random r = new Random(123456);
        BezierCurve bc = BezierCurve.getInstance(degree);
        double[] cp = new double[8];
        for (int ii=0;ii<1000;ii++)
        {
            for (int jj=0;jj<cp.length;jj++)
            {
                cp[jj] = r.nextDouble();
            }
            double pathLength = bc.pathLength(0.01, cp);
            double pathLengthEstimate = bc.pathLengthEstimate(cp);
            System.err.println(pathLength+" < "+pathLengthEstimate);
            assertTrue(pathLength < pathLengthEstimate);
        }
    }
    @Test
    public void testEval()
    {
        Point2D.Double p0 = new Point2D.Double(0,0);
        Point2D.Double p1 = new Point2D.Double(1,5);
        Point2D.Double p2 = new Point2D.Double(2,7);
        Point2D.Double p3 = new Point2D.Double(3,8);
        Point2D.Double p = new Point2D.Double();
        BezierCurve bc = BezierCurve.getInstance(3);
        ParameterizedOperator op = bc.operator(p0, p1, p2, p3);
        assertEquals(0, op.evalY(0), 1e-10);
        assertEquals(8, op.evalY(3), 1e-10);
        assertEquals(0, op.evalX(0), 1e-10);
        assertEquals(3, op.evalX(8), 1e-10);
        op.calc(0.5, p::setLocation);
        assertEquals(p.y, op.evalY(p.x), 1e-10);
        assertEquals(p.x, op.evalX(p.y), 1e-10);
    }
    @Test
    public void testChain()
    {
        Point2D.Double p0 = new Point2D.Double();
        Point2D.Double p1 = new Point2D.Double(1,1);
        ParameterizedOperator op = LINE.operator(p0, p1);
        AffineTransform at = AffineTransform.getScaleInstance(2, 3);
        DoubleTransform t = Transforms.affineTransform(at);
        ParameterizedOperator chain = op.andThen(t);
        DoubleUnaryMatrix J = chain.derivative();
        assertEquals(2, J.eval(0, 0, 0.5), 1e-10);
        assertEquals(3, J.eval(1, 0, 0.5), 1e-10);
    }
}
