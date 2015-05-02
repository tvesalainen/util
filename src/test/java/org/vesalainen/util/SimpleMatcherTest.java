/*
 * Copyright (C) 2015 tkv
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

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.Matcher.Status;

/**
 *
 * @author tkv
 */
public class SimpleMatcherTest
{
    
    public SimpleMatcherTest()
    {
    }

    @Test
    public void test1()
    {
        SimpleMatcher sm = new SimpleMatcher("$??GLL", StandardCharsets.US_ASCII);
        assertEquals(Status.Scan, sm.match('z'));
        assertEquals(Status.Ok, sm.match('$'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('P'));
        assertEquals(Status.Error, sm.match('W'));
        assertEquals(Status.Scan, sm.match('z'));
        assertEquals(Status.Ok, sm.match('$'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('P'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('L'));
        assertEquals(Status.Match, sm.match('L'));
    }
    
}
