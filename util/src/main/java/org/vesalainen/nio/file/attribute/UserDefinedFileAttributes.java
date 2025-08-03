/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.file.attribute;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Collection;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UserDefinedFileAttributes implements UserDefinedAttributes
{
    private final UserDefinedFileAttributeView view;
    private ByteBuffer bb;

    public UserDefinedFileAttributes(File file, int maxSize, LinkOption... options)
    {
        this(file.toPath(), maxSize, options);
    }
    public UserDefinedFileAttributes(Path path, int maxSize, LinkOption... options)
    {
        this(Files.getFileAttributeView(path, UserDefinedFileAttributeView.class, options), maxSize);
    }
    public UserDefinedFileAttributes(UserDefinedFileAttributeView view, int maxSize)
    {
        this.view = view;
        this.bb = ByteBuffer.allocate(maxSize);
    }

    @Override
    public boolean has(String name) throws IOException
    {
        return view.list().contains(name);
    }
    @Override
    public Collection<String> list() throws IOException
    {
        return view.list();
    }
    @Override
    public void deleteAll() throws IOException
    {
        for (String name : list())
        {
            delete(name);
        }
    }
    @Override
    public void delete(String name) throws IOException
    {
        view.delete(name);
    }
    @Override
    public int size(String name) throws IOException
    {
        return view.size(name);
    }
    @Override
    public int write(String name, ByteBuffer src) throws IOException
    {
        return view.write(name, src);
    }
    @Override
    public int read(String name, ByteBuffer dst) throws IOException
    {
        return view.read(name, dst);
    }
    @Override
    public void set(String name, byte[] array) throws IOException
    {
        set(name, array, 0, array.length);
    }
    @Override
    public void set(String name, byte[] array, int offset, int length) throws IOException
    {
        bb.clear();
        bb.put(array, offset, length);
        bb.flip();
        view.write(name, bb);
    }
    @Override
    public byte[] get(String name) throws IOException
    {
        byte[] arr = new byte[size(name)];
        ByteBuffer wrap = ByteBuffer.wrap(arr);
        view.read(name, wrap);
        return arr;
    }
    @Override
    public boolean arraysEquals(String name, byte[] array) throws IOException
    {
        return arraysEquals(name, array, 0, array.length);
    }
    @Override
    public boolean arraysEquals(String name, byte[] array, int offset, int length) throws IOException
    {
        if (length != size(name))
        {
            return false;
        }
        bb.clear();
        view.read(name, bb);
        bb.flip();
        for (int ii=0;ii<length;ii++)
        {
            if (array[offset+ii] != bb.get())
            {
                return false;
            }
        }
        return true;
    }
    public static boolean equals(String name, UserDefinedFileAttributes u1, UserDefinedFileAttributes u2) throws IOException
    {
        if (!u1.has(name) || !u2.has(name))
        {
            return false;
        }
        if (u1.size(name) != u2.size(name))
        {
            return false;
        }
        ByteBuffer b1 = u1.read(name);
        ByteBuffer b2 = u2.read(name);
        return b1.equals(b2);
    }
    @Override
    public void setString(String name, String value) throws IOException
    {
        set(name, value.getBytes(StandardCharsets.UTF_8));
    }
    @Override
    public void setBoolean(String name, boolean value) throws IOException
    {
        bb.clear();
        bb.put((byte) (value ? 1 : 0));
        bb.flip();
        view.write(name, bb);
    }
    @Override
    public void setInt(String name, int value) throws IOException
    {
        bb.clear();
        bb.putInt(value);
        bb.flip();
        view.write(name, bb);
    }
    @Override
    public void setLong(String name, long value) throws IOException
    {
        bb.clear();
        bb.putLong(value);
        bb.flip();
        view.write(name, bb);
    }
    @Override
    public void setDouble(String name, double value) throws IOException
    {
        bb.clear();
        bb.putDouble(value);
        bb.flip();
        view.write(name, bb);
    }
    @Override
    public String getString(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return new String(bb.array(), 0, bb.limit(), StandardCharsets.UTF_8);
    }
    @Override
    public boolean getBoolean(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return bb.get() == 1;
    }
    @Override
    public int getInt(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return bb.getInt();
    }
    @Override
    public long getLong(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return bb.getLong();
    }
    @Override
    public double getDouble(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return bb.getDouble();
    }
    private ByteBuffer read(String name) throws IOException
    {
        bb.clear();
        view.read(name, bb);
        bb.flip();
        return bb;
    }

    @Override
    public String toString()
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("UserDefinedFileAttributes{");
            for (String name : list())
            {
                sb.append(name);
                sb.append(' ');
            }
            sb.append('}');
            return sb.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

}
