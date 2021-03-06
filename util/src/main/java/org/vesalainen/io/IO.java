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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class IO
{
    public static BufferedInputStream buffer(InputStream is)
    {
        if (is instanceof BufferedInputStream)
        {
            return (BufferedInputStream) is;
        }
        else
        {
            return new BufferedInputStream(is);
        }
    }
    public static BufferedOutputStream buffer(OutputStream os)
    {
        if (os instanceof BufferedOutputStream)
        {
            return (BufferedOutputStream) os;
        }
        else
        {
            return new BufferedOutputStream(os);
        }
    }
    public static BufferedReader buffer(Reader r)
    {
        if (r instanceof BufferedReader)
        {
            return (BufferedReader) r;
        }
        else
        {
            return new BufferedReader(r);
        }
    }
    public static BufferedWriter buffer(Writer w)
    {
        if (w instanceof BufferedWriter)
        {
            return (BufferedWriter) w;
        }
        else
        {
            return new BufferedWriter(w);
        }
    }
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
    public static final int readFully(ReaderIntf reader, byte[] buffer) throws IOException
    {
        return readFully(reader, buffer, 0, buffer.length);
    }
    public static final int readFully(ReaderIntf reader, byte[] buffer, int offset, int length) throws IOException
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
    public interface ReaderIntf
    {
        int read(byte[] buffer, int offset, int length) throws IOException;
    }
}
