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
package org.vesalainen.vfs.pm.deb;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.ArrayHelp;
import org.vesalainen.vfs.pm.Condition;
import static org.vesalainen.vfs.pm.Condition.*;
import org.vesalainen.vfs.pm.Dependency;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEBDependencyTest
{
    
    public DEBDependencyTest()
    {
    }

    @Test
    public void test1()
    {
        String text = "foo (>= 1.2), libbar1 (= 1.3.4)";
        List<Dependency> list = DEBDependency.parse(text);
        assertEquals(2, list.size());
        Dependency foo = list.get(0);
        assertEquals("foo (>= 1.2)", foo.toString());
        assertEquals("foo", foo.getName());
        assertEquals("1.2", foo.getVersion());
        Condition[] conditionsFoo = foo.getConditions();
        assertEquals(2, conditionsFoo.length);
        assertTrue(ArrayHelp.contains(conditionsFoo, GREATER));
        assertTrue(ArrayHelp.contains(conditionsFoo, EQUAL));
        
        Dependency lib = list.get(1);
        assertEquals("libbar1 (= 1.3.4)", lib.toString());
        assertEquals("libbar1", lib.getName());
        assertEquals("1.3.4", lib.getVersion());
        Condition[] conditionsLib = lib.getConditions();
        assertEquals(1, conditionsLib.length);
        assertTrue(ArrayHelp.contains(conditionsLib, EQUAL));
    }
    
}
