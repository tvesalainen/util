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

import java.nio.ByteBuffer;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.FileAttributeName;
import org.vesalainen.vfs.attributes.FileAttributeName.Name;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileAttributeAccessStore implements FileAttributeAccess
{
    protected Map<FileAttributeName.Name,Object> attributes;
    
    public FileAttributeAccessStore()
    {
        this(new HashMap<>());
    }

    public FileAttributeAccessStore(Map<Name, Object> attributes)
    {
        this.attributes = attributes;
    }

    public void clear()
    {
        attributes.clear();
    }
    
    public void addAll(Map<String,Object> map)
    {
        for (Entry<String,Object> entry : map.entrySet())
        {
            attributes.put(FileAttributeName.getInstance(entry.getKey()), entry.getValue());
        }
    }
    @Override
    public Object get(FileAttributeName.Name name, Object def)
    {
        return attributes.getOrDefault(name, def);
    }

    @Override
    public Set<FileAttributeName.Name> names()
    {
        return attributes.keySet();
    }

    @Override
    public void put(FileAttributeName.Name name, Object value)
    {
        if (FileAttributeName.USER_VIEW.equals(name) && (value instanceof ByteBuffer))
        {
            ByteBuffer bb = (ByteBuffer) value;
            byte[] arr = new byte[bb.remaining()];
            bb.get(arr);
            attributes.put(name, arr);
        }
        else
        {
            attributes.put(name, value);
        }
    }

    @Override
    public void delete(FileAttributeName.Name name)
    {
        attributes.remove(name);
    }
    
    public FileAttribute<?>[] fileAttributes()
    {
        FileAttribute[] array = new FileAttribute[attributes.size()];
        int index = 0;
        for (Entry<Name, Object> entry : attributes.entrySet())
        {
            array[index++] = new FileAttributeImpl(entry);
        }
        return array;
    }
    
    private static class FileAttributeImpl implements FileAttribute
    {
        private Name name;
        private Object value;

        public FileAttributeImpl(Entry<Name,Object> entry)
        {
            this.name = entry.getKey();
            this.value = entry.getValue();
        }
        
        @Override
        public String name()
        {
            return name.toString();
        }

        @Override
        public Object value()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return "FileAttribute{" + name + " : " + value + '}';
        }
        
    }
}
