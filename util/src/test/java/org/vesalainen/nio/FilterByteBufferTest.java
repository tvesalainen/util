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
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
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
        int ep = 0;
        assertEquals(ep, fbb.position());
        fbb.put((byte)1);
        ep++;
        assertEquals(ep, fbb.position());
        fbb.alignOutput(8);
        ep+=7;
        assertEquals(ep, fbb.position());
        fbb.put("qwerty".getBytes(US_ASCII));
        ep+=6;
        assertEquals(ep, fbb.position());
        fbb.put("asdfghjk".getBytes(US_ASCII), 2, 3);
        ep+=3;
        assertEquals(ep, fbb.position());
        fbb.putChar('a');
        ep+=2;
        assertEquals(ep, fbb.position());
        fbb.putDouble(123.456);
        ep+=8;
        assertEquals(ep, fbb.position());
        fbb.putFloat(1.23F);
        ep+=4;
        assertEquals(ep, fbb.position());
        fbb.putInt(123456);
        ep+=4;
        assertEquals(ep, fbb.position());
        fbb.putLong(987654321L);
        ep+=8;
        assertEquals(ep, fbb.position());
        fbb.putShort((short)123);
        ep+=2;
        assertEquals(ep, fbb.position());
        fbb.putString("foobar");
        ep+=7;
        assertEquals(ep, fbb.position());
        
        fbb.flush();
        fbb.position(0);
        ep = 0;
        
        bb.flip();
        assertEquals(1, fbb.get());
        ep+=1;
        assertEquals(ep, fbb.position());
        fbb.alignInput(8);
        ep+=7;
        assertEquals(ep, fbb.position());
        byte[] buf = new byte[6];
        fbb.get(buf);
        ep+=6;
        assertEquals(ep, fbb.position());
        assertEquals("qwerty", new String(buf, US_ASCII));
        fbb.get(buf, 2, 3);
        ep+=3;
        assertEquals(ep, fbb.position());
        assertEquals("dfg", new String(buf, 2, 3, US_ASCII));
        assertEquals('a', fbb.getChar());
        ep+=2;
        assertEquals(ep, fbb.position());
        assertEquals(123.456, fbb.getDouble(), 1e-10);
        ep+=8;
        assertEquals(ep, fbb.position());
        assertEquals(1.23F, fbb.getFloat(), 1e-10);
        ep+=4;
        assertEquals(ep, fbb.position());
        assertEquals(123456, fbb.getInt());
        ep+=4;
        assertEquals(ep, fbb.position());
        assertEquals(987654321L, fbb.getLong());
        ep+=8;
        assertEquals(ep, fbb.position());
        assertEquals(123, fbb.getShort());
        ep+=2;
        assertEquals(ep, fbb.position());
        assertEquals("foobar", fbb.getString());
        ep+=7;
        assertEquals(ep, fbb.position());
    }
    @Test
    public void testEOF() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        FilterByteBuffer fbb = new FilterByteBuffer(bb, BufferedInputStream::new, BufferedOutputStream::new);
        fbb.put((byte)1);
        fbb.flush();
        bb.flip();
        try
        {
            fbb.getShort();
            fail("should throw EOFException");
        }
        catch (EOFException ex)
        {
            
        }
    }    
    @Test
    public void testUnderflow() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        FilterByteBuffer fbb = new FilterByteBuffer(bb, BufferedInputStream::new, BufferedOutputStream::new);
        fbb.put((byte)1);
        fbb.flush();
        bb.flip();
        
        try
        {
            byte[] buf = new byte[10];
            fbb.get(buf);
            fail("should throw EOFException");
        }
        catch (EOFException ex)
        {
            
        }
    }
    @Test
    public void testOverflow() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(7);
        FilterByteBuffer fbb = new FilterByteBuffer(bb, BufferedInputStream::new, BufferedOutputStream::new);
        fbb.putDouble(123.456);
        try
        {
            fbb.flush();
            fail("should throw BufferOverflowException");
        }
        catch (BufferOverflowException ex)
        {
            
        }
    }
}
