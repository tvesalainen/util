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
package org.vesalainen.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectServerTest
{
    
    public ObjectServerTest()
    {
    }

    @Test
    public void test() throws IOException, InterruptedException
    {
        int port = 12345;
        ObjectServer os = new ObjectServer(port);
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        os.put("lista", list);
        Map<Integer,String> map = new HashMap<>();
        map.put(1, "yksi");
        map.put(2, "kaksi");
        map.put(9, "yhdeksän");
        os.put("mäppi", map);
        
        os.start();
        os.waitUntilRunning();
        
        try (ObjectClient oc = ObjectClient.open("localhost", port))
        {
            Object o1 = oc.get("lista");
            assertEquals(list, o1);
            Object o2 = oc.get("mäppi");
            assertEquals(map, o2);
        }
        os.stop();
    }
    
}
