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
package org.vesalainen.pm.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PMFileSystemProviderTest
{
    
    public PMFileSystemProviderTest()
    {
    }

    @Test
    public void test1() throws URISyntaxException
    {
        Path p = Paths.get("c:\\temp");
        Path parent = p.getParent();
        int nc1 = p.getNameCount();
        Path fn = p.getFileName();
        Path root = p.getRoot();
        int nc2 = root.getNameCount();
        URI uri = new URI("file:///");
        FileSystem fileSystem = FileSystems.getFileSystem(new URI("org.vesalainen.pm:///", null, null));
    }
    
}
