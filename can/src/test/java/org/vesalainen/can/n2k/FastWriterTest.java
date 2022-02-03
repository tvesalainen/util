/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.n2k;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastWriterTest
{
    
    public FastWriterTest()
    {
    }

    @Test
    public void test()
    {
        Random r = new Random(12345678L);
        byte[] b1 = new byte[137];
        byte[] b2 = new byte[137];
        r.nextBytes(b1);
        FastReader reader = new FastReader("name", b2);
        FastWriter writer = new FastWriter();
        writer.write(137, b1, (b)->reader.update(0, 0, 8, b));
        assertArrayEquals(b1, b2);
    }
    
}
