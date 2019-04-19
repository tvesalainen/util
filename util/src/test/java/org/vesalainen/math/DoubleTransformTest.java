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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Random;
import org.junit.Test;
import org.vesalainen.ui.Transforms;
import static org.junit.Assert.*;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;

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
    public void testChain() throws NoninvertibleTransformException
    {
        AffineTransform at = new AffineTransform(1, 2, 3, 4, 5, 6);
        AffineTransform rot = AffineTransform.getRotateInstance(1.2);
        at.concatenate(rot);
        DoubleTransform t1 = Transforms.affineTransform(at);
        DoubleTransform t2 = Transforms.affineTransform(rot);
        DoubleTransform chain = DoubleTransform.chain(t2, t1);
        DoubleTransform inverse = chain.inverse();
        AffineTransform at1nv = at.createInverse();
        Point2D.Double p1 = new Point2D.Double(2, 3);
        Point2D.Double p2 = new Point2D.Double();
        assertEquals(at.transform(p1, p2), chain.transform(p1, p2));
        assertEquals(at1nv.transform(p1, p2), inverse.transform(p1, p2));
    }
    @Test
    public void test() throws NoninvertibleTransformException
    {
        DoubleTransform affineTransform = Transforms.affineTransform(new AffineTransform(1, 2, 3, 4, 5, 6));
        AffineTransform rot = AffineTransform.getRotateInstance(1.2);
        DoubleTransform t2 = Transforms.affineTransform(rot);
        DoubleTransform chain = DoubleTransform.chain(t2, affineTransform);
        DoubleTransform inverse = chain.inverse();
        double m = 1000000;
        double m2 = m/2.0;
        Random r = new Random(1234567L);
        for (int ii=0;ii<1000;ii++)
        {
            double r1 = r.nextDouble();
            double r2 = r.nextDouble();
            double x = r1*m-m2;
            double y = r2*m-m2;
            test(chain, x, y);
            test(DoubleTransform.identity(), x, y);
            test(DoubleTransform.swap(), x, y);
            test(affineTransform, x, y);
        }
    }
    public void test(DoubleTransform transform, double x, double y)
    {
        testGradient(transform, x, y);
        testInverse(transform, x, y);
    }
    private void testInverse(DoubleTransform transform, double x, double y)
    {
        DoubleTransform inverse = transform.inverse();
        Point2D.Double tr = new Point2D.Double();
        transform.transform(x, y, tr::setLocation);
        Point2D.Double in = new Point2D.Double();
        inverse.transform(tr.x, tr.y, in::setLocation);
        assertEquals(x, in.x, 10000*Math.ulp(x));
        assertEquals(y, in.y, 10000*Math.ulp(y));
    }
    private void testGradient(DoubleTransform transform, double x, double y)
    {
        DoubleBinaryMatrix J = transform.gradient();
        DoubleBinaryMatrix Jn = MoreMath.gradient(transform);
        for (int i=0;i<2;i++)
        {
            for (int j=0;j<2;j++)
            {
                assertEquals(Jn.eval(i, j, x, y), J.eval(i, j, x, y), 1e-4);
            }
        }
    }
}
