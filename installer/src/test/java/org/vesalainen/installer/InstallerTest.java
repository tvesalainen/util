/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.vesalainen.nio.FileUtil;

/**
 *
 * @author tkv
 */
public class InstallerTest
{
    static final Path LOCAL = new File("localTest").toPath();
    
    public InstallerTest()
    {
    }

    @Before
    public void before() throws IOException
    {
        Files.createDirectories(LOCAL);
    }
    @After
    public void after() throws IOException
    {
        FileUtil.deleteDirectory(LOCAL);
    }
    @Test
    public void testUpdate()
    {
        Installer.main("-ed", LOCAL.toString(), "-ei", LOCAL.toString(), "-jd", LOCAL.toString(), "-g", "org.vesalainen.nmea", "-a", "router", "-v", "1.8.0", "UPDATE");
    }
    
}
