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
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.vfs.VirtualFileSystems;
import org.vesalainen.vfs.pm.PackageManagerAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMFileSystemTest
{
    
    public RPMFileSystemTest()
    {
    }

    //@Test
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
        FileSystem dfs = VirtualFileSystems.getDefault();
        Path path = dfs.getPath("test.rpm");
        Files.createFile(path);
        try (FileSystem rmpFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            FileStore fs = rmpFS.getFileStores().iterator().next();
            PackageManagerAttributeView view = fs.getFileStoreAttributeView(PackageManagerAttributeView.class);
            view
                .setPackageName("test2")
                .setVersion("1.0")
                .setRelease("r1")
                .setArchitecture("noarch")
                .setDescription("description...")
                .setApplicationArea("area")
                .setLicense("GPL")
                .setOperatingSystem("linux")
                .setSummary("summary...")
                .addRequire("lsb")
                .addRequire("java7-runtime-headless")
                .setPostInstallation("echo qwerty >/tmp/test\n")
                ;
                    
            Path pom = Paths.get("pom.xml");
            Path trg = dfs.getPath("/etc/default/pom.xml");
            Files.createDirectories(trg.getParent());
            Files.copy(pom, trg);
        }
    }    
}
