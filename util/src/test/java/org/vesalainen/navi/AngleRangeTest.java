/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleRangeTest
{
    
    public AngleRangeTest()
    {
    }

    @Test
    public void test1()
    {
        AngleRange cs = new AngleRange();
        
        assertTrue(cs.add(350));
        assertEquals(0, cs.getRange(), 1e-10);
        assertEquals(350, cs.getLeft(), 1e-10);
        assertEquals(350, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(0));
        assertEquals(10, cs.getRange(), 1e-10);
        assertEquals(350, cs.getLeft(), 1e-10);
        assertEquals(0, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(30));
        assertEquals(40, cs.getRange(), 1e-10);
        assertEquals(350, cs.getLeft(), 1e-10);
        assertEquals(30, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(180));
        assertEquals(190, cs.getRange(), 1e-10);
        assertEquals(350, cs.getLeft(), 1e-10);
        assertEquals(180, cs.getRight(), 1e-10);

        assertFalse(cs.add(90));
        
        cs.reset();
        assertEquals(0, cs.getRange(), 1e-10);
    }
    @Test
    public void test2()
    {
        AngleRange cs = new AngleRange();
        
        assertTrue(cs.add(180));
        assertEquals(0, cs.getRange(), 1e-10);
        assertEquals(180, cs.getLeft(), 1e-10);
        assertEquals(180, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(90));
        assertEquals(90, cs.getRange(), 1e-10);
        assertEquals(90, cs.getLeft(), 1e-10);
        assertEquals(180, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(270));
        assertEquals(180, cs.getRange(), 1e-10);
        assertEquals(90, cs.getLeft(), 1e-10);
        assertEquals(270, cs.getRight(), 1e-10);
        
        assertTrue(cs.add(0));
        assertEquals(270, cs.getRange(), 1e-10);
        assertEquals(90, cs.getLeft(), 1e-10);
        assertEquals(0, cs.getRight(), 1e-10);
    }
    
}
