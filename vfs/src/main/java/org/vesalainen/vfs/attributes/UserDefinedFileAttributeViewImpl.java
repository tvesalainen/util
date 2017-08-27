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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.vfs.attributes.FileAttributeName.Name;
import static org.vesalainen.vfs.attributes.FileAttributeName.USER_VIEW;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UserDefinedFileAttributeViewImpl extends FileAttributeViewImpl implements UserDefinedFileAttributeView
{

    public UserDefinedFileAttributeViewImpl(FileAttributeAccess access)
    {
        super("user", access);
    }

    @Override
    public List<String> list() throws IOException
    {
        List<String> list = new ArrayList<>();
        for (Name name : names())
        {
            if (USER_VIEW.equals(name.getView()))
            {
                list.add(name.getName());
            }
        }
        return list;
    }

    @Override
    public int size(String name) throws IOException
    {
        byte [] arr = (byte[]) get("user:"+name);
        return arr != null ? arr.length : 0;
    }

    @Override
    public int read(String name, ByteBuffer dst) throws IOException
    {
        byte [] arr = (byte[]) get("user:"+name);
        if (arr != null)
        {
            dst.put(arr);
            return arr.length;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public int write(String name, ByteBuffer src) throws IOException
    {
        byte[] arr = new byte[src.remaining()];
        src.get(arr);
        put("user:"+name, arr);
        return arr.length;
    }

    @Override
    public void delete(String name) throws IOException
    {
        delete("user:"+name);
    }
    
}
