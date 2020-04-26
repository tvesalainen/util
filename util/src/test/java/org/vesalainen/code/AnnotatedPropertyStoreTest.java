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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.code.APS.E;
import static org.vesalainen.code.APS.E.B2;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.IntGetter;
import org.vesalainen.code.getter.LongGetter;
import org.vesalainen.code.getter.ObjectGetter;
import org.vesalainen.code.setter.ByteSetter;
import org.vesalainen.code.setter.CharSetter;
import org.vesalainen.code.setter.DoubleSetter;
import org.vesalainen.code.setter.FloatSetter;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.code.setter.ObjectSetter;
import org.vesalainen.code.setter.ShortSetter;

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
    public void test1() throws IOException, NoSuchAlgorithmException
    {
        APS aps = new APS();
        Path tmp = Files.createTempFile(null, null);
        aps.store(tmp);
        APS aps0 = new APS(tmp);
        assertTrue(aps.equals(aps0));

        String[] properties = aps.getProperties();
        String[] exp = new String[]
        {
            "enum", "string", "boolean", "byte", "char", "short", "long", "double", "foo", "bar", "goo"
        };

        assertArrayEquals(exp, properties);

        aps.set("enum", B2);
        assertEquals(B2, aps.getObject("enum"));

        String testStr = "ääkkönen\r\nloppu";
        aps.set("string", testStr);
        assertEquals(testStr, aps.getObject("string"));

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

        try
        {
            aps.set("goo", 123456789);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            
        }
        assertEquals(123, aps.i);

        APS aps2 = new APS(aps);
        assertTrue(aps.equals(aps2));

        aps.store(tmp);
        APS aps3 = new APS(tmp);
        assertTrue(aps.equals(aps3));
        APS aps4 = AnnotatedPropertyStore.getInstance(tmp);
        assertTrue(aps.equals(aps4));
        
        byte[] sha1 = aps.getSha1();
        byte[] sha14 = aps4.getSha1();
        assertArrayEquals(sha1, sha14);
        
        Files.deleteIfExists(tmp);
    }
    @Test
    public void testFunctional()
    {
        APS aps = new APS();
        IntGetter ints = aps.getIntGetter("goo");
        IntGetter bytes = aps.getIntGetter("byte");
        IntGetter chars = aps.getIntGetter("char");
        IntGetter shorts = aps.getIntGetter("short");
        LongGetter longs = aps.getLongGetter("long");
        DoubleGetter floats = aps.getDoubleGetter("bar");
        DoubleGetter doubles = aps.getDoubleGetter("double");
        ObjectGetter<E> enums = aps.getObjectGetter("enum");
        ObjectGetter<String> strings = aps.getObjectGetter("string");

        IntSetter intc = aps.getIntSetter("goo");
        ByteSetter bytec = aps.getByteSetter("byte");
        CharSetter charc = aps.getCharSetter("char");
        ShortSetter shortc = aps.getShortSetter("short");
        LongSetter longc = aps.getLongSetter("long");
        FloatSetter floatc = aps.getFloatSetter("bar");
        DoubleSetter doublec = aps.getDoubleSetter("double");
        ObjectSetter<E> enumc = aps.getObjectSetter("enum");
        ObjectSetter<String> stringc = aps.getObjectSetter("string");
        
        intc.set(123);
        assertEquals(123, ints.getInt());
        
        bytec.set((byte)123);
        assertEquals(123, bytes.getInt());
        
        charc.set('ä');
        assertEquals('ä', chars.getInt());
        
        shortc.set((short)123);
        assertEquals(123, shorts.getInt());
        
        longc.set(1234567890);
        assertEquals(1234567890, longs.getLong());
        
        floatc.set(1.23F);
        assertEquals(1.23, floats.getDouble(), 1e-7);
        
        doublec.set(1.23);
        assertEquals(1.23, doubles.getDouble(), 1e-10);
        
        enumc.set(E.B2);
        assertEquals(E.B2, enums.getObject());
        
        stringc.set("qwerty");
        assertEquals("qwerty", strings.getObject());
    }
}
