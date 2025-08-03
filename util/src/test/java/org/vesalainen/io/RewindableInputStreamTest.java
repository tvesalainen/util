/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen
 */
public class RewindableInputStreamTest
{
    
    public RewindableInputStreamTest()
    {
    }

    /**
     * Test of rewind method, of class RewindableReader.
     */
    @Test
    public void test1() throws Exception
    {
        try (InputStream is = RewindableInputStreamTest.class.getClassLoader().getResourceAsStream("test.txt");
            RewindableInputStream ris = new RewindableInputStream(is, 64, 32);
                )
        {
            byte[] buf = new byte[20];
            int rc = ris.read(buf);
            while (rc == buf.length)
            {
                String s1 = new String(buf);
                ris.rewind(10);
                rc = ris.read(buf);
                String s2 = new String(buf);
                assertEquals(s1.substring(10), s2.substring(0, 10));
                rc = ris.read(buf);
            }
        }
    }

}
