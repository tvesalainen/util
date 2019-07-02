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

import java.lang.invoke.MethodHandle;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;
import java.util.Objects;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.ArrayHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPropertyStoreTest
{

    public AnnotatedPropertyStoreTest()
    {
    }

    @Test
    public void test0() throws NoSuchMethodException, IllegalAccessException, Throwable
    {
        MethodHandle cat = lookup().findVirtual(String.class,
                "concat", methodType(String.class, String.class));
        MethodHandle length = lookup().findVirtual(String.class,
                "length", methodType(int.class));
        System.out.println((String) cat.invokeExact("x", "y")); // xy
        MethodHandle f0 = filterReturnValue(cat, length);
        System.out.println((int) f0.invokeExact("x", "y")); // 2
    }

    @Test
    public void test1()
    {
        APS aps = new APS();

        String[] prefixes = aps.getPrefixes();
        String[] exp = new String[]
        {
            "string", "boolean", "byte", "char", "short", "long", "double", "foo", "bar", "goo"
        };

        assertArrayEquals(exp, prefixes);

        aps.set("string", "ääkkönen");
        assertEquals("ääkkönen", aps.getObject("string"));

        aps.set("boolean", true);
        assertTrue(aps.getBoolean("boolean"));

        aps.set("byte", (byte) 123);
        assertEquals(123, aps.getByte("byte"));

        aps.set("char", 'ö');
        assertEquals('ö', aps.getChar("char"));

        aps.set("short", (short) 12);
        assertEquals(12, aps.getShort("short"));

        aps.set("long", 12345L);
        assertEquals(12345L, aps.getLong("long"));

        aps.set("double", 123.456);
        assertEquals(123.456, aps.getDouble("double"), 1e-10);

        aps.set("foo", 123.456F);
        assertEquals(123.456F, aps.getFloat("foo"), 1e-10);

        aps.set("bar", 123.456F);
        assertEquals(123.456F, aps.getFloat("bar"), 1e-10);

        aps.set("goo", 123);
        assertEquals(123, aps.i);

        APS aps2 = new APS(aps);
        assertEquals(aps, aps2);

    }

    class APS extends AnnotatedPropertyStore
    {

        @Property(value = "string", ordinal = 1)
        String s;
        @Property(value = "boolean", ordinal = 2)
        boolean b;
        @Property(value = "byte", ordinal = 3)
        byte by;
        @Property(value = "char", ordinal = 4)
        char cc;
        @Property(value = "short", ordinal = 5)
        short sh;
        @Property(value = "long", ordinal = 6)
        long ll;
        @Property(value = "double", ordinal = 7)
        double db;
        @Property(ordinal = 8)
        float foo;
        @Property(value = "bar", ordinal = 9)
        float ba;
        private int i;

        public APS(AnnotatedPropertyStore aps)
        {
            super(aps);
        }

        public APS()
        {
        }

        @Property(ordinal = 10)
        public void setGoo(int i)
        {
            this.i = i;
        }
        @Property(ordinal = 10)
        public int getGoo()
        {
            return i;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final APS other = (APS) obj;
            if (this.b != other.b)
            {
                return false;
            }
            if (this.by != other.by)
            {
                return false;
            }
            if (this.cc != other.cc)
            {
                return false;
            }
            if (this.sh != other.sh)
            {
                return false;
            }
            if (this.ll != other.ll)
            {
                return false;
            }
            if (Double.doubleToLongBits(this.db) != Double.doubleToLongBits(other.db))
            {
                return false;
            }
            if (Float.floatToIntBits(this.foo) != Float.floatToIntBits(other.foo))
            {
                return false;
            }
            if (Float.floatToIntBits(this.ba) != Float.floatToIntBits(other.ba))
            {
                return false;
            }
            if (this.i != other.i)
            {
                return false;
            }
            if (!Objects.equals(this.s, other.s))
            {
                return false;
            }
            return true;
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
