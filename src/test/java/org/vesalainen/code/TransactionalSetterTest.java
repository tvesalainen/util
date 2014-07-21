/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.code;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class TransactionalSetterTest
{
    
    public TransactionalSetterTest()
    {
    }

    /**
     * Test of getInstance method, of class TransactionalSetter.
     */
    @Test
    public void testGetInstance()
    {
        TS ts = TS.getInstance(TS.class);
        assertNotNull(ts);
        
        ts.setI(123);
        assertEquals(0, ts.getI());
        ts.setString("qwerty");
        assertNull(ts.getString());
        ts.commit(null);
        assertEquals(123, ts.getI());
        assertEquals("qwerty", ts.getString());
        ts.setI(456);
        ts.setString("asdfgh");
        assertEquals(123, ts.getI());
        assertEquals("qwerty", ts.getString());
        ts.rollback(null);
        assertEquals(123, ts.getI());
        assertEquals("qwerty", ts.getString());
    }
    
}
