/*
 * Copyright (C) 2016 tkv
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
 * @author tkv
 */
public class CamelCaseTest
{
    
    public CamelCaseTest()
    {
    }

    @Test
    public void testCamelCase()
    {
        assertEquals("CamelCaseTest", CamelCase.camelCase("   CamelCaseTest   "));
        assertEquals("CamelCaseTest", CamelCase.camelCase("   Camel  Case  Test   "));
        assertEquals("CamelCaseTest", CamelCase.camelCase("camel case test"));
        assertEquals("CamelCaseTest", CamelCase.camelCase("camel123- case - test"));
    }
    
    @Test
    public void testDelimited()
    {
        assertEquals("Camel Case Test", CamelCase.title("   CamelCaseTest   "));
        assertEquals("camel-case-test", CamelCase.delimited("   CamelCaseTest   ", "-"));
    }
    @Test
    public void testProperty()
    {
        assertEquals("camelCaseTest", CamelCase.property("   CamelCaseTest   "));
    }
}
