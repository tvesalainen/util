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
package org.vesalainen.pm.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PMPathTest
{
    FileSystem fs;
    
    public PMPathTest() throws URISyntaxException
    {
        fs = FileSystems.getFileSystem(new URI("org.vesalainen.pm:///", null, null));
    }

    /**
     * Test of getFileSystem method, of class PMPath.
     */
    @Test
    public void testGetFileSystem()
    {
        Path path = fs.getPath("foo", "bar");
        assertEquals(fs, path.getFileSystem());
    }

    /**
     * Test of isAbsolute method, of class PMPath.
     */
    @Test
    public void testIsAbsolute()
    {
        Path p1 = fs.getPath("foo", "bar");
        assertFalse(p1.isAbsolute());
        Path p2 = fs.getPath("/foo", "bar");
        assertTrue(p2.isAbsolute());
    }

    /**
     * Test of getRoot method, of class PMPath.
     */
    @Test
    public void testGetRoot()
    {
        Path p1 = fs.getPath("/foo", "bar");
        assertNotNull(p1.getRoot());
        Path p2 = fs.getPath("foo", "bar");
        assertNull(p2.getRoot());
    }

    /**
     * Test of getFileName method, of class PMPath.
     */
    @Test
    public void testGetFileName()
    {
    }

    /**
     * Test of getParent method, of class PMPath.
     */
    @Test
    public void testGetParent()
    {
    }

    /**
     * Test of getNameCount method, of class PMPath.
     */
    @Test
    public void testGetNameCount()
    {
    }

    /**
     * Test of getName method, of class PMPath.
     */
    @Test
    public void testGetName()
    {
    }

    /**
     * Test of subpath method, of class PMPath.
     */
    @Test
    public void testSubpath()
    {
    }

    /**
     * Test of startsWith method, of class PMPath.
     */
    @Test
    public void testStartsWith_Path()
    {
    }

    /**
     * Test of startsWith method, of class PMPath.
     */
    @Test
    public void testStartsWith_String()
    {
    }

    /**
     * Test of endsWith method, of class PMPath.
     */
    @Test
    public void testEndsWith_Path()
    {
    }

    /**
     * Test of endsWith method, of class PMPath.
     */
    @Test
    public void testEndsWith_String()
    {
    }

    /**
     * Test of normalize method, of class PMPath.
     */
    @Test
    public void testNormalize()
    {
    }

    /**
     * Test of resolve method, of class PMPath.
     */
    @Test
    public void testResolve_Path()
    {
    }

    /**
     * Test of resolve method, of class PMPath.
     */
    @Test
    public void testResolve_String()
    {
    }

    /**
     * Test of resolveSibling method, of class PMPath.
     */
    @Test
    public void testResolveSibling_Path()
    {
    }

    /**
     * Test of resolveSibling method, of class PMPath.
     */
    @Test
    public void testResolveSibling_String()
    {
    }

    /**
     * Test of relativize method, of class PMPath.
     */
    @Test
    public void testRelativize()
    {
    }

    /**
     * Test of toUri method, of class PMPath.
     */
    @Test
    public void testToUri()
    {
    }

    /**
     * Test of toAbsolutePath method, of class PMPath.
     */
    @Test
    public void testToAbsolutePath()
    {
    }

    /**
     * Test of toRealPath method, of class PMPath.
     */
    @Test
    public void testToRealPath() throws Exception
    {
    }

    /**
     * Test of toFile method, of class PMPath.
     */
    @Test
    public void testToFile()
    {
    }

    /**
     * Test of register method, of class PMPath.
     */
    @Test
    public void testRegister_3args() throws Exception
    {
    }

    /**
     * Test of register method, of class PMPath.
     */
    @Test
    public void testRegister_WatchService_WatchEventKindArr() throws Exception
    {
    }

    /**
     * Test of iterator method, of class PMPath.
     */
    @Test
    public void testIterator()
    {
    }

    /**
     * Test of compareTo method, of class PMPath.
     */
    @Test
    public void testCompareTo()
    {
    }
    
}
