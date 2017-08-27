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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
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
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher("*");
        assertTrue(fanm.any(SIZE));
        assertTrue(fanm.any(LAST_MODIFIED_TIME));
    }
    
    @Test
    public void testBasic()
    {
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher("size,lastModifiedTime,lastAccessTime");
        assertTrue(fanm.any(SIZE));
        assertFalse(fanm.any(PERMISSIONS));
        assertFalse(fanm.any(GROUP));
        assertTrue(fanm.any(LAST_MODIFIED_TIME));
        assertTrue(fanm.any(LAST_ACCESS_TIME));
    }
    
    @Test
    public void testPosix()
    {
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher("posix:*");
        assertTrue(fanm.any(SIZE));
        assertTrue(fanm.any(PERMISSIONS));
        assertTrue(fanm.any(GROUP));
        assertTrue(fanm.any(LAST_MODIFIED_TIME));
    }
    
    @Test
    public void testPosix2()
    {
        FileAttributeNameMatcher fanm = new FileAttributeNameMatcher("posix:permissions,owner,size");
        assertTrue(fanm.any(SIZE));
        assertTrue(fanm.any(PERMISSIONS));
        assertTrue(fanm.any(OWNER));
        assertFalse(fanm.any(LAST_MODIFIED_TIME));
    }
    
}
