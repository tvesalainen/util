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

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.attributes.FileAttributeName.FileAttributeNameMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileAttributeNameTest
{
    
    public FileAttributeNameTest()
    {
    }

    @Test
    public void testStar()
    {
        Set<String> views = new HashSet<>();
        views.add("basic");
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher(views, "*");
        assertTrue(fanm.any(SIZE));
        assertTrue(fanm.any(LAST_MODIFIED_TIME));
    }
    
    @Test
    public void testPosix()
    {
        Set<String> views = new HashSet<>();
        views.add("basic");
        views.add("posix");
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher(views, "posix:*,lastModifiedTime,lastAccessTime");
        assertFalse(fanm.any(SIZE));
        assertTrue(fanm.any(PERMISSIONS));
        assertTrue(fanm.any(GROUP));
        assertTrue(fanm.any(LAST_MODIFIED_TIME));
    }
    
}
