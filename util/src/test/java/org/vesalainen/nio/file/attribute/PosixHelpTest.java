/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.nio.file.attribute;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class PosixHelpTest
{
    
    public PosixHelpTest()
    {
    }

    @Test
    public void test1()
    {
        assertEquals("rwxr--r--", PosixHelp.toString(0744));
        assertEquals("rwxrwxrwx", PosixHelp.toString(0777));
        assertEquals(0744, PosixHelp.getMode("rwxr--r--"));
        assertEquals(0777, PosixHelp.getMode("rwxrwxrwx"));
    }
    
}
