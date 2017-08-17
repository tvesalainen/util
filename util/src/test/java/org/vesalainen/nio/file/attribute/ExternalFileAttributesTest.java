/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ExternalFileAttributesTest
{
    
    public ExternalFileAttributesTest()
    {
    }

    @Test
    public void testArray() throws IOException
    {
        ExternalFileAttributes efa = new ExternalFileAttributes();
        byte[] exp = new byte[] { 0xf, 0xe, 0xd };
        byte[] exp2 = new byte[] { 0xe, 0xd };
        efa.set("array", exp);
        assertTrue(efa.has("array"));
        assertEquals(3, efa.size("array"));
        assertArrayEquals(exp, efa.get("array"));
        assertTrue(efa.arraysEquals("array", exp));
        assertFalse(efa.arraysEquals("array", exp2, 1, 2));
    }
    
    @Test
    public void testPrimitives() throws IOException
    {
        ExternalFileAttributes efa = new ExternalFileAttributes();
        efa.setBoolean("boolean", true);
        efa.setDouble("double", 1.2345678e-32);
        efa.setInt("int", 987654321);
        efa.setLong("long", 123456789L);
        efa.setString("string", "foo bar och Åke Lindström");
        assertEquals(true, efa.getBoolean("boolean"));
        assertEquals(1.2345678e-32, efa.getDouble("double"), 1e-10);
        assertEquals(987654321, efa.getInt("int"));
        assertEquals(123456789L, efa.getLong("long"));
        assertEquals("foo bar och Åke Lindström", efa.getString("string"));
    }
    @Test
    public void testByteBuffer() throws IOException
    {
        ExternalFileAttributes efa = new ExternalFileAttributes();
        byte[] exp = "foo bar och Åke Lindström".getBytes(StandardCharsets.UTF_8);
        ByteBuffer  bb = ByteBuffer.wrap(exp);
        efa.write("bb", bb);
        bb.clear();
        efa.read("bb", bb);
        assertArrayEquals(exp, bb.array());
    }
}
