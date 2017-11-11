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
package org.vesalainen.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeMapBuilderTest
{
    
    public RangeMapBuilderTest()
    {
    }

    @Test
    public void testArray()
    {
        RangeMapBuilder<String> b = new RangeMapBuilder<>();
        b.put(new SimpleRange('a', 'z'+1), "lower");
        b.put(new SimpleRange('A', 'Z'+1), "upper");
        b.put(new SimpleRange('0', '9'+1), "digit");
        b.put(new SimpleRange('\n'), "nl");
        b.put(new SimpleRange('\t'), "tab");
        b.put(new SimpleRange('\r'), "cr");
        b.put(new SimpleRange(' '), "space");
        RangeMap<String> map = b.build();
        assertEquals("digit", map.get('0'));
        assertEquals("digit", map.get('9'));
        assertNull(map.get('9'+1));
        assertEquals("lower", map.get('d'));
        assertEquals("upper", map.get('K'));
        assertEquals("tab", map.get('\t'));
        assertNull(map.get('-'));
    }
    @Test
    public void testBinary()
    {
        RangeMapBuilder<String> b = new RangeMapBuilder<>();
        b.put(new SimpleRange(3, 15), "low");
        b.put(new SimpleRange(300, 1500), "mid");
        b.put(new SimpleRange(3000, 15000), "high");
        RangeMap<String> map = b.build();
        assertEquals("low", map.get(10));
        assertEquals("mid", map.get(1000));
        assertEquals("high", map.get(10000));
        assertNull(map.get(100));
    }
    @Test
    public void testAll()
    {
        RangeMapBuilder<String> b = new RangeMapBuilder<>();
        b.put(new SimpleRange(0, Integer.MAX_VALUE), "all");
        RangeMap<String> map = b.build();
        assertEquals("all", map.get(123456));
    }
}
