/*
 * Copyright (C) 2016 tkv
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

/**
 *
 * @author tkv
 */
public class GreatCircleTest
{
    private static final double Epsilon = 1e-10;
    
    public GreatCircleTest()
    {
    }

    @Test
    public void test()
    {
        assertEquals(216.0205897735579, GreatCircle.distance(50.1, -005.42, 53.38, -003.03), Epsilon);
        assertEquals(23.35256231948781, GreatCircle.initialBearing(50.1, -005.42, 53.38, -003.03), Epsilon);
    }
    
}
