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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SinglePath extends BasePath
{
    private String name;
    private final List<Path> singleton;

    public SinglePath(FileSystem fileSystem, String name)
    {
        super(fileSystem);
        this.name = name;
        this.singleton = Collections.singletonList(this);
    }

    static final boolean isEmpty(Path path)
    {
        return contentIs(path, "");
    }

    static final boolean isCurrentDirectory(Path path)
    {
        return contentIs(path, ".");
    }

    static boolean isParentDirectory(Path path)
    {
        return contentIs(path, "..");
    }

    private static boolean contentIs(Path path, String content)
    {
        return !path.isAbsolute() && path.getNameCount()==1 && content.equals(path.toString());
    }

    @Override
    public boolean isAbsolute()
    {
        return false;
    }

    @Override
    public Path getRoot()
    {
        return null;
    }

    @Override
    public Path getFileName()
    {
        return this;
    }

    @Override
    public Path getParent()
    {
        return null;
    }

    @Override
    public int getNameCount()
    {
        return 1;
    }

    @Override
    public Path getName(int index)
    {
        if (index != 0)
        {
            throw new IllegalArgumentException(index+" out of range");
        }
        return this;
    }

    @Override
    public Path subpath(int beginIndex, int endIndex)
    {
        if (beginIndex != 0 && endIndex != 1)
        {
            throw new IllegalArgumentException("out of range");
        }
        return this;
    }

    @Override
    public boolean startsWith(Path other)
    {
        if (!other.isAbsolute() && other.getNameCount() == 1)
        {
            return equals(other.getName(0));
        }
        return false;
    }

    @Override
    public boolean endsWith(Path other)
    {
        return startsWith(other);
    }

    @Override
    public Path normalize()
    {
        if (isCurrentDirectory(this))
        {
            return new SinglePath(fileSystem, "");
        }
        else
        {
            return this;
        }
    }

    @Override
    public Iterator<Path> iterator()
    {
        return singleton.iterator();
    }

    @Override
    public int compareTo(Path other)
    {
        if (other instanceof SinglePath)
        {
            SinglePath sp = (SinglePath) other;
            return name.compareTo(sp.name);
        }
        return 1;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public String toString()
    {
        return name;
    }
    
}
