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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PipeChannelTest
{
    
    public PipeChannelTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        byte[] bytes = "qwerty".getBytes(US_ASCII);
        ByteBuffer bb1 = ByteBuffer.wrap(bytes);
        ByteBuffer bb2 = ByteBuffer.allocate(6);
        PipeChannel[] peers = PipeChannel.createPeers();
        assertEquals(2, peers.length);
        PipeChannel pc1 = peers[0];
        PipeChannel pc2 = peers[1];
        int w = pc1.write(bb1);
        assertEquals(6, w);
        int r = pc2.read(bb2);
        assertEquals(6, r);
        assertArrayEquals(bytes, bb2.array());
        bb1.clear();
        bb2.clear();
        w = pc2.write(bb1);
        assertEquals(6, w);
        r = pc1.read(bb2);
        assertEquals(6, r);
        assertArrayEquals(bytes, bb2.array());
    }
    
}
