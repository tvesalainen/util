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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OpenTypeAppendableTest
{
    
    public OpenTypeAppendableTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[]{"foo", "bar"};
        OpenTypeAppendable.append(sb, arr.getClass(), arr);
        assertEquals("<div><div>foo</div><div>bar</div></div>", sb.toString());
    }
    //@Test
    public void test2() throws IOException, MalformedObjectNameException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        MBeanServer pbs = ManagementFactory.getPlatformMBeanServer();
        System.gc();
        Object value = pbs.getAttribute(ObjectName.getInstance("java.lang:type=GarbageCollector,name=PS MarkSweep"), "LastGcInfo");
        StringBuilder sb = new StringBuilder();
        OpenTypeAppendable.append(sb, value.getClass(), value);
        assertEquals("", sb.toString());
    }
    
}
