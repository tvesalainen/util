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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BasePath implements Path
{
    protected FileSystem fileSystem;

    protected BasePath(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    @Override
    public FileSystem getFileSystem()
    {
        return fileSystem;
    }

    @Override
    public boolean startsWith(String other)
    {
        return startsWith(fileSystem.getPath(other));
    }

    @Override
    public boolean endsWith(String other)
    {
        return endsWith(fileSystem.getPath(other));
    }

    @Override
    public Path resolve(String other)
    {
        return resolve(fileSystem.getPath(other));
    }

    @Override
    public Path resolve(Path other)
    {
        if (other.isAbsolute())
        {
            return other;
        }
        if (other.getNameCount() == 0)
        {
            return this;
        }
        List<Path> nl = new ArrayList<>();
        Iterator<Path> itThis = iterator();
        while (itThis.hasNext())
        {
            nl.add(itThis.next());
        }
        Iterator<Path> itOth = other.iterator();
        while (itOth.hasNext())
        {
            nl.add(itOth.next());
        }
        return new MultiPath(fileSystem, isAbsolute(), nl);
    }

    @Override
    public Path resolveSibling(String other)
    {
        return resolveSibling(fileSystem.getPath(other));
    }

    @Override
    public Path resolveSibling(Path other)
    {
        return getParent().resolve(other);  // could be more efficient!!!
    }

    @Override
    public Path relativize(Path other)
    {
        if (other.startsWith(this) && isAbsolute() == other.isAbsolute())
        {
            return other.subpath(getNameCount(), other.getNameCount());
        }
        else
        {
            throw new IllegalArgumentException(other+" cannot be relativized");
        }
    }

    @Override
    public URI toUri()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Path toAbsolutePath()
    {
        if (isAbsolute())
        {
            return this;
        }
        else
        {
            Path nor = normalize();
            List<Path> nl = new ArrayList<>();
            Iterator<Path> it = nor.iterator();
            while (it.hasNext())
            {
                nl.add(it.next());
            }
            return new MultiPath(fileSystem, true, nl);
        }
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException
    {
        return normalize();
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
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.fileSystem);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        final BasePath other = (BasePath) obj;
        if (!Objects.equals(this.fileSystem, other.fileSystem))
        {
            return false;
        }
        if (!Objects.equals(getRoot(), getRoot()))
        {
            return false;
        }
        if (getNameCount() != other.getNameCount())
        {
            return false;
        }
        int len = getNameCount();
        for (int ii=0;ii<len;ii++)
        {
            if (!getName(ii).toString().equals(other.getName(ii).toString()))
            {
                return false;
            }
        }
        return true;
    }

}
