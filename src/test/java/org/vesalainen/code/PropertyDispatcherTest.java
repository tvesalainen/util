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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class PropertyDispatcherTest
{
    
    public PropertyDispatcherTest()
    {
    }

    /**
     * Test of getInstance method, of class PropertyDispatcher.
     */
    @Test
    public void testGetInstance()
    {
        PS ps = new PS();
        PD pd = PD.getInstance(PD.class);
        assertNotNull(pd);
        
        pd.addObserver(ps, "string", "i");
        pd.setI(123);
        assertEquals(null, ps.get("i"));
        pd.setString("qwerty");
        assertNull(ps.get("string"));
        pd.commit(null);
        assertEquals(123, ps.get("i"));
        assertEquals("qwerty", ps.get("string"));
        pd.setI(456);
        pd.setString("asdfgh");
        assertEquals(123, ps.get("i"));
        assertEquals("qwerty", ps.get("string"));
        pd.rollback(null);
        assertEquals(123, ps.get("i"));
        assertEquals("qwerty", ps.get("string"));
    }

    public class PS implements PropertySetter
    {
        private Map<String,Object> map = new HashMap<>();

        public Object get(String property)
        {
            return map.get(property);
        }
        @Override
        public void set(String property, boolean arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, byte arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, char arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, short arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, int arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, long arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, float arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, double arg)
        {
            map.put(property, arg);
        }

        @Override
        public void set(String property, Object arg)
        {
            map.put(property, arg);
        }
        
    }
}
