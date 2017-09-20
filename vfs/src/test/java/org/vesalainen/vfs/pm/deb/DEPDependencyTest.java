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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.ArrayHelp;
import static org.vesalainen.vfs.pm.Condition.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEPDependencyTest
{
    
    public DEPDependencyTest()
    {
    }

    @Test
    public void test1()
    {
        DEPDependency d = new DEPDependency("libc6 (>= 2.2.1)");
        assertEquals("libc6", d.getName());
        assertEquals("2.2.1", d.getVersion());
        assertTrue(ArrayHelp.contains(d.getConditions(), GREATER));
        assertTrue(ArrayHelp.contains(d.getConditions(), EQUAL));
        assertEquals("libc6 (>= 2.2.1)", d.toString());
    }
    
}
