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
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoublePolygonTest
{

    private double Epsilon = 1e-10;
    
    public DoublePolygonTest()
    {
    }

    @Test
    public void test0()
    {
        DoublePolygon p = new DoublePolygon();
        p.add(0, 0);
        p.add(0, 1);
        p.add(1, 0);
        assertTrue(p.contains(0.4, 0.4));
        assertFalse(p.contains(0.5, 0.5));
        assertTrue(p.getBounds2D().contains(0.5, 0.5));
    }
    @Test
    public void testClose()
    {
        test(true, null);
    }
    @Test
    public void testTransform()
    {
        test(false, AffineTransform.getTranslateInstance(0, 0));
    }
    public void test(boolean close, AffineTransform at)
    {
        DoublePolygon p = new DoublePolygon();
        p.add(0, 0);
        p.add(0, 1);
        p.add(1, 0);
        if (close)
        {
            p.closePath();
        }
        PathIterator pi = p.getPathIterator(at);
        double[] d = new double[6];
        
        assertFalse(pi.isDone());
        assertEquals(SEG_MOVETO, pi.currentSegment(d));
        assertEquals(0.0, d[0], Epsilon);
        assertEquals(0.0, d[1], Epsilon);

        pi.next();
        assertFalse(pi.isDone());
        assertEquals(SEG_LINETO, pi.currentSegment(d));
        assertEquals(0.0, d[0], Epsilon);
        assertEquals(1.0, d[1], Epsilon);

        pi.next();
        assertFalse(pi.isDone());
        assertEquals(SEG_LINETO, pi.currentSegment(d));
        assertEquals(1.0, d[0], Epsilon);
        assertEquals(0.0, d[1], Epsilon);

        if (close)
        {
            pi.next();
            assertFalse(pi.isDone());
            assertEquals(SEG_CLOSE, pi.currentSegment(d));
        }
        
        pi.next();
        assertTrue(pi.isDone());
    }
    
}
