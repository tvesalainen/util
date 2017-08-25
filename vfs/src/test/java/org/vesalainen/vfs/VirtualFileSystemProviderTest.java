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
import java.net.URI;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystemProviderTest
{
    FileSystem fileSystem;
    public VirtualFileSystemProviderTest() throws URISyntaxException, IOException
    {
        fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.vfs:///", null, null));
        Files.createDirectories(fileSystem.getPath("/etc/default/java"));
        Files.createDirectories(fileSystem.getPath("/usr/local/bin"));
        Files.createDirectories(fileSystem.getPath("/usr/local/lib"));
        Files.createDirectories(fileSystem.getPath("/bin"));
        Files.createDirectories(fileSystem.getPath("/var/log"));
        Files.createDirectories(fileSystem.getPath("/home/timo"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/java"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/jar"));
        Files.createFile(fileSystem.getPath("/usr/local/bin/README"));
        Files.createFile(fileSystem.getPath("/bin/bash"));
        Files.createFile(fileSystem.getPath("/bin/sh"));
        Files.createFile(fileSystem.getPath("/bin/pg"));
        Files.createFile(fileSystem.getPath("/home/timo/hello.c"));
        Files.createFile(fileSystem.getPath("/home/timo/hello.o"));
        Files.createFile(fileSystem.getPath("/home/timo/hello"));
    }

    @Test
    public void test1() throws URISyntaxException, IOException
    {
        Path cr = Paths.get("c:\\temp");
        Path temp = Paths.get("temp");
        Path parent = cr.getParent();
        int nc1 = cr.getNameCount();
        Path fn = cr.getFileName();
        Path root = cr.getRoot();
        int nc2 = root.getNameCount();
        URI uri = new URI("file:///");
        Path d = Paths.get("d:\\");
    }

    @Test
    public void testCopy() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        assertEquals(Files.size(source), Files.size(target));
        List<String> lines = Files.readAllLines(target, US_ASCII);
        assertEquals(exp, lines);
        Path target2 = fileSystem.getPath("bar");
        Files.copy(target, target2);
        List<String> lines2 = Files.readAllLines(target2, US_ASCII);
        assertEquals(exp, lines2);
    }
    @Test
    public void testCreateDirectory() throws URISyntaxException, IOException
    {
        Path target = fileSystem.getPath("foo");
        Files.createDirectory(target);
        assertTrue(Files.exists(target));
        assertTrue(Files.isDirectory(target));
        Path bar = fileSystem.getPath("foo/bar");
        Files.createFile(bar);
        try
        {
            Files.delete(target);
            fail("DirectoryNotEmptyException");
        }
        catch(DirectoryNotEmptyException ex)
        {
        }
        Files.delete(bar);
        Files.delete(target);
        assertFalse(Files.exists(target));
    }
    @Test
    public void testCreate() throws URISyntaxException, IOException
    {
        Path target = fileSystem.getPath("foo/bar");
        Files.createDirectories(target.getParent());
        Files.createFile(target);
        assertTrue(Files.exists(target));
        assertTrue(Files.isRegularFile(target));
        Files.delete(target);
        assertFalse(Files.exists(target));
    }
    @Test
    public void testCreateSymbolicLink() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path link = fileSystem.getPath("bar");
        Files.createSymbolicLink(link, target);
        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
        assertEquals(Files.size(source), Files.size(link));
        assertTrue(Files.isSymbolicLink(link));
    }
    @Test
    public void testCreateLink() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path link = fileSystem.getPath("bar");
        Files.createLink(link, target);
        assertEquals(Files.size(source), Files.size(link));
        assertTrue(Files.isSameFile(link, target));
        Files.deleteIfExists(target);
        assertFalse(Files.exists(target));
        assertTrue(Files.exists(link));
        List<String> lines = Files.readAllLines(link, US_ASCII);
        assertEquals(exp, lines);
    }
    @Test
    public void testMove() throws URISyntaxException, IOException
    {
        Path source = Paths.get("pom.xml");
        List<String> exp = Files.readAllLines(source, US_ASCII);
        Path target = fileSystem.getPath("foo");
        Files.copy(source, target);
        Path bar = fileSystem.getPath("bar");
        Files.move(target, bar);
        assertFalse(Files.exists(target));
        assertTrue(Files.exists(bar));
        assertEquals(Files.size(source), Files.size(bar));
    }
    @Test
    public void testDirectoryStream() throws URISyntaxException, IOException
    {
        List<Path> list1 = Files.list(fileSystem.getPath("/usr/local/bin")).collect(Collectors.toList());
        assertEquals(4, list1.size());
        assertEquals(fileSystem.getPath("/usr/local/bin"), list1.get(0));
        assertEquals(fileSystem.getPath("/usr/local/bin/README"), list1.get(1));
        assertEquals(fileSystem.getPath("/usr/local/bin/jar"), list1.get(2));
        assertEquals(fileSystem.getPath("/usr/local/bin/java"), list1.get(3));
        
    }
}
