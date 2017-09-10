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
package org.vesalainen.vfs.attributes;

import java.nio.file.attribute.FileAttributeView;
import java.util.Collections;
import java.util.Set;
import org.vesalainen.vfs.attributes.FileAttributeName.Name;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileAttributeViewImpl implements FileAttributeView
{
    private String name;
    protected FileAttributeAccess access;

    public FileAttributeViewImpl(String name, FileAttributeAccess access)
    {
        this.name = name;
        this.access = access;
    }

    protected Object get(String name)
    {
        Name norm = FileAttributeName.getInstance(name);
        return access.get(norm, getDef(norm));
    }
    private Object getDef(Name name)
    {
        Class<?> type = FileAttributeName.type(name);
        if (Set.class.equals(type))
        {
            return Collections.EMPTY_SET;
        }
        if (Boolean.class.equals(type))
        {
            return false;
        }
        if (Integer.class.equals(type))
        {
            return Integer.valueOf(0);
        }
        if (Long.class.equals(type))
        {
            return Long.valueOf(0);
        }
        return null;
    }
    protected void put(String name, Object value)
    {
        access.put(FileAttributeName.getInstance(name), value);
    }
    protected Set<Name> names()
    {
        return access.names();
    }
    @Override
    public String name()
    {
        return name;
    }
}
