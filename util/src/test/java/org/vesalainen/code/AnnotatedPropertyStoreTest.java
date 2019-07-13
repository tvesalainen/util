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
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.code.APS.E.B2;

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
    public void test1() throws IOException
    {
        APS aps = new APS();
        Path tmp = Files.createTempFile(null, null);
        aps.store(tmp);
        APS aps0 = new APS(tmp);
        assertTrue(aps.isSame(aps0));

        String[] prefixes = aps.getPrefixes();
        String[] exp = new String[]
        {
            "enum", "string", "boolean", "byte", "char", "short", "long", "double", "foo", "bar", "goo"
        };

        assertArrayEquals(exp, prefixes);

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

        aps.set("goo", 123456789);
        assertEquals(123, aps.i);

        APS aps2 = new APS(aps);
        assertTrue(aps.isSame(aps2));

        aps.store(tmp);
        APS aps3 = new APS(tmp);
        assertTrue(aps.isSame(aps3));
        APS aps4 = AnnotatedPropertyStore.getInstance(tmp);
        assertTrue(aps.isSame(aps4));
        Files.deleteIfExists(tmp);
    }

}
