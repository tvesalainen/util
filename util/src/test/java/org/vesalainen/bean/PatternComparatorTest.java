/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.bean;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PatternComparatorTest
{
    
    public PatternComparatorTest()
    {
    }

    @Test
    public void test1()
    {
        PatternComparator pc = new PatternComparator();
        String[] sa = new String[] 
        {
            "f1",
            "b#",
            "b=hint",
            "f2",
            "a+hint",
            "f3",
        };
        Arrays.sort(sa, pc);
        assertEquals("b=hint", sa[0]);
        assertEquals("a+hint", sa[1]);
        assertEquals("f1", sa[2]);
        assertEquals("f2", sa[3]);
        assertEquals("f3", sa[4]);
        assertEquals("b#", sa[5]);
    }
    
    @Test
    public void test2()
    {
        PatternComparator pc = new PatternComparator();
        String[] exp = new String[] 
        {
            "a#",
            "a.0=hint",
            "a.0.x",
            "a.0.x#",
            "a.0.x.0-",
            "a.0-",
        };
        String[] sa = new String[] 
        {
            "a#",
            "a.0=hint",
            "a.0.x",
            "a.0.x#",
            "a.0.x.0-",
            "a.0-",
        };
        assertArrayEquals(exp, sa);
    }
    
}
