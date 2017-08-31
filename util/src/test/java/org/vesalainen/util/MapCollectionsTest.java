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
public class MapCollectionsTest
{
    
    public MapCollectionsTest()
    {
    }

    @Test
    public void testUnmodifiableMapList()
    {
        MapList<Integer,String> ml = new HashMapList<>();
        ml.add(1, "foo");
        ml.add(2, "bar");
        MapList<Integer, String> uml = MapCollections.unmodifiableMapList(ml);
        try
        {
            uml.remove(1);
            fail("UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ex)
        {
        }
        try
        {
            uml.get(2).clear();
            fail("UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ex)
        {
        }
    }
    @Test
    public void testUnmodifiableMapSet()
    {
        MapSet<Integer,String> ms = new HashMapSet<>();
        ms.add(1, "foo");
        ms.add(2, "bar");
        MapSet<Integer, String> ums = MapCollections.unmodifiableMapSet(ms);
        try
        {
            ums.remove(1);
            fail("UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ex)
        {
        }
        try
        {
            ums.get(2).clear();
            fail("UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ex)
        {
        }
    }
    
}
