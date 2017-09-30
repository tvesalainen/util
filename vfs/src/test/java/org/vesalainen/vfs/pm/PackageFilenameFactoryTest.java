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
package org.vesalainen.vfs.pm;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.vfs.VirtualFileSystems;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PackageFilenameFactoryTest
{
    private FileSystem fs;
    private Path dir;
    public PackageFilenameFactoryTest()
    {
        FileSystem fs = VirtualFileSystems.getDefault();
        dir = fs.getPath("/");
    }

    @Test
    public void testDeb()
    {
        String type = "deb";
        String packageName = "pkg";
        String version = "0.9";
        int release = 2;
        String architecture = "linux";
        Path path = PackageFilenameFactory.getPath(dir, type, packageName, version, release, architecture);
        PackageFilename fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(packageName, fn.getPackage());
        assertEquals(version, fn.getVersion());
        assertEquals(release, fn.getRelease());
        assertEquals(architecture, fn.getArchitecture());
    }
    @Test
    public void testRpm()
    {
        String type = "rpm";
        String packageName = "pkg";
        String version = "0.9";
        int release = 2;
        String architecture = "linux";
        Path path = PackageFilenameFactory.getPath(dir, type, packageName, version, release, architecture);
        PackageFilename fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(packageName, fn.getPackage());
        assertEquals(version, fn.getVersion());
        assertEquals(release, fn.getRelease());
        assertEquals(architecture, fn.getArchitecture());
    }
    @Test
    public void testDebRelease() throws IOException
    {
        String type = "deb";
        String packageName = "pkg";
        String version = "0.9";
        String architecture = "linux";
        Path path = PackageFilenameFactory.getPath(dir, type, packageName, version, architecture);
        PackageFilename fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(packageName, fn.getPackage());
        assertEquals(version, fn.getVersion());
        assertEquals(1, fn.getRelease());
        assertEquals(architecture, fn.getArchitecture());
        Files.createFile(path);
        path = PackageFilenameFactory.getPath(dir, type, packageName, version, architecture);
        fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(2, fn.getRelease());
    }
    @Test
    public void testRpmRelease() throws IOException
    {
        String type = "rpm";
        String packageName = "pkg";
        String version = "0.9";
        String architecture = "linux";
        Path path = PackageFilenameFactory.getPath(dir, type, packageName, version, architecture);
        PackageFilename fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(packageName, fn.getPackage());
        assertEquals(version, fn.getVersion());
        assertEquals(1, fn.getRelease());
        assertEquals(architecture, fn.getArchitecture());
        Files.createFile(path);
        path = PackageFilenameFactory.getPath(dir, type, packageName, version, architecture);
        fn = PackageFilenameFactory.getInstance(path);
        assertTrue(fn.isValid());
        assertEquals(2, fn.getRelease());
    }
    
}
