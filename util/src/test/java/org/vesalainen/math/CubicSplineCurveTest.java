/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CubicSplineCurveTest
{
    static final double Epsilon = 1e-4;
    
    public CubicSplineCurveTest()
    {
    }

    @Test
    public void test1()
    {
        List<Point> list = null;
        for (double k=-5;k<=5;k++)
        {
            list = new ArrayList<Point>();
            for (double x=-10;x<=10;x++)
            {
                list.add(new AbstractPoint(x,k*x));
            }
            CubicSplineCurve csc = new CubicSplineCurve(list);
            for (double x=-10;x<=10;x+=0.1)
            {
                Point expected = new AbstractPoint(x,k*x);
                Point tested = csc.getNearest(x, Epsilon/10);
                assertEquals(expected.getX(), tested.getX(), Epsilon);
                assertEquals(expected.getY(), tested.getY(), Epsilon);
            }
            double avg = csc.average(-10, 10, Epsilon);
            assertFalse(Math.abs(avg) > Epsilon);
            avg = csc.average(-5, 5, Epsilon);
            assertFalse(Math.abs(avg) > Epsilon);
            avg = csc.average(Epsilon);
            assertFalse(Math.abs(avg) > Epsilon);
        }
    }
    
    @Test
    public void test2()
    {
        List<Point> list = new ArrayList<Point>();
        for (double x=-10;x<=10;x++)
        {
            list.add(new AbstractPoint(x,x*x));
        }
        CubicSplineCurve csc = new CubicSplineCurve(list);
        for (double x=-9;x<=9;x+=0.1)
        {
            Point expected = new AbstractPoint(x,x*x);
            Point tested = csc.getNearest(x, Epsilon);
            assertEquals(expected.getX(), tested.getX(), 0.1);
            assertEquals(expected.getY(), tested.getY(), 0.1);
        }
    }
    
    @Test
    public void test3()
    {
        try
        {
            Plotter p = new Plotter(1000, 1000);
            CubicSplineCurve csc = new CubicSplineCurve(0, 1, 1, 3, 2, 2, 3, 4, 4, 3);
            Iterator<Point> iterator = csc.iterator(0.1);
            while (iterator.hasNext())
            {
                Point t = iterator.next();
                System.err.println(t);
                p.drawPoint(t);
            }
            p.drawCoordinates();
            p.margin(0.01);
            p.plotToDocFiles(CubicSplineCurve.class, "test3", "png");
        }
        catch (IOException ex)
        {
            Logger.getLogger(CubicSplineCurveTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
