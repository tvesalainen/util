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
        IntRangeMapBuilder<String> b = new IntRangeMapBuilder<>();
        b.put(new SimpleIntRange('a', 'z'+1), "lower");
        b.put(new SimpleIntRange('A', 'Z'+1), "upper");
        b.put(new SimpleIntRange('0', '9'+1), "digit");
        b.put(new SimpleIntRange('\n'), "nl");
        b.put(new SimpleIntRange('\t'), "tab");
        b.put(new SimpleIntRange('\r'), "cr");
        b.put(new SimpleIntRange(' '), "space");
        IntRangeMap<String> map = b.build();
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
        IntRangeMapBuilder<String> b = new IntRangeMapBuilder<>();
        b.put(new SimpleIntRange(3, 15), "low");
        b.put(new SimpleIntRange(300, 1500), "mid");
        b.put(new SimpleIntRange(3000, 15000), "high");
        IntRangeMap<String> map = b.build();
        assertEquals("low", map.get(10));
        assertEquals("mid", map.get(1000));
        assertEquals("high", map.get(10000));
        assertNull(map.get(100));
    }
    @Test
    public void testAll()
    {
        IntRangeMapBuilder<String> b = new IntRangeMapBuilder<>();
        b.put(new SimpleIntRange(0, Integer.MAX_VALUE), "all");
        IntRangeMap<String> map = b.build();
        assertEquals("all", map.get(123456));
    }
}
