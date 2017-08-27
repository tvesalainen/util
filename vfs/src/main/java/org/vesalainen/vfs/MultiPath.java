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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiPath extends BasePath
{
    private List<Path> list = new ArrayList<>(); 
    private List<Path> names;
    
    MultiPath(VirtualFileSystem fileSystem, Root root, List<Path> lst)
    {
        super(fileSystem);
        if (root != null)
        {
            list.add(root);
            list.addAll(lst);
            names = list.subList(1, list.size());
        }
        else
        {
            list.addAll(lst);
            names = list;
        }
        this.names = Collections.unmodifiableList(names);
    }
    
    MultiPath(VirtualFileSystem fileSystem, Root root, String first, String... more)
    {
        super(fileSystem);
        if (root != null)
        {
            list.add(root);
            names = new ArrayList<>();
            add(first, more);
            list.addAll(names);
        }
        else
        {
            names = list;
            add(first, more);
        }
        names = Collections.unmodifiableList(names);
    }
    private void add(String first, String... more)
    {
        if (!first.isEmpty())
        {
            add(first);
            for (String m : more)
            {
                add(m);
            }
        }
    }
    private void add(String str)
    {
        String[] split = str.split(fileSystem.getSeparator());
        for (String s : split)
        {
            names.add(SinglePath.getInstance(fileSystem, s));
        }
    }
    @Override
    public boolean isAbsolute()
    {
        return names.size() != list.size();
    }

    @Override
    public Path getRoot()
    {
        if (isAbsolute())
        {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Path getFileName()
    {
        if (names.isEmpty())
        {
            return null;
        }
        else
        {
            return names.get(names.size()-1);
        }
    }

    @Override
    public Path getParent()
    {
        if (names.size() > 1)
        {
            return new MultiPath(fileSystem, (Root) getRoot(), names.subList(0, names.size()-1));
        }
        if (names.size() == 1 && isAbsolute())
        {
            return getRoot();
        }
        return null;
    }

    @Override
    public int getNameCount()
    {
        return names.size();
    }

    @Override
    public Path getName(int index)
    {
        return names.get(index);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex)
    {
        return new MultiPath(fileSystem, null, names.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other)
    {
        if (other instanceof BasePath)
        {
            BasePath oth = (BasePath) other;
            if (isAbsolute() == oth.isAbsolute() && getNameCount() >= other.getNameCount())
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
    public boolean endsWith(Path other)
    {
        if (other instanceof BasePath)
        {
            BasePath oth = (BasePath) other;
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
    public Path normalize()
    {
        boolean hasDots = names.stream().anyMatch((p)->SinglePath.isCurrentDirectory(p) || SinglePath.isParentDirectory(p));
        if (hasDots)
        {
            List<Path> nl = new ArrayList<>(names);
            for (int ii=0;ii<nl.size();ii++)
            {
                Path p = nl.get(ii);
                if (SinglePath.isCurrentDirectory(p))
                {
                    nl.remove(ii--);
                }
                else
                {
                    if (SinglePath.isParentDirectory(p))
                    {
                        if (ii == 0)
                        {
                            return this;
                        }
                        nl.remove(--ii);
                        nl.remove(ii--);
                    }
                }
            }
            return new MultiPath(fileSystem, (Root) getRoot(), nl);
        }
        else
        {
            return this;
        }
    }

    @Override
    public Iterator<Path> iterator()
    {
        return names.iterator();
    }

    @Override
    public int compareTo(Path other)
    {
        if (isAbsolute() == other.isAbsolute())
        {
            int count = Math.min(getNameCount(), other.getNameCount());
            for (int ii=0;ii<count;ii++)
            {
                int c = getName(ii).compareTo(other.getName(ii));
                if (c != 0)
                {
                    return c;
                }
            }
            return getNameCount() - other.getNameCount();
        }
        else
        {
            return isAbsolute() ? -1 : 1;
        }
    }

    @Override
    public String toString()
    {
        return names.stream().map((p)->p.toString()).collect(Collectors.joining("/", isAbsolute()?"/":"", ""));
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.list);
        return hash;
    }


}
