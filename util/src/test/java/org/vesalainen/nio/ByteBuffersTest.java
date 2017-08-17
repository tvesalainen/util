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
package org.vesalainen.nio;

import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBuffersTest
{
    
    public ByteBuffersTest()
    {
    }

    @Test
    public void test1()
    {
        ByteBuffer bb1 = ByteBuffer.wrap("0123456789".getBytes());
        ByteBuffer bb2 = ByteBuffer.wrap("0123456789".getBytes());
        ByteBuffer bb3 = ByteBuffer.allocate(5);
        ByteBuffer bb4 = ByteBuffer.allocate(10);
        ByteBuffers.move(new ByteBuffer[]{bb1, bb2}, new ByteBuffer[]{bb3, bb4});
        assertFalse(bb1.hasRemaining());
        assertEquals(5, bb2.remaining());
        assertFalse(bb3.hasRemaining());
        assertFalse(bb4.hasRemaining());
    }
    
    @Test
    public void test2()
    {
        ByteBuffer bb1 = ByteBuffer.wrap("0123456789".getBytes());
        ByteBuffer bb2 = ByteBuffer.wrap("0123456789".getBytes());
        ByteBuffer bb3 = ByteBuffer.allocate(15);
        ByteBuffer bb4 = ByteBuffer.allocate(10);
        ByteBuffers.move(new ByteBuffer[]{bb1, bb2}, new ByteBuffer[]{bb3, bb4});
        assertFalse(bb1.hasRemaining());
        assertFalse(bb2.hasRemaining());
        assertFalse(bb3.hasRemaining());
        assertEquals(5, bb4.remaining());
    }
    
}
