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
package org.vesalainen.nio.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PathHelperTest
{
    
    public PathHelperTest()
    {
    }

    @Test
    public void testTransform()
    {
        Path ob = Paths.get("a", "b");
        Path nb = Paths.get("q", "w", "e");
        Path p = Paths.get("a", "b", "c", "d");
        Path exp = Paths.get("q", "w", "e", "c", "d");
        assertEquals(exp, PathHelper.transform(ob, nb, p));
    }
    @Test
    public void testPosixString()
    {
        assertEquals("foo/bar", PathHelper.posixString(PathHelper.fromPosix("foo/bar")));
        assertEquals("/foo/bar", PathHelper.posixString(PathHelper.fromPosix("/foo/bar")));
        assertEquals("./", PathHelper.posixString(PathHelper.fromPosix(".")));
    }
    
}
