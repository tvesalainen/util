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
package org.vesalainen.can.dbc;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiplexerIndicatorTest
{
    
    public MultiplexerIndicatorTest()
    {
    }

    @Test
    public void testToString1()
    {
        MultiplexerIndicator mi = new MultiplexerIndicator();
        assertEquals("M", mi.toString());
    }
    @Test
    public void testToString2()
    {
        MultiplexerIndicator mi = new MultiplexerIndicator(0);
        assertEquals("m0", mi.toString());
    }
    @Test
    public void testToString3()
    {
        MultiplexerIndicator mi = new MultiplexerIndicator(0, true);
        assertEquals("m0M", mi.toString());
    }
    
}
