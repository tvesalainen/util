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
package org.vesalainen.ui;

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.math.MoreMath;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarTransformTest
{
    private static final double A0 = 0;
    private static final double A90 = Math.PI/2;
    private static final double A180 = Math.PI;
    private static final double A270 = 3*Math.PI/2;
    public PolarTransformTest()
    {
    }

    @Test
    public void test1()
    {
        PolarTransform pt = new PolarTransform(true);
        Point2D p1 = new Point2D.Double(A0, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(0, 1);
        pt.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-10);
        assertEquals(exp.getY(), p2.getY(), 1e-10);
        DoubleTransform inverse = pt.inverse();
        inverse.transform(p2, exp);
        assertEquals(exp.getX(), p1.getX(), 1e-10);
        assertEquals(exp.getY(), p1.getY(), 1e-10);
    }
    @Test
    public void test2()
    {
        PolarTransform pt = new PolarTransform(true);
        Point2D p1 = new Point2D.Double(A270, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(-1, 0);
        pt.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-10);
        assertEquals(exp.getY(), p2.getY(), 1e-10);
        DoubleTransform inverse = pt.inverse();
        inverse.transform(p2, exp);
        assertEquals(exp.getX(), p1.getX(), 1e-10);
        assertEquals(exp.getY(), p1.getY(), 1e-10);
    }
    @Test
    public void test3()
    {
        PolarTransform pt = new PolarTransform(true);
        Point2D p1 = new Point2D.Double(A90, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(1, 0);
        pt.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-10);
        assertEquals(exp.getY(), p2.getY(), 1e-10);
        DoubleTransform inverse = pt.inverse();
        inverse.transform(p2, exp);
        assertEquals(exp.getX(), p1.getX(), 1e-10);
        assertEquals(exp.getY(), p1.getY(), 1e-10);
    }
    @Test
    public void test4()
    {
        PolarTransform pt = new PolarTransform(true);
        Point2D p1 = new Point2D.Double(A180, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(0, -1);
        pt.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-10);
        assertEquals(exp.getY(), p2.getY(), 1e-10);
        DoubleTransform inverse = pt.inverse();
        inverse.transform(p2, exp);
        assertEquals(exp.getX(), p1.getX(), 1e-10);
        assertEquals(exp.getY(), p1.getY(), 1e-10);
    }
    @Test
    public void testGradientGrad()
    {
        PolarTransform pt = new PolarTransform(true);
        DoubleBinaryMatrix Jn = MoreMath.gradient(pt);
        DoubleBinaryMatrix J = pt.gradient();
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<2;j++)
            {
                assertEquals(Jn.eval(i, j, A0, 1), J.eval(i, j, A0, 1), 1e-8);
                assertEquals(Jn.eval(i, j, A90, 1), J.eval(i, j, A90, 1), 1e-8);
                assertEquals(Jn.eval(i, j, A180, 1), J.eval(i, j, A180, 1), 1e-8);
                assertEquals(Jn.eval(i, j, A270, 1), J.eval(i, j, A270, 1), 1e-8);
            }
        }
    }
    @Test
    public void testGradientDeg()
    {
        PolarTransform pt = new PolarTransform(false);
        DoubleBinaryMatrix Jn = MoreMath.gradient(pt);
        DoubleBinaryMatrix J = pt.gradient();
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<2;j++)
            {
                assertEquals(Jn.eval(i, j, 0, 2), J.eval(i, j, 0, 2), 1e-8);
                assertEquals(Jn.eval(i, j, 90, 2), J.eval(i, j, 90, 2), 1e-8);
                assertEquals(Jn.eval(i, j, 180, 2), J.eval(i, j, 180, 2), 1e-8);
                assertEquals(Jn.eval(i, j, 270, 2), J.eval(i, j, 270, 2), 1e-8);
            }
        }
    }
    @Test
    public void testGradient2()
    {
        PolarTransform pt = new PolarTransform(false);
        DoubleBinaryMatrix J = pt.gradient();
        double d1 = J.determinant().applyAsDouble(0, 1);
        DoubleBinaryMatrix m = DoubleBinaryMatrix.getInstance(2, 100, 0, 0, -100);
        double d2 = m.determinant().applyAsDouble(0, 1);
        DoubleBinaryMatrix mm = m.multiply(J);
        double d3 = mm.determinant().applyAsDouble(0, 1);
    }
}
