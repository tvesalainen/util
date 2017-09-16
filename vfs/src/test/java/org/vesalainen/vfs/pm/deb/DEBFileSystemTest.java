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
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.vfs.VirtualFileSystems;
import org.vesalainen.vfs.pm.rpm.RPMFileSystemTest;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DEBFileSystemTest
{
    
    public DEBFileSystemTest()
    {
    }

    @Test
    public void testRead() throws URISyntaxException, IOException
    {
        URL url = DEBFileSystemTest.class.getResource("/test2_1.0-1_all.deb");
        Path path = Paths.get(url.toURI());
        try (FileSystem debFS = VirtualFileSystems.newFileSystem(path, Collections.EMPTY_MAP))
        {
            
        }
    }
    
}
