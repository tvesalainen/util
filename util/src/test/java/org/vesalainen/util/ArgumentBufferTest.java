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
package org.vesalainen.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArgumentBufferTest
{
    
    public ArgumentBufferTest()
    {
    }

    @Test
    public void test1()
    {
        ArgumentBuffer ab = new ArgumentBuffer(2);
        ab.put((byte)1);
        assertEquals(1, ab.getByte());
        ab.put((byte)2);
        assertEquals(2, ab.getByte());
        ab.put((byte)3);
        assertEquals(3, ab.getByte());
    }
    
}
