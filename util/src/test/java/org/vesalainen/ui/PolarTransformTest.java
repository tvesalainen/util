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
    public void testDerivative1()
    {
        PolarTransform pt = new PolarTransform(true);
        DoubleTransform derivative = pt.derivative();
        Point2D p1 = new Point2D.Double(A0, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(1, 1);
        derivative.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-8);
        assertEquals(exp.getY(), p2.getY(), 1e-8);
    }
    @Test
    public void testDerivative2()
    {
        PolarTransform pt = new PolarTransform(true);
        DoubleTransform derivative = pt.derivative();
        Point2D p1 = new Point2D.Double(A90, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(1, -1);
        derivative.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-8);
        assertEquals(exp.getY(), p2.getY(), 1e-8);
    }
    @Test
    public void testDerivative3()
    {
        PolarTransform pt = new PolarTransform(true);
        DoubleTransform derivative = pt.derivative();
        Point2D p1 = new Point2D.Double(A180, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(-1, -1);
        derivative.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-8);
        assertEquals(exp.getY(), p2.getY(), 1e-8);
    }
    @Test
    public void testDerivative4()
    {
        PolarTransform pt = new PolarTransform(true);
        DoubleTransform derivative = pt.derivative();
        Point2D p1 = new Point2D.Double(A270, 1);
        Point2D p2 = new Point2D.Double();
        Point2D exp = new Point2D.Double(-1, 1);
        derivative.transform(p1, p2);
        assertEquals(exp.getX(), p2.getX(), 1e-8);
        assertEquals(exp.getY(), p2.getY(), 1e-7);
    }
    
}
