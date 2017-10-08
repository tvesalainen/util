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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DistinguishMapTest
{
    
    public DistinguishMapTest()
    {
    }

    @Test
    public void test1()
    {
        Set<String> gpsKeys = new HashSet<>();
        gpsKeys.add("RMC");
        gpsKeys.add("RMA");
        gpsKeys.add("RMB");
        gpsKeys.add("TXT");
        Set<String> aisKeys = new HashSet<>();
        aisKeys.add("VDM");
        aisKeys.add("RMC");
        aisKeys.add("RMA");
        aisKeys.add("RMB");
        aisKeys.add("TXT");
        Set<String> windKeys = new HashSet<>();
        windKeys.add("WHV");
        windKeys.add("DBT");
        windKeys.add("TXT");
        DistinguishMap<String,String> dm = new DistinguishMap<>();
        dm.add(gpsKeys, "gps");
        assertEquals(gpsKeys, dm.keySet());
        dm.add(aisKeys, "ais");
        assertEquals(1, dm.size());
        assertTrue(dm.containsKey("VDM"));
        dm.add(windKeys, "wind");
        assertEquals(3, dm.size());
        assertTrue(dm.containsKey("VDM"));
        assertTrue(dm.containsKey("WHV"));
        assertTrue(dm.containsKey("DBT"));
        dm.remove(windKeys, "wind");
        assertEquals(1, dm.size());
        assertTrue(dm.containsKey("VDM"));
        dm.remove(aisKeys, "ais");
        assertEquals(gpsKeys, dm.keySet());
    }
    
}
