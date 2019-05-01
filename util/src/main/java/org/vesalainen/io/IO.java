/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IO
{
    /**
     * Writes object to path
     * @param <T>
     * @param obj
     * @param path
     * @param options
     * @throws IOException 
     */
    public static final <T extends Serializable> void serialize(T obj, Path path, OpenOption... options) throws IOException
    {
        try (   OutputStream os = Files.newOutputStream(path, options);
                ObjectOutputStream oos = new ObjectOutputStream(os);
                )
        {
            oos.writeObject(obj);
        }
    }
    /**
     * Reads serialized object from path
     * @param <T>
     * @param path
     * @param options
     * @return
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static final <T extends Serializable> T deserialize(Path path, OpenOption... options) throws IOException, ClassNotFoundException
    {
        try (   InputStream is = Files.newInputStream(path, options);
                ObjectInputStream ois = new ObjectInputStream(is);
                )
        {
            return (T) ois.readObject();
        }
    }
    public static final int readFully(Reader reader, byte[] buffer) throws IOException
    {
        return readFully(reader, buffer, 0, buffer.length);
    }
    public static final int readFully(Reader reader, byte[] buffer, int offset, int length) throws IOException
    {
        int count = 0;
        while (length > 0)
        {
            int rc = reader.read(buffer, offset, length);
            if (rc == -1)
            {
                if (count > 0)
                {
                    return count;
                }
                else
                {
                    return -1;
                }
            }
            length -= rc;
            offset += rc;
            count += rc;
        }
        return count;
    }
    @FunctionalInterface
    public interface Reader
    {
        int read(byte[] buffer, int offset, int length) throws IOException;
    }
}
