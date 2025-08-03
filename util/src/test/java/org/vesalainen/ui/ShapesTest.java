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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ShapesTest
{
    
    public ShapesTest()
    {
    }

    @Test
    public void testScaleInPlace()
    {
        PlusShape ps = new PlusShape();
        Shape sip = Shapes.scaleInPlace(ps, 0.3, 0.5);
        Rectangle2D b1 = ps.getBounds2D();
        Rectangle2D b2 = sip.getBounds2D();
        assertEquals(b1.getCenterX(), b2.getCenterX(), 1e-10);
        assertEquals(b1.getCenterY(), b2.getCenterY(), 1e-10);
    }
    
}
