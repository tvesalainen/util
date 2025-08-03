/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import javafx.scene.paint.Color;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class InterpolatingColorTest
{
    
    public InterpolatingColorTest()
    {
    }

    @Test
    public void test1()
    {
        InterpolatingColor windColor = new InterpolatingColor(0, 180, 10, 50, 20, -40, 40, -80);
        double e = 1e-5;
        Color c1 = windColor.color(0);
        assertEquals(180, c1.getHue(), e);
        assertEquals(0.1, c1.getSaturation(), e);

        Color c2 = windColor.color(10);
        assertEquals(50, c2.getHue(), e);
        assertEquals(0.324999, c2.getSaturation(), e);

        Color c3 = windColor.color(20);
        assertEquals(320, c3.getHue(), e);
        assertEquals(0.55, c3.getSaturation(), e);

        Color c4 = windColor.color(40);
        assertEquals(280, c4.getHue(), e);
        assertEquals(1.0, c4.getSaturation(), e);
    }
    
}
