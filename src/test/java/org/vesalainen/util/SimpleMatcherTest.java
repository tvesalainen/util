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
        assertEquals(Status.Error, sm.match('z'));
        assertEquals(Status.Ok, sm.match('$'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('P'));
        assertEquals(Status.Error, sm.match('W'));
        assertEquals(Status.Error, sm.match('z'));
        assertEquals(Status.Ok, sm.match('$'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('P'));
        assertEquals(Status.Ok, sm.match('G'));
        assertEquals(Status.Ok, sm.match('L'));
        assertEquals(Status.Match, sm.match('L'));
    }
    
    @Test
    public void test2()
    {
        SimpleMatcher sm1 = new SimpleMatcher("$??GLL", StandardCharsets.US_ASCII);
        SimpleMatcher sm2 = new SimpleMatcher("$??GGA", StandardCharsets.US_ASCII);
        OrMatcher om = new OrMatcher();
        om.add(sm1);
        om.add(sm2);
        assertEquals(Status.Error, om.match('z'));
        assertEquals(Status.Ok, om.match('$'));
        assertEquals(Status.Ok, om.match('G'));
        assertEquals(Status.Ok, om.match('P'));
        assertEquals(Status.Error, om.match('W'));
        assertEquals(Status.Error, om.match('z'));
        assertEquals(Status.Ok, om.match('$'));
        assertEquals(Status.Ok, om.match('G'));
        assertEquals(Status.Ok, om.match('P'));
        assertEquals(Status.Ok, om.match('G'));
        assertEquals(Status.Ok, om.match('L'));
        assertEquals(Status.Match, om.match('L'));
    }
    
    @Test
    public void test3()
    {
        SimpleMatcher sm = new SimpleMatcher("$*GLL", StandardCharsets.US_ASCII);
        assertEquals(Status.Error, sm.match('z'));
        assertEquals(Status.Ok, sm.match('$'));
        assertEquals(Status.WillMatch, sm.match('I'));
        assertEquals(Status.WillMatch, sm.match('I'));
        assertEquals(Status.WillMatch, sm.match('W'));
        assertEquals(Status.WillMatch, sm.match('z'));
        assertEquals(Status.WillMatch, sm.match('$'));
        assertEquals(Status.WillMatch, sm.match('I'));
        assertEquals(Status.WillMatch, sm.match('I'));
        assertEquals(Status.WillMatch, sm.match('G'));
        assertEquals(Status.WillMatch, sm.match('L'));
        assertEquals(Status.Match, sm.match('L'));
    }
    
}
