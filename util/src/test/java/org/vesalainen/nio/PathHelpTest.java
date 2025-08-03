/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PathHelpTest
{
    
    public PathHelpTest()
    {
    }

    @Test
    public void testGetTimePath()
    {
        Path exp = Paths.get("foo", "bar").resolve("20180501101112.test");
        Path got = PathHelp.getTimePath(Paths.get("foo", "bar"), "test", ZonedDateTime.of(2018, 5, 1, 10, 11, 12, 0, ZoneOffset.UTC));
        assertEquals(exp, got);
    }
    
}
