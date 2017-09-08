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
import static java.nio.file.StandardCopyOption.*;
import org.junit.Test;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.vfs.arch.cpio.CPIOFileSystemTest;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARCPIOFileSystemWriteTest extends TARCPIOTestBase
{
    private FileSystem fs;
    private final Path fsroot;
    
    public TARCPIOFileSystemWriteTest() throws IOException, URISyntaxException
    {
        URL url = CPIOFileSystemTest.class.getResource("/posix.tar.gz");
        Path path = Paths.get(url.toURI());
        fs = FileSystems.newFileSystem(path, null);
        fsroot = fs.getRootDirectories().iterator().next();
        testFileSystem(fs);
        testUserAndGroup(fs);
        testLongNames(fs);
    }

    @Test
    public void test0() throws URISyntaxException, IOException
    {
        int ceil = (int) Math.log10(100);
    }
    @Test
    public void testWritePosix() throws URISyntaxException, IOException
    {
        Path path = Paths.get("z:\\test\\posix.tar.gz");
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (FileSystem nfs = FileSystems.newFileSystem(path, null))
        {
            Path root = nfs.getRootDirectories().iterator().next();
            FileUtil.copy(fsroot, root, COPY_ATTRIBUTES);
            testFileSystem(nfs);
            testUserAndGroup(nfs);
            testLongNames(nfs);
        }
    }
}
