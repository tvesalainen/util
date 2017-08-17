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
package org.vesalainen.util.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.vesalainen.nio.FileUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JarBuilderTest
{
    static final File LOCAL = new File("jarTest");
    
    public JarBuilderTest()
    {
    }

    @Before
    public void before() throws IOException
    {
        Files.createDirectories(LOCAL.toPath());
    }
    @After
    public void after() throws IOException
    {
        FileUtil.deleteDirectory(LOCAL);
    }
    
    @Test
    public void test1() throws IOException
    {
        File file = new File(LOCAL, "test.jar");
        try (JarBuilder jar = new JarBuilder(file);
                InputStream is = JarBuilderTest.class.getResourceAsStream("/lines_ansi.txt"))
        {
            jar.addEntry("lines_ansi.txt", is);
            jar.addEntry("text", "qwerty".getBytes());
        }
        try (JarFile jf = new JarFile(file))
        {
            assertNotNull(jf.getJarEntry("lines_ansi.txt"));
            assertNotNull(jf.getJarEntry("text"));
        }
    }
    
}
