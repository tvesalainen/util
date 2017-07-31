/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.nio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author tkv
 */
public class FilterByteBufferTest
{
    
    public FilterByteBufferTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        FilterByteBuffer fbb = new FilterByteBuffer(bb, BufferedInputStream::new, BufferedOutputStream::new);
        fbb.put((byte)1);
        fbb.put("qwerty".getBytes(US_ASCII));
        fbb.put("asdfghjk".getBytes(US_ASCII), 2, 3);
        fbb.putChar('a');
        fbb.putDouble(123.456);
        fbb.putFloat(1.23F);
        fbb.putInt(123456);
        fbb.putLong(987654321L);
        fbb.putShort((short)123);
        fbb.flush();
        
        bb.flip();
        assertEquals(1, fbb.get());
        byte[] buf = new byte[6];
        fbb.get(buf);
        assertEquals("qwerty", new String(buf, US_ASCII));
        fbb.get(buf, 2, 3);
        assertEquals("dfg", new String(buf, 2, 3, US_ASCII));
        assertEquals('a', fbb.getChar());
        assertEquals(123.456, fbb.getDouble(), 1e-10);
        assertEquals(1.23F, fbb.getFloat(), 1e-10);
        assertEquals(123456, fbb.getInt());
        assertEquals(987654321L, fbb.getLong());
        assertEquals(123, fbb.getShort());
    }
    
}
