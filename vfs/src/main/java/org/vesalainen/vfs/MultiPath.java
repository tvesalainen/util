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
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiPath extends BasePath
{
    private static final Map<VirtualFileSystem,Map<List<Path>,MultiPath>> cache = new WeakHashMap<>();
    
    private List<Path> list = new ArrayList<>(); 
    private List<Path> names;
    private String toString;
    private Path parent;
    private final boolean absolute;
    private Root root;
    private Path filename;
    
    private MultiPath(VirtualFileSystem fileSystem, List<Path> lst)
    {
        super(fileSystem);
        if (!lst.isEmpty() && lst.get(0).isAbsolute())
        {
            list.addAll(lst);
            names = list.subList(1, list.size());
        }
        else
        {
            list.addAll(lst);
            names = list;
        }
        this.names = Collections.unmodifiableList(names);
        // absolute
        absolute = names.size() != list.size();;
        // root
        if (absolute)
        {
            root = (Root) list.get(0);
        }
        // parent
        if (names.size() > 1)
        {
            parent = getInstance(fileSystem, list.subList(0, list.size()-1));
        }
        if (names.size() == 1 && absolute)
        {
            parent = root;
        }
        // filename
        if (names.isEmpty())
        {
            filename = null;
        }
        else
        {
            filename = names.get(names.size()-1);
        }
    }
    static final MultiPath getInstance(VirtualFileSystem fileSystem, Root root, String first, String... more)
    {
        List<Path> list = new ArrayList<>();
        if (root != null)
        {
            list.add(root);
        }
        add(fileSystem, list, first, more);
        return getInstance(fileSystem, list);
    }
    static final MultiPath getInstance(VirtualFileSystem fileSystem, List<Path> lst)
    {
        Map<List<Path>,MultiPath> map = cache.get(fileSystem);
        if (map == null)
        {
            map = new WeakHashMap<>();
            cache.put(fileSystem, map);
        }
        MultiPath mp = map.get(lst);
        if (mp == null)
        {
            mp = new MultiPath(fileSystem, lst);
            map.put(lst, mp);
        }
        return mp;
    }
    private static void add(VirtualFileSystem fileSystem, List<Path> names, String first, String... more)
    {
        if (!first.isEmpty())
        {
            add(fileSystem, names, first);
            for (String m : more)
            {
                add(fileSystem, names, m);
            }
        }
    }
    private static void add(VirtualFileSystem fileSystem, List<Path> names, String str)
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
        return absolute;
    }

    @Override
    public Path getRoot()
    {
        return root;
    }

    @Override
    public Path getFileName()
    {
        return filename;
    }

    @Override
    public Path getParent()
    {
        return parent;
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
        return getInstance(fileSystem, names.subList(beginIndex, endIndex));
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
            List<Path> nl = new ArrayList<>(list);
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
            return getInstance(fileSystem, nl);
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
        if (toString == null)
        {
            toString = names.stream().map((p)->p.toString()).collect(Collectors.joining("/", isAbsolute()?"/":"", ""));
        }
        return toString;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.list);
        return hash;
    }


}
