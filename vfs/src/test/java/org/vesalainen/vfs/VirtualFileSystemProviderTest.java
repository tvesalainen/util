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
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFileSystemProviderTest
{
    FileSystem fileSystem;
    public VirtualFileSystemProviderTest() throws URISyntaxException
    {
        fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.vfs:///", null, null));
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

    //@Test
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
    public void testCreate() throws URISyntaxException, IOException
    {
        Path target = fileSystem.getPath("foo/bar");
        Files.createFile(target, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr--r--")));
        assertTrue(Files.exists(target));
    }
}
