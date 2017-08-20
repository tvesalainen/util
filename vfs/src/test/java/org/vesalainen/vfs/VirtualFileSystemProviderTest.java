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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        assertEquals(Files.size(source), Files.size(target));
        byte[] readAllBytes = Files.readAllBytes(target);
        List<String> list = Files.readAllLines(target, US_ASCII);
    }
}
