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
package org.vesalainen.nio.channels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChannelHelperTest
{
    
    public ChannelHelperTest()
    {
    }

    @Test
    public void testReadableByteChannelHeap() throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream("qwertyuioplkjhgfdsazxcvbnm".getBytes());
        ReadableByteChannel ch = ChannelHelper.newReadableByteChannel(bais);
        ByteBuffer bb = ByteBuffer.allocate(6);

        int rc = ch.read(bb);
        assertEquals(6, rc);
        assertArrayEquals("qwerty".getBytes(), bb.array());

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertArrayEquals("uioplk".getBytes(), bb.array());

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertArrayEquals("jhgfds".getBytes(), bb.array());    

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertArrayEquals("azxcvb".getBytes(), bb.array());    

        bb.clear();
        rc = ch.read(bb);
        assertEquals(2, rc);
        bb.flip();
        assertEquals('n', bb.get());    
        assertEquals('m', bb.get());    
    }
    
    @Test
    public void testReadableByteChannelDirect() throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream("qwertyuioplkjhgfdsazxcvbnm".getBytes());
        ReadableByteChannel ch = ChannelHelper.newReadableByteChannel(bais);
        ByteBuffer bb = ByteBuffer.allocateDirect(6);

        int rc = ch.read(bb);
        assertEquals(6, rc);
        assertTrue(equals("qwerty", bb));

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertTrue(equals("uioplk", bb));

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertTrue(equals("jhgfds", bb));

        bb.clear();
        rc = ch.read(bb);
        assertEquals(6, rc);
        assertTrue(equals("azxcvb", bb));

        bb.clear();
        rc = ch.read(bb);
        assertEquals(2, rc);
        bb.flip();
        assertTrue(equals("nm", bb));
    }
    @Test
    public void testWritableByteChannelHeap() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel ch = ChannelHelper.newWritableByteChannel(baos);
        ByteBuffer bb = ByteBuffer.allocate(6);
        
        bb.put("qwe".getBytes());
        bb.flip();
        int rc = ch.write(bb);
        assertEquals(3, rc);
        assertEquals("qwe", baos.toString(StandardCharsets.US_ASCII.name()));

        bb.clear();
        bb.put("rtyuio".getBytes());
        bb.flip();
        rc = ch.write(bb);
        assertEquals(6, rc);
        assertEquals("qwertyuio", baos.toString(StandardCharsets.US_ASCII.name()));

        bb.clear();
        bb.put("asdfg".getBytes());
        bb.flip();
        rc = ch.write(bb);
        assertEquals(5, rc);
        assertEquals("qwertyuioasdfg", baos.toString(StandardCharsets.US_ASCII.name()));
    }
    @Test
    public void testWritableByteChannelDirect() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel ch = ChannelHelper.newWritableByteChannel(baos);
        ByteBuffer bb = ByteBuffer.allocateDirect(6);
        
        bb.put("qwe".getBytes());
        bb.flip();
        int rc = ch.write(bb);
        assertEquals(3, rc);
        assertEquals("qwe", baos.toString(StandardCharsets.US_ASCII.name()));

        bb.clear();
        bb.put("rtyuio".getBytes());
        bb.flip();
        rc = ch.write(bb);
        assertEquals(6, rc);
        assertEquals("qwertyuio", baos.toString(StandardCharsets.US_ASCII.name()));

        bb.clear();
        bb.put("asdfg".getBytes());
        bb.flip();
        rc = ch.write(bb);
        assertEquals(5, rc);
        assertEquals("qwertyuioasdfg", baos.toString(StandardCharsets.US_ASCII.name()));
    }
    private boolean equals(String str, ByteBuffer bb)
    {
        int len = str.length();
        for (int ii=0;ii<len;ii++)
        {
            if (bb.get(ii) != str.charAt(ii))
            {
                return false;
            }
        }
        return true;
    }
}
