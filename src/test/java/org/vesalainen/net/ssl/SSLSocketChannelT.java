/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
public class SSLSocketChannelT
{
    
    public SSLSocketChannelT()
    {
    }

    @Test
    public void test1() throws IOException
    {
        SSLSocketChannel ch = SSLSocketChannel.open("www.facebook.com", 443);
        ByteBuffer req = ByteBuffer.wrap("GET / HTTP/1.1\r\nHost: www.facebook.com\r\n\r\n".getBytes());
        int wr = ch.write(req);
        assertEquals(req.array().length, wr);
        ByteBuffer res = ByteBuffer.allocate(1024);
        int rd = ch.read(res);
        assertEquals(res.position(), rd);
        System.err.println(HexDump.toHex(res.array()));
        ch.close();
    }
    
}
