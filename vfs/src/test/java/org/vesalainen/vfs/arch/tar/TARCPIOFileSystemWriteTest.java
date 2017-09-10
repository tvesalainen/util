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
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.vesalainen.nio.FileUtil;
import org.vesalainen.vfs.VirtualFileSystemProvider;
import org.vesalainen.vfs.arch.ArchiveFileSystem;
import org.vesalainen.vfs.arch.FileFormat;
import static org.vesalainen.vfs.arch.FileFormat.*;
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
    public void testWritePosix() throws IOException
    {
        testWrite("z:\\test\\posix.tar.gz", TAR_PAX);
    }
    @Test
    public void testWriteGnu() throws IOException
    {
        testWrite("z:\\test\\gnu.tar.gz", TAR_GNU);
    }
    public void testWrite(String filename, FileFormat format) throws IOException
    {
        Path path = Paths.get(filename);
        Files.deleteIfExists(path);
        Files.createFile(path);
        Map<String,Object> env = new HashMap<>();
        env.put(ArchiveFileSystem.FORMAT, format);
        FileSystem fileSystem = FileSystems.getFileSystem(VirtualFileSystemProvider.URI);
        try (FileSystem nfs = fileSystem.provider().newFileSystem(path, env))
        {
            Path root = nfs.getRootDirectories().iterator().next();
            FileUtil.copy(fsroot, root, COPY_ATTRIBUTES);
            testFileSystem(nfs);
            testUserAndGroup(nfs);
            testLongNames(nfs);
        }
        FileSystem tfs = FileSystems.newFileSystem(path, null);
        testFileSystem(tfs);
        testUserAndGroup(tfs);
        testLongNames(tfs);
    }
}
