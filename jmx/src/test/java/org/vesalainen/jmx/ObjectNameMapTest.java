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
package org.vesalainen.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectNameMapTest
{
    
    public ObjectNameMapTest()
    {
    }

    @Test
    public void test1() throws MalformedObjectNameException
    {
        ObjectNameMap<String> onm = new ObjectNameMap<>();
        assertTrue(onm.isEmpty());
        assertEquals(0, onm.size());
        onm.put(ObjectName.getInstance("dom1:Type=1,Value=1"), "dom1:Type=1,Value=1");
        onm.put(ObjectName.getInstance("dom1:Type=1,Value=2"), "dom1:Type=1,Value=2");
        onm.put(ObjectName.getInstance("dom1:Type=1,Value=3"), "dom1:Type=1,Value=3");
        onm.put(ObjectName.getInstance("dom1:Type=2,Value=1"), "dom1:Type=2,Value=1");
        onm.put(ObjectName.getInstance("dom1:Type=2,Value=2"), "dom1:Type=2,Value=2");
        onm.put(ObjectName.getInstance("dom1:Type=2,Value=3"), "dom1:Type=2,Value=3");
        onm.put(ObjectName.getInstance("dom2:Type=1,Value=1"), "dom2:Type=1,Value=1");
        onm.put(ObjectName.getInstance("dom2:Type=1,Value=2"), "dom2:Type=1,Value=2");
        onm.put(ObjectName.getInstance("dom2:Type=1,Value=3"), "dom2:Type=1,Value=3");
        onm.put(ObjectName.getInstance("dom2:Type=2,Value=1"), "dom2:Type=2,Value=1");
        onm.put(ObjectName.getInstance("dom2:Type=2,Value=2"), "dom2:Type=2,Value=2");
        onm.put(ObjectName.getInstance("dom2:Type=2,Value=3"), "dom2:Type=2,Value=3");
        String[] domains = onm.getDomains();
        assertEquals(2, domains.length);
        assertEquals(12, onm.size());
        assertFalse(onm.isEmpty());
        assertTrue(onm.containsKey(ObjectName.getInstance("dom1:Type=2,Value=3")));
        assertFalse(onm.containsKey(ObjectName.getInstance("dom1:Type=2,Value=4")));
        assertTrue(onm.containsValue("dom1:Type=2,Value=3"));
        assertFalse(onm.containsValue("dom1:Type=2,Value=4"));
        assertEquals("dom2:Type=1,Value=3", onm.get(ObjectName.getInstance("dom2:Type=1,Value=3")));
        assertNull(onm.get(ObjectName.getInstance("dom3:Type=1,Value=3")));
        assertEquals(12, onm.keySet().size());
        assertEquals(12, onm.values().size());
        assertEquals(12, onm.entrySet().size());
        assertEquals(12, onm.queryNames(ObjectName.WILDCARD, null).size());
        assertEquals(3, onm.queryNames(ObjectName.getInstance("dom2:Type=1,*"), null).size());

        assertEquals("dom2:Type=1,Value=3", onm.remove(ObjectName.getInstance("dom2:Type=1,Value=3")));
        assertEquals(11, onm.size());
        onm.clear();
        assertEquals(0, onm.size());
        assertTrue(onm.isEmpty());
    }
    
}
