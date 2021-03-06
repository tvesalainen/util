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
package org.vesalainen.util.navi;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.test.Asserts;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationRangeMapTest
{
    
    public LocationRangeMapTest()
    {
    }

    @Test
    public void test1()
    {                                                   //n   e    s   w
        LocationBoundingBox bb1 = new LocationBoundingBox(30, 170, 20, 160);
        LocationBoundingBox bb2 = new LocationBoundingBox(10, 175, 0, 160);
        LocationBoundingBox bb3 = new LocationBoundingBox(-10, -170, -20, 170);
        LocationBoundingBox bb4 = new LocationBoundingBox(-30, -160, -40, -170);
        
        LocationRangeMap<LocationBoundingBox> db = new LocationRangeMap<>();
        db.put(bb1, bb1);
        db.put(bb2, bb2);
        db.put(bb3, bb3);
        db.put(bb4, bb4); 
        
        List<LocationBoundingBox> all = CollectionHelp.create(bb1, bb2, bb3, bb4);
        List<LocationBoundingBox> list;
        
        list  = db.overlapping(bb2).collect(Collectors.toList());
        for (LocationBoundingBox bb : list)
        {
            assertTrue(bb2.isIntersecting(bb));
        }
        all.removeAll(list);
        for (LocationBoundingBox bb : all)
        {
            assertFalse(bb2.isIntersecting(bb));
        }
        
        list  = db.overlapping(bb3).collect(Collectors.toList());
        assertEquals(1, list.size());
        
        Asserts.assertSerializable(db);
    }
    
}
