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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GZIPChannelTest
{
    
    public GZIPChannelTest()
    {
    }

    @Test
    public void test1() throws URISyntaxException, IOException
    {
        URL url = GZIPChannelTest.class.getResource("/router2.gz");
        File file = new File(url.toURI());
        Path path = file.toPath();
        ByteBuffer bb = ByteBuffer.allocate(4096);
        try (GZIPChannel ch = new GZIPChannel(path, READ))
        {
            ch.read(bb);
            ch.read(bb);
        }
    }
    
}
