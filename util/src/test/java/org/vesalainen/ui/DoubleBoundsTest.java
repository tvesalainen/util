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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleBoundsTest
{
    
    public DoubleBoundsTest()
    {
    }

    @Test
    public void test0()
    {
        DoubleBounds db = new DoubleBounds();
        assertEquals(-Double.MAX_VALUE/2, db.getMaxX(), 1e-10);
        assertEquals(-Double.MAX_VALUE/2, db.getMaxY(), 1e-10);
        assertEquals(-Double.MAX_VALUE, db.width, 1e-10);
        assertEquals(-Double.MAX_VALUE, db.height, 1e-10);
        assertTrue(db.isEmpty());
    }
    @Test
    public void test1()
    {
        DoubleBounds db = new DoubleBounds();
        db.add(2, 2);
        assertEquals(0, db.height, 1e-10);
        assertEquals(0, db.width, 1e-10);
        assertEquals(2, db.x, 1e-10);
        assertEquals(2, db.y, 1e-10);
    }    
}
