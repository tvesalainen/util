/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import static java.nio.charset.StandardCharsets.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrintBufferTest
{
    
    public PrintBufferTest()
    {
    }

    @Test
    public void test1()
    {
        ByteBuffer bb = ByteBuffer.allocate(100);
        PrintBuffer pb = new PrintBuffer(US_ASCII, bb);
        pb.format("test %d %s", 123, "qwerty");
        bb.flip();
        ByteBufferCharSequence seq = new ByteBufferCharSequence(bb);
        assertEquals("test 123 qwerty", seq.toString());
    }
    @Test
    public void test2()
    {
        ByteBuffer bb = ByteBuffer.allocate(100);
        PrintBuffer pb = new PrintBuffer(UTF_8, bb);
        pb.format("testiä %d %s", 123, "ÖÄöä");
        pb.flush();
        bb.flip();
        String seq = new String(bb.array(), 0, bb.remaining(), UTF_8);
        assertEquals("testiä 123 ÖÄöä", seq.toString());
    }
    
}
