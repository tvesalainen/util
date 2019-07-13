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
package org.vesalainen.code;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class InterfaceDispatcherTest
{
    
    public InterfaceDispatcherTest()
    {
    }

    @Test
    public void test1()
    {
        FS fs = FS.newInstance();
        assertFalse(fs.hasObservers());
        fs.setB((byte)1);
        fs.setString("kukkuu");
        S s1 = new S("string", "b", "c", "d");
        S s2 = new S("string", "b", "c", "d");
        fs.addObserver(s1);
        assertTrue(fs.hasObservers());
        fs.addObserver(s2);
        fs.setB((byte)1);
        fs.setString("kukkuu");
        fs.removeObserver(s2);
        fs.setB((byte)1);
        fs.removeObserver(s1);
        fs.setB((byte)1);
        assertFalse(fs.hasObservers());
    }
    @Test
    public void testannotatedPropertyStore()
    {
        FS fs = FS.newInstance();
        APS2 aps = new APS2();
        fs.addObserver(aps);
        fs.start("");
        fs.setB((byte)1);
        fs.setString("kukkuu");
        fs.commit("");
        assertEquals((byte)1, aps.getByte("b"));
        assertEquals("kukkuu", aps.getObject("string"));
        fs.start("");
        fs.setB((byte)2);
        fs.setString("nukkuu");
        fs.rollback("");
        assertEquals((byte)1, aps.getByte("b"));
        assertEquals("kukkuu", aps.getObject("string"));
        fs.start("");
        fs.setB((byte)2);
        fs.rollback("");
        assertEquals((byte)1, aps.getByte("b"));
        fs.start("");
        fs.setB((byte)3);
        fs.commit("");
        assertEquals((byte)3, aps.getByte("b"));
        fs.removeObserver(aps);
        fs.start("");
        fs.setB((byte)4);
        fs.commit("");
        assertEquals((byte)3, aps.getByte("b"));
    }
    @Test
    public void testTransactions()
    {
        FS fs = FS.newInstance();
        S s1 = new S("string", "b", "c", "d");
        S s2 = new S("string", "j");
        fs.addObserver(s1);
        fs.addObserver(s2);
        fs.start("");
        fs.setB((byte)1);
        fs.commit("");
        assertEquals((byte)1, s1.get("b"));
        assertEquals(null, s2.get("b"));
        fs.start("");
        fs.setB((byte)2);
        fs.rollback("");
        assertEquals((byte)1, s1.get("b"));
        fs.start("");
        fs.setB((byte)2);
        fs.rollback("");
        assertEquals((byte)1, s1.get("b"));
        fs.start("");
        fs.setB((byte)3);
        fs.commit("");
        assertEquals((byte)3, s1.get("b"));
        fs.start("");
        fs.setJ(123456L);
        fs.commit("");
    }
    private static class S extends AbstractPropertySetter
    {
        private String[] prefixes;
        private Map<String,Object> map = new HashMap<>();

        public S(String... prefixes)
        {
            this.prefixes = prefixes;
        }
        
        @Override
        public String[] getPrefixes()
        {
            return prefixes;
        }

        @Override
        public void setProperty(String property, Object arg)
        {
            if (arg instanceof Long)
            {
                throw new IllegalArgumentException("long not ok");
            }
            map.put(property, arg);
        }
        public Object get(String property)
        {
            return map.get(property);
        }
    }
}
