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
package org.vesalainen.vfs.arch.cpio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
public class CPIOFileSystemTest
{
    
    public CPIOFileSystemTest()
    {
    }

    @Test
    public void testReadCPIO() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/lsb.cpio");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        for (Path root : fs.getRootDirectories())
        {
            Files.walk(root).forEach((p)->System.err.println(p));
        }
        Path pom = fs.getPath("opt/org.vesalainen/foo/pom.xml");
        assertEquals(1032, Files.size(pom));
        byte[] readAllBytes = Files.readAllBytes(pom);
        assertEquals(1032, readAllBytes.length);
    }
    @Test
    public void testReadCPIOGZ() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/lsb.cpio.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        for (Path root : fs.getRootDirectories())
        {
            Files.walk(root).forEach((p)->System.err.println(p));
        }
        Path pom = fs.getPath("opt/org.vesalainen/foo/pom.xml");
        assertEquals(1032, Files.size(pom));
        byte[] readAllBytes = Files.readAllBytes(pom);
        assertEquals(1032, readAllBytes.length);
    }
    //@Test
    public void testWriteCPIO() throws URISyntaxException, IOException
    {
        Path path = Paths.get("z:\\writeTest.cpio");
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (FileSystem fs = FileSystems.newFileSystem(path, null))
        {
            Path lpom = Paths.get("pom.xml");
            Path cpom = fs.getPath("pom.xml");
            Files.copy(lpom, cpom);
        }
        assertTrue(Files.exists(path));
    }
    //@Test
    public void testWriteCPIOGZ() throws URISyntaxException, IOException
    {
        Path path = Paths.get("z:\\writeTest.cpio.gz");
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (FileSystem fs = FileSystems.newFileSystem(path, null))
        {
            Path lpom = Paths.get("pom.xml");
            Path cpom = fs.getPath("pom.xml");
            Files.copy(lpom, cpom);
        }
        assertTrue(Files.exists(path));
    }
    
}
