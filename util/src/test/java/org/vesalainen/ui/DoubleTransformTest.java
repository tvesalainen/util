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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTransformTest
{
    
    public DoubleTransformTest()
    {
    }

    @Test
    public void test1()
    {
        DoubleTransform triplex = (x,y,n)->{System.err.printf("%f %f\n", x,y);n.accept(3*x, y);};
        DoubleTransform tripley = (x,y,n)->{System.err.printf("%f %f\n", x,y);n.accept(x, 3*y);};
        DoubleTransform andThen = triplex.andThen(tripley);
        Point2D.Double exp = new Point2D.Double(3, 3);
        Point2D.Double got = new Point2D.Double();
        andThen.transform(1, 1, (x,y)->{got.setLocation(x, y);System.err.printf("%f %f\n", x,y);});
        assertEquals(exp, got);
    }
    @Test
    public void test2()
    {
        double[] dst = new double[4];
        DoubleTransform.swap().transform(null, new double[]{1, 2, 3, 4}, dst, 2);
        assertEquals(2, dst[0], 1e-10);
        assertEquals(1, dst[1], 1e-10);
        assertEquals(4, dst[2], 1e-10);
        assertEquals(3, dst[3], 1e-10);
    }    
    @Test
    public void test3()
    {
        DoubleTransform t = (x,y,c)->c.accept(2*x, y);
        DoubleTransform derivate = t.derivate();
        Point2D.Double exp = new Point2D.Double(2, 1);
        Point2D.Double got = new Point2D.Double();
        derivate.transform(2, 3, got::setLocation);
        assertEquals(exp.x, got.x, 1e-10);
        assertEquals(exp.y, got.y, 1e-10);
    }
    @Test
    public void testSwapDerivate()
    {
        DoubleTransform derivate = DoubleTransform.swap().derivate();
        Point2D.Double exp = new Point2D.Double(1, 1);
        Point2D.Double got = new Point2D.Double();
        derivate.transform(2, 3, got::setLocation);
        assertEquals(exp, got);
    }
    @Test
    public void testAndTheMultiply()
    {
        DoubleTransform t1 = (x,y,c)->c.accept(2*x, 3*y);
        DoubleTransform t2 = (x,y,c)->c.accept(4*x, 5*y);
        DoubleTransform t3 = t1.andThenMultiply(t2);
        Point2D.Double exp = new Point2D.Double(8, 15);
        Point2D.Double got = new Point2D.Double();
        t3.transform(1, 1, got::setLocation);
        assertEquals(exp, got);
    }
    @Test
    public void test() throws NoninvertibleTransformException
    {
        DoubleTransform affineTransform = Transforms.affineTransform(new AffineTransform(1, 2, 3, 4, 5, 6));
        double m = 1000000;
        double m2 = m/2.0;
        Random r = new Random(1234567L);
        for (int ii=0;ii<1000;ii++)
        {
            double r1 = r.nextDouble();
            double r2 = r.nextDouble();
            double x = r1*m-m2;
            double y = r2*m-m2;
            test(DoubleTransform.identity(), x, y);
            test(DoubleTransform.swap(), x, y);
            test(affineTransform, x, y);
        }
    }
    public void test(DoubleTransform transform, double x, double y)
    {
        testDerivate(transform, x, y);
        testInverse(transform, x, y);
    }
    private void testInverse(DoubleTransform transform, double x, double y)
    {
        DoubleTransform inverse = transform.inverse();
        Point2D.Double tr = new Point2D.Double();
        transform.transform(x, y, tr::setLocation);
        Point2D.Double in = new Point2D.Double();
        inverse.transform(tr.x, tr.y, in::setLocation);
        assertEquals(x, in.x, 1e-10);
        assertEquals(y, in.y, 1e-10);
    }
    private void testDerivate(DoubleTransform transform, double x, double y)
    {
        DoubleTransform derivate = transform.derivate();
        Point2D.Double exp = new Point2D.Double();
        derivate.transform(x, y, exp::setLocation);
        Point2D.Double p1 = new Point2D.Double();
        Point2D.Double p2 = new Point2D.Double();
        transform.transform(x, y, p1::setLocation);
        double dx = Math.ulp(x);
        double dy = Math.ulp(y);
        transform.transform(x+dx, y+dy, p2::setLocation);
        assertEquals(exp.x, (p2.x-p1.x)/dx, 1e-10);
        assertEquals(exp.y, (p2.y-p1.y)/dy, 1e-10);
    }
    private void test(DoubleTransform transform)
    {
        double d = Math.ulp(1.0);
        for (double ii=1.0;ii<400;ii++)
        {
            d *= 10.0;
            double der = ((1.0+d)-1.0)/d;
            System.err.println(ii+": "+d+" "+der);
        }
    }
}
