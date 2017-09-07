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
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.vesalainen.vfs.arch.cpio.CPIOFileSystemTest;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARCPIOFileSystemReadTest extends TARCPIOTestBase
{
    
    public TARCPIOFileSystemReadTest()
    {
    }

    @Test
    public void testReadPosix() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/posix.tar.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testUserAndGroup(fs);
        testLongNames(fs);
    }
    @Test
    public void testReadUSTAR() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/ustar.tar.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testUserAndGroup(fs);
    }
    @Test
    public void testReadGNU() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/gnu.tar.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testUserAndGroup(fs);
        testLongNames(fs);
    }
    @Test
    public void testReadOldGNU() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/oldgnu.tar.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testUserAndGroup(fs);
        testLongNames(fs);
    }
    @Test
    public void testReadV7() throws URISyntaxException, IOException
    {
        URL url = CPIOFileSystemTest.class.getResource("/v7.tar.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
    }
    @Test
    public void testSVR4CPIO() throws URISyntaxException, IOException   // The new (SVR4) portable format with a checksum added.
    {
        URL url = CPIOFileSystemTest.class.getResource("/newc.cpio.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testLongNames(fs);
    }
    @Test
    public void testCRCCPIO() throws URISyntaxException, IOException   // The new (SVR4) portable format with a checksum added.
    {
        URL url = CPIOFileSystemTest.class.getResource("/crc.cpio.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testLongNames(fs);
    }
    @Test
    public void testASCIICPIO() throws URISyntaxException, IOException   // The new (SVR4) portable format with a checksum added.
    {
        URL url = CPIOFileSystemTest.class.getResource("/ascii.cpio.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testLongNames(fs);
    }
    @Test
    public void testBinaryCPIO() throws URISyntaxException, IOException   // The new (SVR4) portable format with a checksum added.
    {
        URL url = CPIOFileSystemTest.class.getResource("/bin.cpio.gz");
        Path path = Paths.get(url.toURI());
        FileSystem fs = FileSystems.newFileSystem(path, null);
        testFileSystem(fs);
        testLongNames(fs);
    }
    
}
