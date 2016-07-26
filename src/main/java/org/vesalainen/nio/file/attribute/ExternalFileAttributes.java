/*
 * Copyright (C) 2016 tkv
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author tkv
 */
public class ExternalFileAttributes implements UserDefinedAttributes, Serializable
{
    public static final String Suffix = ".atr";
    private static final long serialVersionUID = 1L;
    private Map<String,byte[]> map;
    private File file;

    ExternalFileAttributes()
    {
        map = new HashMap<>();
    }

    public ExternalFileAttributes(File file) throws IOException
    {
        this.file = getAttributeFile(file);
        if (this.file.exists())
        {
            try (FileInputStream fis = new FileInputStream(this.file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);)
            {
                map = (Map<String, byte[]>) ois.readObject();
            }
            catch (ClassNotFoundException ex)
            {
                throw new IOException(ex);
            }
        }
        else
        {
            map = new HashMap<>();
        }
    }

    public static final File getAttributeFile(File file)
    {
        return new File(file.getAbsolutePath()+Suffix);
    }
    private void store() throws IOException
    {
        if (file != null)
        {
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 ObjectOutputStream oos = new ObjectOutputStream(bos);)
            {
                oos.writeObject(map);
            }
        }
    }
    @Override
    public boolean arraysEquals(String name, byte[] array) throws IOException
    {
        return Arrays.equals(array, map.get(name));
    }

    @Override
    public boolean arraysEquals(String name, byte[] array, int offset, int length) throws IOException
    {
        byte[] ar = map.get(name);
        if (ar == null)
        {
            return array == null;
        }
        if (ar.length != length)
        {
            return false;
        }
        for (int ii=0;ii<length;ii++)
        {
            if (ar[ii] != array[offset + ii])
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void delete(String name) throws IOException
    {
        map.remove(name);
        store();
    }

    @Override
    public void deleteAll() throws IOException
    {
        map.clear();
        store();
    }

    @Override
    public byte[] get(String name) throws IOException
    {
        return map.get(name);
    }

    @Override
    public boolean getBoolean(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        if (array.length != 1 || array[0] > 1 || array[0] < 0)
        {
            throw new IllegalArgumentException(name+" not boolean");
        }
        return array[0] == 1;
    }

    @Override
    public double getDouble(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        if (array.length != 8)
        {
            throw new IllegalArgumentException(name+" not double");
        }
        return Primitives.readDouble(array);
    }

    @Override
    public int getInt(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        if (array.length != 4)
        {
            throw new IllegalArgumentException(name+" not double");
        }
        return Primitives.readInt(array);
    }

    @Override
    public long getLong(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        if (array.length != 8)
        {
            throw new IllegalArgumentException(name+" not double");
        }
        return Primitives.readLong(array);
    }

    @Override
    public String getString(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        return new String(array, StandardCharsets.UTF_8);
    }

    @Override
    public boolean has(String name) throws IOException
    {
        return map.containsKey(name);
    }

    @Override
    public Collection<String> list() throws IOException
    {
        return map.keySet();
    }

    @Override
    public int read(String name, ByteBuffer dst) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        dst.put(array);
        return array.length;
    }

    @Override
    public void set(String name, byte[] array) throws IOException
    {
        map.put(name, Arrays.copyOf(array, array.length));
        store();
    }

    @Override
    public void set(String name, byte[] array, int offset, int length) throws IOException
    {
        map.put(name, Arrays.copyOfRange(array, offset, offset + length));
        store();
    }

    @Override
    public void setBoolean(String name, boolean value) throws IOException
    {
        map.put(name, value ? new byte[] {1} : new byte[] {0});
        store();
    }

    @Override
    public void setDouble(String name, double value) throws IOException
    {
        map.put(name, Primitives.writeDouble(value));
        store();
    }

    @Override
    public void setInt(String name, int value) throws IOException
    {
        map.put(name, Primitives.writeInt(value));
        store();
    }

    @Override
    public void setLong(String name, long value) throws IOException
    {
        map.put(name, Primitives.writeLong(value));
        store();
    }

    @Override
    public void setString(String name, String value) throws IOException
    {
        map.put(name, value.getBytes(StandardCharsets.UTF_8));
        store();
    }

    @Override
    public int size(String name) throws IOException
    {
        byte[] array = map.get(name);
        if (array == null)
        {
            return 0;
        }
        return array.length;
    }

    @Override
    public int write(String name, ByteBuffer src) throws IOException
    {
        byte[] array = new byte[src.remaining()];
        src.get(array);
        map.put(name, array);
        store();
        return array.length;
    }
    
}
