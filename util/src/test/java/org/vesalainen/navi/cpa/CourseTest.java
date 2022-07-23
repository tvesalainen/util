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
package org.vesalainen.navi.cpa;

import static java.util.concurrent.TimeUnit.HOURS;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.PolynomialExpressionBuilder;
import org.vesalainen.math.PolynomialExpressionBuilder.Polynom;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CourseTest
{
    
    public CourseTest()
    {
    }

    @Test
    public void testGenerate()
    {
        PolynomialExpressionBuilder b = new PolynomialExpressionBuilder("t", "x");
        Polynom x1 = b.create("c1.getLatitude()", "c1.deltaLatitude()");
        Polynom y1 = b.create("c1.getLongitude()", "c1.deltaLongitude()");
        Polynom x2 = b.create("c2.getLatitude()", "c2.deltaLatitude()");
        Polynom y2 = b.create("c2.getLongitude()", "c2.deltaLongitude()");
        Polynom dx = b.minus(x1, x2);
        Polynom dy = b.minus(y1, y2);
        Polynom dx2 = b.mul(dx, dx);
        Polynom dy2 = b.mul(dy, dy);
        Polynom plus = b.plus(dx2, dy2);
        Polynom der = b.derivative(plus);
        System.err.println(b.subVars("double"));
        System.err.println("double sum = "+plus+";");
        System.err.println("double der = "+der+";");
    }
    @Test
    public void testCPA1()
    {
        SimpleCourse c1 = new SimpleCourse(-1, 0, 1, 0);
        assertEquals(0, c1.getLatitudeAt(HOURS.toMillis(60)), 1e-10);
        SimpleCourse c2 = new SimpleCourse(0, -1, 1, 90);
        assertEquals(0, c2.getLongitudeAt(HOURS.toMillis(60)), 1e-10);
        long time = c1.getCPATime(c2);
        assertEquals(HOURS.toMillis(60), time);
        double distance = c1.getCPADistance(c2);
        assertEquals(0, distance, 1e-10);
    }
    @Test
    public void testCPA2()
    {
        SimpleCourse c1 = new SimpleCourse(-1, 0, 1, 0);
        assertEquals(0, c1.getLatitudeAt(HOURS.toMillis(60)), 1e-10);
        SimpleCourse c2 = new SimpleCourse(0, -1, 2, 90);
        assertEquals(0, c2.getLongitudeAt(HOURS.toMillis(30)), 1e-10);
        long time = c1.getCPATime(c2);
        //assertEquals(HOURS.toMillis(60), time);
        double distance = c1.getCPADistance(c2);
        //assertEquals(0, distance, 1e-10);
    }
    @Test
    public void testNorth()
    {
        SimpleCourse c = new SimpleCourse(0, 0, 1, 0);
        assertEquals(1, c.getLatitudeAt(HOURS.toMillis(60)), 1e-10);
        assertEquals(0, c.getLongitudeAt(HOURS.toMillis(60)), 1e-10);
    }
    @Test
    public void testEast()
    {
        SimpleCourse c = new SimpleCourse(0, 0, 1, 90);
        assertEquals(0, c.getLatitudeAt(HOURS.toMillis(60)), 1e-10);
        assertEquals(1, c.getLongitudeAt(HOURS.toMillis(60)), 1e-10);
    }
    
}
