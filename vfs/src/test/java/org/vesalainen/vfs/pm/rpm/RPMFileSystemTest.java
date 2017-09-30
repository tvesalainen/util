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
package org.vesalainen.vfs.pm.rpm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.vfs.VirtualFileSystems;
import org.vesalainen.vfs.pm.FileUse;
import org.vesalainen.vfs.pm.PackageFileAttributes;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMFileSystemTest
{
    
    public RPMFileSystemTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.ALL);
    }

    @Test
    public void testRead() throws URISyntaxException, IOException
    {
        URL url = RPMFileSystemTest.class.getResource("/redhat-lsb-4.0-2.1.4.el5.i386.rpm");
        Path path = Paths.get(url.toURI());
        try (FileSystem rmpFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            
        }
    }
    @Test
    public void testWrite() throws URISyntaxException, IOException
    {
        long pomSize = 0;
        FileSystem dfs = VirtualFileSystems.getDefault();
        Path path = dfs.getPath("test-1.0-1.noarch.rpm");
        Files.createFile(path);
        try (FileSystem rmpFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            FileStore fs = rmpFS.getFileStores().iterator().next();
            PackageManagerAttributeView view = fs.getFileStoreAttributeView(PackageManagerAttributeView.class);
            view
                .setDescription("description...")
                .setApplicationArea("area")
                .setLicense("GPL")
                .setSummary("summary...")
                .addRequire("lsb")
                .addRequire("java7-runtime-headless")
                .setPostInstallation("echo qwerty >/tmp/test\n")
                ;
                    
            Path pom = Paths.get("pom.xml");
            pomSize = Files.size(pom);
            Path trg = rmpFS.getPath("/etc/default/pom.xml");
            Files.createDirectories(trg.getParent());
            Files.copy(pom, trg);
            PackageFileAttributes.setLanguage(trg, "C");
            PackageFileAttributes.setUsage(trg, FileUse.CONFIGURATION);
        }
        try (FileSystem rmpFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            FileStore fs = rmpFS.getFileStores().iterator().next();
            PackageManagerAttributeView view = fs.getFileStoreAttributeView(PackageManagerAttributeView.class);
            assertEquals("test", view.getPackageName());
            assertEquals("1.0", view.getVersion());
            assertEquals("1", view.getRelease());
            assertEquals("noarch", view.getArchitecture());
            assertEquals("description...", view.getDescription());
            assertEquals("area", view.getApplicationArea());
            assertEquals("GPL", view.getLicense());
            assertEquals("linux", view.getOperatingSystem());
            assertEquals("summary...", view.getSummary());
            Collection<String> requires = view.getRequires();
            assertTrue(requires.contains("lsb"));
            assertTrue(requires.contains("java7-runtime-headless"));
            assertEquals("echo qwerty >/tmp/test\n", view.getPostInstallation());
            
            Path trg = rmpFS.getPath("/etc/default/pom.xml");
            assertEquals(pomSize, Files.size(trg));
            assertEquals("C", PackageFileAttributes.getLanguage(trg));
            Set<FileUse> usage = PackageFileAttributes.getUsage(trg);
            assertTrue(usage.contains(FileUse.CONFIGURATION));
            assertFalse(usage.contains(FileUse.DOCUMENTATION));
        }
        Path rpm = Paths.get("z:\\test\\test.rpm");
        //Files.copy(path, rpm);
    }    
}
