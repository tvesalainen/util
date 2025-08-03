/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SynchronizedRingByteBufferTest
{
    
    public SynchronizedRingByteBufferTest()
    {
    }

    @Test
    public void testBBIO() throws IOException, InterruptedException
    {
        String exp = "qwerty";
        ByteBuffer bb1 = ByteBuffer.wrap(exp.getBytes(US_ASCII)).compact();
        ByteBuffer bb2 = ByteBuffer.allocate(6);
        RingByteBuffer ring = new RingByteBuffer(11, false);
        SynchronizedRingByteBuffer sync = new SynchronizedRingByteBuffer(11, false);
        for (int ii=0;ii<5;ii++)
        {
            ring.discard();
            bb1.flip();
            int rc = ring.fill(bb1);
            assertEquals(6, rc);
            assertEquals(0, ring.marked());
            assertEquals(6, ring.remaining());
            ring.getAll(false);
            assertEquals(6, ring.marked());
            assertEquals(0, ring.remaining());
            rc = sync.tryFillAll(ring);
            assertEquals(6, rc);
            sync.waitRemaining();
            sync.getAll(false);
            bb2.clear();
            rc = sync.writeTo(bb2);
            assertEquals(6, rc);
            assertEquals(6, bb2.position());
            assertArrayEquals(bb1.array(), bb2.array());
            sync.discard();
        }
    }
    
}
