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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PMFileSystemProviderTest
{
    FileSystem fileSystem;
    public PMFileSystemProviderTest() throws URISyntaxException
    {
        fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.pm:///", null, null));
    }

    @Test
    public void test1() throws URISyntaxException
    {
        Path p = Paths.get("c:\\temp");
        Path parent = p.getParent();
        int nc1 = p.getNameCount();
        Path fn = p.getFileName();
        Path root = p.getRoot();
        int nc2 = root.getNameCount();
        URI uri = new URI("file:///");
    }

    @Test
    public void test2() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
    }
    /**
     * Test of getScheme method, of class PMFileSystemProvider.
     */
    @Test
    public void testGetScheme()
    {
    }

    /**
     * Test of newFileSystem method, of class PMFileSystemProvider.
     */
    @Test
    public void testNewFileSystem() throws Exception
    {
    }

    /**
     * Test of getFileSystem method, of class PMFileSystemProvider.
     */
    @Test
    public void testGetFileSystem()
    {
    }

    /**
     * Test of getPath method, of class PMFileSystemProvider.
     */
    @Test
    public void testGetPath()
    {
    }

    /**
     * Test of newByteChannel method, of class PMFileSystemProvider.
     */
    @Test
    public void testNewByteChannel() throws Exception
    {
    }

    /**
     * Test of newDirectoryStream method, of class PMFileSystemProvider.
     */
    @Test
    public void testNewDirectoryStream() throws Exception
    {
    }

    /**
     * Test of createDirectory method, of class PMFileSystemProvider.
     */
    @Test
    public void testCreateDirectory() throws Exception
    {
    }

    /**
     * Test of delete method, of class PMFileSystemProvider.
     */
    @Test
    public void testDelete() throws Exception
    {
    }

    /**
     * Test of copy method, of class PMFileSystemProvider.
     */
    @Test
    public void testCopy() throws Exception
    {
    }

    /**
     * Test of move method, of class PMFileSystemProvider.
     */
    @Test
    public void testMove() throws Exception
    {
    }

    /**
     * Test of isSameFile method, of class PMFileSystemProvider.
     */
    @Test
    public void testIsSameFile() throws Exception
    {
    }

    /**
     * Test of isHidden method, of class PMFileSystemProvider.
     */
    @Test
    public void testIsHidden() throws Exception
    {
    }

    /**
     * Test of getFileStore method, of class PMFileSystemProvider.
     */
    @Test
    public void testGetFileStore() throws Exception
    {
    }

    /**
     * Test of checkAccess method, of class PMFileSystemProvider.
     */
    @Test
    public void testCheckAccess() throws Exception
    {
    }

    /**
     * Test of getFileAttributeView method, of class PMFileSystemProvider.
     */
    @Test
    public void testGetFileAttributeView()
    {
    }

    /**
     * Test of readAttributes method, of class PMFileSystemProvider.
     */
    @Test
    public void testReadAttributes_3args_1() throws Exception
    {
    }

    /**
     * Test of readAttributes method, of class PMFileSystemProvider.
     */
    @Test
    public void testReadAttributes_3args_2() throws Exception
    {
    }

    /**
     * Test of setAttribute method, of class PMFileSystemProvider.
     */
    @Test
    public void testSetAttribute() throws Exception
    {
    }
    
}
