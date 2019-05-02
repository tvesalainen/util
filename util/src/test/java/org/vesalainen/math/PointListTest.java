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

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PointListTest
{
    
    public PointListTest()
    {
    }

    @Test
    public void testIndexOf()
    {
        PointList pl = new PointList();
        Point2D.Double p1 = new Point2D.Double(1,2);
        Point2D.Double p2 = new Point2D.Double(3,4);
        Point2D.Double p3 = new Point2D.Double(5,6);
        Point2D.Double p4 = new Point2D.Double(7,8);
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);
        pl.add(p4);
        assertEquals(0, pl.indexOf(p1));
        assertEquals(3, pl.indexOf(p4));
        assertEquals(2, pl.indexOf(1, 5.5, 5.5, 0.5, 0.5));
        assertEquals(2, pl.indexOf(1, Double.NaN, 5.5, 0.5, 0.5));
        assertEquals(2, pl.indexOf(2, Double.NaN, Double.NaN, 0.5, 0.5));
    }
    @Test
    public void testSet()
    {
        PointList pl = new PointList(2);
        Point2D.Double p1 = new Point2D.Double(1,2);
        Point2D.Double p2 = new Point2D.Double(3,4);
        Point2D.Double p3 = new Point2D.Double(5,6);
        Point2D.Double p4 = new Point2D.Double(7,8);
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);
        pl.set(1, p4);
        assertEquals(p4, pl.get(1));
    }
    @Test
    public void test1()
    {
        PointList pl = new PointList(2);
        Point2D.Double p1 = new Point2D.Double(1,2);
        Point2D.Double p2 = new Point2D.Double(3,4);
        Point2D.Double p3 = new Point2D.Double(5,6);
        Point2D.Double p4 = new Point2D.Double(7,8);
        
        pl.add(p1);
        assertEquals(1, pl.size());
        assertEquals(p1, pl.get(0));
        
        pl.add(p2);
        assertEquals(2, pl.size());
        assertEquals(p2, pl.get(1));
        
        pl.add(p3);
        assertEquals(3, pl.size());
        assertEquals(p3, pl.get(2));
        
        pl.add(1, p4);
        assertEquals(4, pl.size());
        assertEquals(p4, pl.get(1));
        assertEquals(p2, pl.get(2));
        assertEquals(p3, pl.get(3));
        
        double[] array = pl.array();
        assertEquals(8, array.length);
        
        pl.remove(0);
        assertEquals(3, pl.size());
        assertEquals(p4, pl.get(0));
        assertEquals(p2, pl.get(1));
        assertEquals(p3, pl.get(2));
        
        pl.remove(2);
        assertEquals(2, pl.size());
        assertEquals(p4, pl.get(0));
        assertEquals(p2, pl.get(1));
    }
    @Test
    public void testSort()
    {
        PointList pl = new PointList(2);
        Point2D.Double p1 = new Point2D.Double(1,2);
        Point2D.Double p2 = new Point2D.Double(3,4);
        Point2D.Double p3 = new Point2D.Double(5,6);
        Point2D.Double p4 = new Point2D.Double(7,8);
        pl.add(p4);
        pl.add(p3);
        pl.add(p2);
        pl.add(p1);
        pl.sort();
        assertEquals(p1, pl.get(0));
        assertEquals(p2, pl.get(1));
        assertEquals(p3, pl.get(2));
        assertEquals(p4, pl.get(3));
    }
}
