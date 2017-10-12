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
package org.vesalainen.math;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.Lists;
import org.vesalainen.util.MapSet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SymmetricDifferenceMatcherTest
{
    
    public SymmetricDifferenceMatcherTest()
    {
    }

    @Test
    public void test1()
    {
        SymmetricDifferenceMatcher<Integer,String> m = new SymmetricDifferenceMatcher();
        
        m.map(Lists.create(0, 1, 2, 3, 4), "small");
        m.map(Lists.create(5, 6, 7, 8, 9, 10), "big");
        m.map(Lists.create(1, 3, 5, 7, 9), "odd");
        m.map(Lists.create(2, 4, 6, 8, 10), "even");
        
        Set<String> unresolved = new HashSet<>();
        unresolved.addAll(Lists.create("small", "big", "odd", "even"));
        assertEquals(unresolved, m.getUnresolved());
        
        MapSet<String,Integer> samples = new HashMapSet<>();
        samples.addAll("SMALL", Lists.create(0, 1, 2));
        samples.addAll("BIG", Lists.create(10, 9, 8));
        samples.addAll("ODD", Lists.create(3, 7));
        samples.addAll("EVEN", Lists.create(4, 8));
        
        m.match(samples, (String s1, String s2)->assertTrue(s1.equalsIgnoreCase(s2)));
        assertTrue(m.getUnresolved().isEmpty());
    }
    
}
