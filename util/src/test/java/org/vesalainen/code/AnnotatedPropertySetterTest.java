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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPropertySetterTest
{
    
    public AnnotatedPropertySetterTest()
    {
    }

    @Test
    public void test1()
    {
        APS aps = new APS();
        
        String[] prefixes = aps.getPrefixes();
        assertEquals(10, prefixes.length);
        
        assertTrue(ArrayHelp.contains(prefixes, "string"));
        assertTrue(ArrayHelp.contains(prefixes, "boolean"));
        assertTrue(ArrayHelp.contains(prefixes, "byte"));
        assertTrue(ArrayHelp.contains(prefixes, "char"));
        assertTrue(ArrayHelp.contains(prefixes, "short"));
        assertTrue(ArrayHelp.contains(prefixes, "long"));
        assertTrue(ArrayHelp.contains(prefixes, "double"));
        assertTrue(ArrayHelp.contains(prefixes, "foo"));
        assertTrue(ArrayHelp.contains(prefixes, "bar"));
        assertTrue(ArrayHelp.contains(prefixes, "goo"));
        
        aps.set("string", "ääkkönen");
        assertEquals("ääkkönen", aps.s);
        
        aps.set("boolean", true);
        assertTrue(aps.b);
        
        aps.set("byte", (byte)123);
        assertEquals(123, aps.by);
        
        aps.set("char", 'ö');
        assertEquals('ö', aps.cc);
        
        aps.set("short", (short)12);
        assertEquals(12, aps.sh);
        
        aps.set("long", 12345L);
        assertEquals(12345L, aps.ll);
        
        aps.set("double", 123.456);
        assertEquals(123.456, aps.db, 1e-10);
        
        aps.set("foo", 123.456F);
        assertEquals(123.456F, aps.foo, 1e-10);
        
        aps.set("bar", 123.456F);
        assertEquals(123.456F, aps.ba, 1e-10);
        
        
        aps.set("goo", 123);
        assertEquals(123, aps.i);
        
    }
    
    class APS extends AnnotatedPropertySetter
    {
        @Property("string")
        String s;
        @Property("boolean")
        boolean b;
        @Property("byte")
        byte by;
        @Property("char")
        char cc;
        @Property("short")
        short sh;
        @Property("long")
        long ll;
        @Property("double")
        double db;
        @Property
        float foo;
        @Property("bar")
        float ba;
        private int i;
        
        @Property
        public void setGoo(int i)
        {
            this.i = i;
        }
        @Override
        public void start(String reason)
        {
        }

        @Override
        public void rollback(String reason)
        {
        }

        @Override
        public void commit(String reason)
        {
        }
        
    }
}
