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
    //@Test
    public void testGetInstance()
    {
        System.err.println("BeanProxy");
        try
        {
            BT instance = BT.getInstance(BT.class);
            assertNotNull(instance);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    @BeanProxyClass("org.vesalainen.code.BTImpl")
    public abstract class BT extends BeanProxy implements Intf
    {
        
    }
    public interface Intf
    {
        void setXYZ(int x);
        void setString(String s);
    }
}
