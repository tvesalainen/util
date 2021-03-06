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
package org.vesalainen.vfs;

import java.nio.file.PathMatcher;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.regex.RegexMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GlobTest
{
    
    public GlobTest()
    {
    }

    @Test
    public void test1()
    {
        Glob g = Glob.newInstance();
        assertEquals("(.*java|.*jar)", g.parse("{java,jar}"));
        assertEquals(".*\\.java", g.parse("*.java"));
        assertEquals(".*\\..*", g.parse("*.*"));
        assertEquals(".*\\.", g.parse("*."));
        assertEquals("foo\\..?", g.parse("foo.?"));
        assertEquals("/home/[^/]*/[^/]*", g.parse("/home/*/*"));
        assertEquals("/home/.*", g.parse("/home/**"));
        assertEquals("/home/[^\\-a-z]", g.parse("/home/[!-a-z]"));
    }
    
}
