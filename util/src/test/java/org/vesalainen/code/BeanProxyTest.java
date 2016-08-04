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
public class BeanProxyTest
{
    
    public BeanProxyTest()
    {
    }

    /**
     * Test of getInstance method, of class BeanProxy.
     */
    @Test
    public void testGetInstance()
    {
        System.err.println("BeanProxy");
        try
        {
            BT bt = BT.getInstance(BT.class);
            assertNotNull(bt);
            bt.setZ(true);
            assertTrue(bt.getZ());
            bt.setB((byte)123);
            assertEquals((byte)123, bt.getB());
            bt.setC('w');
            assertEquals('w', bt.getC());
            bt.setS((short)12345);
            assertEquals((short)12345, bt.getS());
            bt.setI(123456);
            assertEquals(123456, bt.getI());
            bt.setJ(1234567);
            assertEquals(1234567, bt.getL());
            bt.setF(1234567.89F);
            assertEquals(1234567.89F, bt.getF(), 0.0000000001);
            bt.setD(12345678.9);
            assertEquals(12345678.9, bt.getD(), 0.0000000001);
            bt.setString("qwerty");
            assertEquals("qwerty", bt.getString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
