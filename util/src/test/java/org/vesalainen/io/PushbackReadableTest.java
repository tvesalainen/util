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
import java.nio.CharBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class PushbackReadableTest
{
    
    public PushbackReadableTest()
    {
    }

    /**
     * Test of pushback method, of class PushbackReadable.
     */
    @Test
    public void testPushback() throws Exception
    {
        try (InputStream is = RewindableReaderTest.class.getClassLoader().getResourceAsStream("test.txt");
            InputStreamReader isr = new InputStreamReader(is);
            PushbackReadable pr = new PushbackReadable(isr);
                )
        {
            CharBuffer cb1 = CharBuffer.allocate(32);
            CharBuffer cb2 = CharBuffer.allocate(32);
            int rc1 = pr.read(cb1);
            assertEquals(32, rc1);
            cb1.flip();
            assertEquals("Lorem ipsum dolor sit amet, soll", cb1.toString());
            cb1.clear();
            cb1.append("inserting text ");
            cb2.append("at the position");
            cb1.flip();
            cb2.flip();
            pr.pushback(cb1, cb2);
            cb1.clear();
            cb1.limit(10);
            rc1 = pr.read(cb1);
            cb1.flip();
            String exp = "inserting some text at the positionicitudin risus sed nulla, proin wisi cursus duis id, sit vestibulum urna dui m";
            assertEquals(exp.substring(0, rc1), cb1.toString());
            cb2.clear();
            cb2.append("some ");
            cb2.flip();
            pr.pushback(cb2);
            cb1.clear();
            int rc2 = pr.read(cb1);
            cb1.flip();
            assertEquals(exp.substring(rc1, rc1+rc2), cb1.toString());
            rc1 += rc2;
            cb1.clear();
            rc2 = pr.read(cb1);
            cb1.flip();
            assertEquals(exp.substring(rc1, rc1+rc2), cb1.toString());
        }
    }
    
}
