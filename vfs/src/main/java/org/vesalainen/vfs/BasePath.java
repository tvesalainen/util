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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BasePath implements Path
{
    protected VirtualFileSystem fileSystem;

    protected BasePath(VirtualFileSystem fileSystem)
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
        other = checkFileSystem(other);
        if (other.isAbsolute())
        {
            return other;
        }
        if (other.getNameCount() == 0)
        {
            return this;
        }
        List<Path> nl = new ArrayList<>();
        if (isAbsolute())
        {
            nl.add(getRoot());
        }
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
        return MultiPath.getInstance(fileSystem, nl);
    }

    @Override
    public Path resolveSibling(String other)
    {
        return resolveSibling(fileSystem.getPath(other));
    }

    @Override
    public Path resolveSibling(Path other)
    {
        return getParent().resolve(other);
    }

    @Override
    public Path relativize(Path other)
    {
        checkFileSystemIsSame(other);
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
        Path nor = normalize();
        if (nor.isAbsolute())
        {
            return nor;
        }
        else
        {
            List<Path> nl = new ArrayList<>();
            nl.add(fileSystem.getDefaultRoot());
            Iterator<Path> it = nor.iterator();
            while (it.hasNext())
            {
                nl.add(it.next());
            }
            return MultiPath.getInstance(fileSystem, nl);
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

    protected void checkFileSystemIsSame(Path path)
    {
        if (!fileSystem.equals(path.getFileSystem()))
        {
            throw new UnsupportedOperationException("mixing filesystems not supported");
        }
    }
    public static final void checkFileSystemIsSame(Path p1, Path p2)
    {
        Objects.requireNonNull(p1);
        Objects.requireNonNull(p2);
        if (p1.getFileSystem() != p2.getFileSystem())
        {
            throw new UnsupportedOperationException("mixing filesystems not supported");
        }
    }

    private Path checkFileSystem(Path other)
    {
        if (fileSystem.equals(other.getFileSystem()))
        {
            return other;
        }
        if (other.isAbsolute())
        {
            throw new UnsupportedOperationException("importing absolute path from another file system not supported");
        }
        switch (other.getNameCount())
        {
            case 0:
                return fileSystem.getPath("");
            case 1:
                return fileSystem.getPath(other.getName(0).toString());
            default:
                String first = other.getName(0).toString();
                String[] more = new String[other.getNameCount()-1];
                for (int ii=0;ii<more.length;ii++)
                {
                    more[ii] = other.getName(ii+1).toString();
                }
                return fileSystem.getPath(first, more);
                
        }
    }
}
