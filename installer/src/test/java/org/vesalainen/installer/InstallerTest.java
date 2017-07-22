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
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author tkv
 */
public class InstallerTest
{
    static final Path LOCAL = new File("localTest").toPath();
    static final Path INIT = LOCAL.resolve("init.d");
    static final Path DEFAULT = LOCAL.resolve("default");
    static final Path BIN = LOCAL.resolve("bin");
    
    public InstallerTest()
    {
    }

    @Before
    public void before() throws IOException
    {
        Files.createDirectories(LOCAL);
        Files.createDirectories(INIT);
        Files.createDirectories(DEFAULT);
        Files.createDirectories(BIN);
    }
    @After
    public void after() throws IOException
    {
        //FileUtil.deleteDirectory(LOCAL);
    }
    @Test
    public void testServer()
    {
        Installer.main("-jp", "9000", "-dd", DEFAULT.toString(), "-id", INIT.toString(), "-jd", LOCAL.toString(), "-g", "org.vesalainen.nmea", "-a", "nmea-router", "SERVER");
    }
    
    @Test
    public void testWinClient()
    {
        Installer.main("-ed", BIN.toString(), "-jd", BIN.toString(), "-g", "org.vesalainen", "-a", "installer", "CLIENT");
    }
    
}
