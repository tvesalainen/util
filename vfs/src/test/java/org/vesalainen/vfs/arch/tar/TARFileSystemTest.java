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
package org.vesalainen.vfs.arch.tar;

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
import org.vesalainen.vfs.arch.cpio.CPIOFileSystemTest;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARFileSystemTest
{
    
    public TARFileSystemTest()
    {
    }

    @Test
    public void testReadTAR() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/test.tar");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        for (Path root : fs.getRootDirectories())
        {
            Files.walk(root).forEach((p)->System.err.println(p));
        }
        Path pom = fs.getPath("router2.xml");
        assertEquals(1032, Files.size(pom));
        byte[] readAllBytes = Files.readAllBytes(pom);
        assertEquals(1032, readAllBytes.length);
    }
    
}
