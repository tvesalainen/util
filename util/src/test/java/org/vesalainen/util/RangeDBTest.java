/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RangeDBTest
{
    
    public RangeDBTest()
    {
    }

    @Test
    public void test1()
    {
        RangeMap<Integer,String> db = new RangeMap<>();
        List<String> list;
        list = db.overlapping(12, 100).collect(Collectors.toList());
        assertEquals(0, list.size());

        db.put(1, 3, "1-3");
        db.put(1, 7, "1-7");
        db.put(2, 5, "2-5");
        db.put(3, 6, "3-6");
        db.put(9, 12, "9-12");

        db.ensureSorted();
        assertEquals(5, db.excludeFrom(-1));
        assertEquals(5, db.excludeFrom(0));
        assertEquals(4, db.excludeFrom(1));
        assertEquals(3, db.excludeFrom(2));
        assertEquals(2, db.excludeFrom(3));
        assertEquals(1, db.excludeFrom(-5));
        assertEquals(1, db.excludeFrom(4));
        assertEquals(0, db.excludeFrom(-6));
        
        assertEquals(0, db.excludeTo(-1));
        assertEquals(1, db.excludeTo(0));
        assertEquals(1, db.excludeTo(-2));
        assertEquals(2, db.excludeTo(1));
        assertEquals(3, db.excludeTo(2));
        assertEquals(4, db.excludeTo(3));
        assertEquals(4, db.excludeTo(-5));
        assertEquals(5, db.excludeTo(4));
        assertEquals(5, db.excludeTo(-6));
        
        list = db.overlapping(2, 7).collect(Collectors.toList());
        assertEquals(4, list.size());
        
        list = db.overlapping(2, 6).collect(Collectors.toList());
        assertEquals(4, list.size());
        
        list = db.overlapping(10, 11).collect(Collectors.toList());
        assertEquals(1, list.size());
        
        list = db.overlapping(9, 12).collect(Collectors.toList());
        assertEquals(1, list.size());
        
        list = db.overlapping(0, 30).collect(Collectors.toList());
        assertEquals(5, list.size());
        
        list = db.overlapping(0, 1).collect(Collectors.toList());
        assertEquals(0, list.size());
        
        list = db.overlapping(12, 100).collect(Collectors.toList());
        assertEquals(0, list.size());
        
        list = db.overlapping(13, 100).collect(Collectors.toList());
        assertEquals(0, list.size());
        
        list = db.overlapping(1, 2).collect(Collectors.toList());
        assertEquals(2, list.size());
        
        list = db.overlapping(4, 5).collect(Collectors.toList());
        assertEquals(3, list.size());
        
        db.put(-4, 100, "-4-100");
        
        list = db.overlapping(4, 5).collect(Collectors.toList());
        assertEquals(4, list.size());
        
        list = db.overlapping(0, 1).collect(Collectors.toList());
        assertEquals(1, list.size());
        
        list = db.overlapping(9, 12).collect(Collectors.toList());
        assertEquals(2, list.size());
        
        db.put(3, 6, "3-6*");

        list = db.overlapping(4, 5).collect(Collectors.toList());
        assertEquals(5, list.size());
        
    }
    
}
