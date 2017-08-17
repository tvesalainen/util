/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.concurrent;

import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RingbufferSupportTest
{
    
    public RingbufferSupportTest()
    {
    }

    @Test
    public void test1()
    {
        ByteBuffer bb = ByteBuffer.allocate(6);
        for (byte b=1;b<=6;b++)
        {
            bb.put(b);
        }
        RingbufferSupport rb = new RingbufferSupport(bb);
        ByteBuffer[] ar = rb.getBuffers(0, 6);
        assertEquals(1, ar.length);
        assertEquals(6, ar[0].remaining());
        for (byte b=1;b<=6;b++)
        {
            assertEquals(b, ar[0].get());
        }
        ar = rb.getBuffers(3, 5);
        assertEquals(2, ar.length);
        assertEquals(3, ar[0].remaining());
        assertEquals(2, ar[1].remaining());
        assertEquals(4, ar[0].get());
        assertEquals(1, ar[1].get());
        
        ar = rb.getBuffers(2, 2);
        assertEquals(1, ar.length);
        assertEquals(2, ar[0].remaining());
        assertEquals(3, ar[0].get());
        
    }
    
}
