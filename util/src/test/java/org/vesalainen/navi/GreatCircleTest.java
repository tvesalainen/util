/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GreatCircleTest
{
    private static final double Epsilon = 3e-3;
    
    public GreatCircleTest()
    {
    }

    @Test
    public void testDistance()
    {
        assertEquals(216.0205897735579, GreatCircle.distance(50.1, -005.42, 53.38, -003.03), Epsilon);
        assertEquals(120, GreatCircle.distance(0, 25, 0, 27), Epsilon*30);
        assertEquals(30, GreatCircle.distance(60, 25, 60, 24), Epsilon*30);
        assertEquals(60, GreatCircle.distance(60, 179, 60, -179), Epsilon*30);
        assertEquals(60, GreatCircle.distance(60, -179, 60, 179), Epsilon*30);
    }
    
    @Test
    public void testBearing()
    {
        assertEquals(23.35256231948781, GreatCircle.initialBearing(50.1, -005.42, 53.38, -003.03), Epsilon);
        assertEquals(90, GreatCircle.initialBearing(0, 179, 0, -179), Epsilon);
        assertEquals(270, GreatCircle.initialBearing(0, -179, 0, 179), Epsilon);
        assertEquals(180, GreatCircle.initialBearing(60, 25, 59, 25), Epsilon);
    }
    @Test
    public void testPrecision()
    {
        double distance = METER.convertTo(1, NAUTICAL_DEGREE);
        double coef = Math.sqrt(0.5);
        for (int ii=0;ii<24;ii++)
        {
            double delta = distance*coef;
            double exp = NAUTICAL_DEGREE.convertTo(distance, METER);
            double d1 = Navis.distance(0, 0, delta, delta);
            double g1 = NAUTICAL_MILE.convertTo(d1, METER);
            double d2 = GreatCircle.distance(0, 0, delta, delta);
            double g2 = NAUTICAL_MILE.convertTo(d2, METER);
            System.err.print(exp);
            System.err.print(", ");
            System.err.print(deltaPercent(exp, g1));
            System.err.print(", ");
            System.err.print(deltaPercent(exp, g2));
            System.err.println();
            distance *= 2;
        }
    }
    private double deltaPercent(double exp, double got)
    {
        return 100.0*(exp-got)/exp;
    }
}
