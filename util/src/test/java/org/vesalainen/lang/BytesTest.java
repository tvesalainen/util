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
package org.vesalainen.lang;

import java.io.ByteArrayOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BytesTest
{
    
    public BytesTest()
    {
    }

    @Test
    public void testBoolean()
    {
        byte[] exp = new byte[]{(byte)1, (byte)0};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set(true, o::write);
        Bytes.set(false, o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testByte()
    {
        byte[] exp = new byte[]{(byte)1, (byte)0};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set((byte)1, o::write);
        Bytes.set((byte)0, o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testChar()
    {
        byte[] exp = new byte[]{(byte)0, (byte)32};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set(' ', o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testShort()
    {
        byte[] exp = new byte[]{(byte)1, (byte)2};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set((short)0x0102, o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testInt()
    {
        byte[] exp = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set(0x01020304, o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testLong()
    {
        byte[] exp = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set(0x0102030405060708L, o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testSerializable()
    {
        byte[] exp = new byte[]{(byte)-84, (byte)-19, (byte)0, (byte)5, (byte)116, (byte)0, (byte)6, (byte)'q', (byte)'w', (byte)'e', (byte)'r', (byte)'t', (byte)'y'};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set("qwerty", o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    @Test
    public void testNonserializable()
    {
        byte[] exp = new byte[]{(byte)'T', (byte)'{', (byte)'i', (byte)'=', (byte)'1', (byte)'}'};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bytes.set(new T(1), o::write);
        assertArrayEquals(exp, o.toByteArray());
    }
    
    class T
    {
        private int i;

        public T(int i)
        {
            this.i = i;
        }

        @Override
        public String toString()
        {
            return "T{" + "i=" + i + '}';
        }
        
    }
}
