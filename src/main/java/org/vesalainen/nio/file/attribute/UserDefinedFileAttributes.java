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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.ThreadSafeTemporary;

/**
 *
 * @author tkv
 */
public class UserDefinedFileAttributes
{
    private final UserDefinedFileAttributeView view;
    private ThreadSafeTemporary<ByteBuffer> bbStore;
    private ThreadSafeTemporary<ObjectInputStream> inStore;
    private ThreadSafeTemporary<ObjectOutputStream> outStore;

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
        this.bbStore = new ThreadSafeTemporary<>(()->{return ByteBuffer.allocate(maxSize);});
        this.inStore = new ThreadSafeTemporary<>(()->{return createInput();});
        this.outStore = new ThreadSafeTemporary<>(()->{return createOutput();});
    }

    private ObjectInputStream createInput()
    {
        try
        {
            return new ObjectInputStream(new BBIS(bbStore.get()));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private ObjectOutputStream createOutput()
    {
        try
        {
            return new ObjectOutputStream(new BBOS(bbStore.get()));
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public List<String> list() throws IOException
    {
        return view.list();
    }
    public void delete(String name) throws IOException
    {
        view.delete(name);
    }
    public int size(String name) throws IOException
    {
        return view.size(name);
    }
    public int write(String name, ByteBuffer src) throws IOException
    {
        return view.write(name, src);
    }
    public int read(String name, ByteBuffer dst) throws IOException
    {
        return view.read(name, dst);
    }
    public void setString(String name, String value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeUTF(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public void set(String name, boolean value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeBoolean(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public void set(String name, int value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeInt(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public void set(String name, long value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeLong(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public void set(String name, double value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeDouble(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public void setObject(String name, Serializable value) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        ObjectOutputStream out = outStore.get();
        out.writeObject(value);
        out.flush();
        bb.flip();
        view.write(name, bb);
    }
    public String getString(String name) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readUTF();
    }
    public boolean getBoolean(String name) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readBoolean();
    }
    public int getInt(String name) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readInt();
    }
    public long getLong(String name) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readLong();
    }
    public double getDouble(String name) throws IOException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readDouble();
    }
    public Object getObject(String name) throws IOException, ClassNotFoundException
    {
        ByteBuffer bb = bbStore.get();
        bb.clear();
        view.read(name, bb);
        bb.flip();
        ObjectInputStream in = inStore.get();
        return in.readObject();
    }
    private static class BBIS extends InputStream
    {
        private final ByteBuffer bb;

        public BBIS(ByteBuffer bb)
        {
            this.bb = bb;
        }
        
        @Override
        public int read() throws IOException
        {
            if (bb.hasRemaining())
            {
                return bb.get();
            }
            else
            {
                return -1;
            }
        }
        
    }
    private static class BBOS extends OutputStream
    {
        private final ByteBuffer bb;

        public BBOS(ByteBuffer bb)
        {
            this.bb = bb;
        }

        @Override
        public void write(int b) throws IOException
        {
            try
            {
                bb.put((byte) b);
            }
            catch (BufferOverflowException ex)
            {
                throw new IOException(ex);
            }
        }
        
    }
}
