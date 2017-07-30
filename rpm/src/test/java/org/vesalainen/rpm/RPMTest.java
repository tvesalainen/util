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
package org.vesalainen.rpm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class RPMTest
{
    
    public RPMTest()
    {
    }

    @Test
    public void test1() throws IOException, URISyntaxException
    {
        URL url = RPMTest.class.getResource("/lsb-4.0-26.1.2.armv7hl.rpm");
        try (   RPM rpm = new RPM())
        {
            Path path = Paths.get(url.toURI());
            long size = Files.size(path);
            ByteBuffer bb = ByteBuffer.allocate((int) size);
            byte[] buf = Files.readAllBytes(path);
            ByteBuffer exp = ByteBuffer.wrap(buf);
            rpm.load(path);
            // lead
            assertArrayEquals(RPM.LEAD_MAGIC, rpm.magic);
            assertEquals(3, rpm.major);
            assertEquals(0, rpm.minor);
            assertEquals(0, rpm.type);
            //assertEquals(1, rpm.archnum);
            //assertEquals("lsb-4.0-3mdv2010.1", rpm.name);
            assertEquals(1, rpm.osnum);
            assertEquals(5, rpm.signatureType);
            // header
            assertArrayEquals(RPM.HEADER_MAGIC, rpm.signature.magic);
            rpm.append(System.err);
            rpm.save(bb);
            assertSame(exp, bb);
        }
    }
    private void assertSame(ByteBuffer b1, ByteBuffer b2)
    {
        int len = b2.position();
        for (int ii=0;ii<len;ii++)
        {
            byte c1 = b1.get(ii);
            byte c2 = b2.get(ii);
            if (c1 != c2)
            {
                throw new AssertionError(c1+" != "+c2+" at position "+ii);
            }
        }
    }
}
