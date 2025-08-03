/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferInputStreamTest
{
    
    public ByteBufferInputStreamTest()
    {
    }

    @Test
    public void test() throws IOException
    {
        byte[] bytes = "qwertyasdfgh1234567890".getBytes();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ByteBufferInputStream bbis = new ByteBufferInputStream(bb);
        byte[] b = new byte[10];
        assertEquals('q', bbis.read());
        int rc = bbis.read(b);
        assertEquals(10, rc);
        assertEquals('w', b[0]);
        assertEquals(11, bbis.available());
        bbis.mark(0);
        assertEquals(11, bbis.skip(20));
        assertEquals(0, bbis.available());
        bbis.reset();
        assertEquals(11, bbis.available());
    }
    
}
