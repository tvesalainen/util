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
package org.vesalainen.vfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Root extends SinglePath
{
    private static Map<FileSystem,Root> map = new HashMap<>();
    
    Root(VirtualFileSystem fileSystem, String name)
    {
        super(fileSystem, name);
    }

    String matches(String first)
    {
        if (first.startsWith(name))
        {
            return first.substring(name.length());
        }
        return null;
    }
    @Override
    public boolean isAbsolute()
    {
        return true;
    }

    @Override
    public Path getRoot()
    {
        return this;
    }

    @Override
    public Path getFileName()
    {
        return null;
    }

    @Override
    public Path getParent()
    {
        return null;
    }

    @Override
    public int getNameCount()
    {
        return 0;
    }

    @Override
    public Path getName(int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Path subpath(int beginIndex, int endIndex)
    {
        if (beginIndex == 0 && endIndex == 0)
        {
            return SinglePath.getInstance(fileSystem, "");
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean startsWith(Path other)
    {
        checkFileSystemIsSame(other);
        if (other instanceof Root)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean endsWith(Path other)
    {
        checkFileSystemIsSame(other);
        if (other instanceof Root)
        {
            return true;
        }
        return false;
    }

    @Override
    public Path normalize()
    {
        return this;
    }

    @Override
    public URI toUri()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Path toAbsolutePath()
    {
        return this;
    }

    @Override
    public File toFile()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Path> iterator()
    {
        return Collections.emptyIterator();
    }

    @Override
    public int compareTo(Path other)
    {
        if (other instanceof Root)
        {
            return 0;
        }
        return -1;
    }
    
}
