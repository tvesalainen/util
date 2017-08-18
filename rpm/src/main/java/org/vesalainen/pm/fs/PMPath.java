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
import java.util.stream.Collectors;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PMPath implements Path
{
    private FileSystem fileSystem;
    private String name;
    private List<Path> list;
    private boolean absolute;
    private PMPath root;

    PMPath(FileSystem fileSystem, String name)
    {
        this.fileSystem = fileSystem;
        this.name = name;
    }

    PMPath(FileSystem fileSystem, boolean absolute)
    {
        this.fileSystem = fileSystem;
        this.absolute = absolute;
    }

    PMPath(FileSystem fileSystem, boolean absolute, List<Path> list)
    {
        this.fileSystem = fileSystem;
        this.absolute = absolute;
        this.list = list;
    }
    
    PMPath(FileSystem fileSystem, String first, String... more)
    {
        this.fileSystem = fileSystem;
        this.list = new ArrayList<>();
        if (first.startsWith("/"))
        {
            absolute = true;
            list.add(new PMPath(fileSystem, first.substring(1)));
        }
        else
        {
            list.add(new PMPath(fileSystem, first));
        }
        for (String m : more)
        {
            list.add(new PMPath(fileSystem, m));
        }
    }
    
    @Override
    public FileSystem getFileSystem()
    {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute()
    {
        return absolute;
    }

    @Override
    public Path getRoot()
    {
        if (absolute)
        {
            if (root == null)
            {
                root = new PMPath(fileSystem, absolute);
            }
        }
        return root;
    }

    @Override
    public Path getFileName()
    {
        if (list.isEmpty())
        {
            return null;
        }
        else
        {
            return list.get(list.size()-1);
        }
    }

    @Override
    public Path getParent()
    {
        if (list.size() > 1)
        {
            return new PMPath(fileSystem, absolute, list.subList(0, list.size()-1));
        }
        if (list.size() == 1 && absolute)
        {
            return getRoot();
        }
        return null;
    }

    @Override
    public int getNameCount()
    {
        return list.size();
    }

    @Override
    public Path getName(int index)
    {
        return list.get(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex)
    {
        return new PMPath(fileSystem, false, list.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other)
    {
        if (other instanceof PMPath)
        {
            PMPath oth = (PMPath) other;
            if (absolute == oth.absolute && getNameCount() >= other.getNameCount())
            {
                int lim = oth.getNameCount();
                for (int ii=0;ii<lim;ii++)
                {
                    if (!getName(ii).equals(oth.getName(ii)))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean startsWith(String other)
    {
        if (!list.isEmpty())
        {
            return list.get(0).equals(other);
        }
        return false;
    }

    @Override
    public boolean endsWith(Path other)
    {
        if (other instanceof PMPath)
        {
            PMPath oth = (PMPath) other;
            if (getNameCount() >= other.getNameCount())
            {
                int lim = oth.getNameCount();
                int off = getNameCount() - other.getNameCount();
                for (int ii=0;ii<lim;ii++)
                {
                    if (!getName(ii+off).equals(oth.getName(ii)))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean endsWith(String other)
    {
        if (!list.isEmpty())
        {
            return list.get(list.size()-1).equals(other);
        }
        return false;
    }

    @Override
    public Path normalize()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path resolve(Path other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path resolve(String other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path resolveSibling(Path other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path resolveSibling(String other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path relativize(Path other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URI toUri()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path toAbsolutePath()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File toFile()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Path> iterator()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Path other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString()
    {
        if (name != null)
        {
            return name;
        }
        else
        {
            return list.stream().map((p)->p.toString()).collect(Collectors.joining("/", absolute?"/":"", ""));
        }
    }
    
}
