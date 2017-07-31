/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.nio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * FilterByteBuffer enables filtered I/O to underlying ByteBuffer with 
 * ByteBuffer like API. Byte order is BIG_ENDIAN.
 * 
 * @author tkv
 */
public class FilterByteBuffer implements AutoCloseable
{
    private ByteBuffer bb;
    DataInputStream in;
    DataOutputStream out;

    /**
     * Creates new FilterByteBuffer over bb 
     * @param bb
     * @param fin Mapper can be null.
     * @param fout Mapper can be null. 
     */
    public FilterByteBuffer(ByteBuffer bb, Function<? super InputStream,? extends InputStream> fin, Function<? super OutputStream,? extends OutputStream> fout)
    {
        this.bb = bb;
        if (fin != null)
        {
            this.in = new DataInputStream(fin.apply(new BBIn()));
        }
        if (fout != null)
        {
            this.out = new DataOutputStream(fout.apply(new BBOut()));
        }
    }
    
    public int position()
    {
        return bb.position();
    }
           
    public void flush() throws IOException
    {
        checkOut();
        out.flush();
    }
    public byte get() throws IOException
    {
        checkIn();
        return in.readByte();
    }

    public FilterByteBuffer put(byte b) throws IOException
    {
        checkOut();
        out.writeByte(b);
        return this;
    }
    public FilterByteBuffer get(byte[] dst, int offset, int length) throws IOException
    {
        checkIn();
        in.read(dst, offset, length);
        return this;
    }
    public FilterByteBuffer get(byte[] dst) throws IOException
    {
        checkIn();
        in.read(dst);
        return this;
    }
    public FilterByteBuffer put(byte[] src, int offset, int length) throws IOException
    {
        checkOut();
        out.write(src, offset, length);
        return this;
    }
    public FilterByteBuffer put(byte[] src) throws IOException
    {
        checkOut();
        out.write(src);
        return this;
    }
        
    public char getChar() throws IOException
    {
        checkIn();
        return in.readChar();
    }

    public FilterByteBuffer putChar(char value) throws IOException
    {
        checkOut();
        out.writeChar(value);
        return this;
    }

    public short getShort() throws IOException
    {
        checkIn();
        return in.readShort();
    }

    public FilterByteBuffer putShort(short value) throws IOException
    {
        checkOut();
        out.writeShort(value);
        return this;
    }

    public int getInt() throws IOException
    {
        checkIn();
        return in.readInt();
    }

    public FilterByteBuffer putInt(int value) throws IOException
    {
        checkOut();
        out.writeInt(value);
        return this;
    }

    public long getLong() throws IOException
    {
        checkIn();
        return in.readLong();
    }

    public FilterByteBuffer putLong(long value) throws IOException
    {
        checkOut();
        out.writeLong(value);
        return this;
    }

    public float getFloat() throws IOException
    {
        checkIn();
        return in.readFloat();
    }

    public FilterByteBuffer putFloat(float value) throws IOException
    {
        checkOut();
        out.writeFloat(value);
        return this;
    }

    public double getDouble() throws IOException
    {
        checkIn();
        return in.readDouble();
    }

    public FilterByteBuffer putDouble(double value) throws IOException
    {
        checkOut();
        out.writeDouble(value);
        return this;
    }

    @Override
    public void close() throws Exception
    {
        if (out != null)
        {
            out.close();
        }
        if (in != null)
        {
            in.close();
        }
    }

    private void checkOut()
    {
        if (out == null)
        {
            throw new IllegalStateException("out mapper is null");
        }
    }

    private void checkIn()
    {
        if (in == null)
        {
            throw new IllegalStateException("in mapper is null");
        }
    }

    private class BBOut extends OutputStream
    {

        @Override
        public void write(int b) throws IOException
        {
            bb.put((byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            bb.put(b, off, len);
        }

    }
    private class BBIn extends InputStream
    {

        @Override
        public int read() throws IOException
        {
            return bb.get();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            len = Math.min(len, bb.remaining());
            bb.get(b, off, len);
            return len;
        }

    }
}
