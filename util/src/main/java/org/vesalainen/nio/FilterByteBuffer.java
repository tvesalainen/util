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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * FilterByteBuffer enables filtered I/O to underlying ByteBuffer with 
 * ByteBuffer like API. Byte order is BIG_ENDIAN.
 * <p>
 * Note! Get methods throw EOFException and put (flush) methods throw 
 * BufferOverflowException when ByteBuffer remaining is not enough.
 * <p>
 * Usage example: Read/Write GZIP content to/from FileChannel using mapped ByteBuffer.
 * 
 * @author tkv
 * @see java.io.DataInputStream
 * @see java.io.DataOutputStream
 */
public class FilterByteBuffer implements AutoCloseable
{
    private ByteBuffer bb;
    private DataInputStream in;
    private DataOutputStream out;
    private int position;

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
    /**
     * Returns current position. Position starts at 0. Every get and put method
     * will increase position.
     * @return 
     */
    public int position()
    {
        return position;
    }
    /**
     * Set's position to given int. Doesn't do anything else!
     * @param position 
     */
    public void position(int position)
    {
        this.position = position;
    }
    /**
     * Puts skip number of 0 bytes to output.
     * @param skip
     * @throws IOException 
     */
    public void skipOutput(int skip) throws IOException
    {
        for (int ii=0;ii<skip;ii++)
        {
            put((byte)0);
        }
    }
    /**
     * Skips skip number of bytes from input.
     * @param skip
     * @throws IOException 
     */
    public void skipInput(int skip) throws IOException
    {
        in.skip(skip);
        position += skip;
    }
    /**
     * Skips output so that position % align == 0
     * @param align
     * @throws IOException 
     */
    public void alignOutput(int align) throws IOException
    {
        int mod = position % align;
        if (mod > 0)
        {
            skipOutput(align - mod);
        }
    }
    /**
     * Skips output so that position % align == 0
     * @param align
     * @throws IOException 
     */
    public void alignInput(int align) throws IOException
    {
        int mod = position % align;
        if (mod > 0)
        {
            skipInput(align - mod);
        }
    }
    /**
     * Flushes output to ByteBuffer.
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public void flush() throws IOException
    {
        checkOut();
        out.flush();
    }
    /**
     * Returns next byte.
     * @return
     * @throws IOException
     * @throws EOFException If get request couldn't be filled. 
     */
    public byte get() throws IOException
    {
        checkIn();
        position++;
        return in.readByte();
    }
    /**
     * Puts byte.
     * @param b
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer put(byte b) throws IOException
    {
        checkOut();
        out.writeByte(b);
        position++;
        return this;
    }
    /**
     * Tries to read length bytes to dst. 
     * @param dst
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public FilterByteBuffer get(byte[] dst, int offset, int length) throws IOException
    {
        checkIn();
        int rc = in.read(dst, offset, length);
        if (rc != length)
        {
            throw new EOFException();
        }
        position += length;
        return this;
    }
    /**
     * Tries to fill dst. 
     * @param dst
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public FilterByteBuffer get(byte[] dst) throws IOException
    {
        checkIn();
        int rc = in.read(dst);
        if (rc != dst.length)
        {
            throw new EOFException();
        }
        position += dst.length;
        return this;
    }
    /**
     * Puts length bytes.
     * @param src
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer put(byte[] src, int offset, int length) throws IOException
    {
        checkOut();
        out.write(src, offset, length);
        position += length;
        return this;
    }
    /**
     * Puts whole dst.
     * @param src
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer put(byte[] src) throws IOException
    {
        checkOut();
        out.write(src);
        position += src.length;
        return this;
    }
    /**
     * Return 2 next bytes as char
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public char getChar() throws IOException
    {
        checkIn();
        position += 2;
        return in.readChar();
    }
    /**
     * Puts char as 2 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putChar(char value) throws IOException
    {
        checkOut();
        out.writeChar(value);
        position += 2;
        return this;
    }
    /**
     * Returns short as 2 bytes.
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public short getShort() throws IOException
    {
        checkIn();
        position += 2;
        return in.readShort();
    }
    /**
     * Puts short as 2 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putShort(short value) throws IOException
    {
        checkOut();
        out.writeShort(value);
        position += 2;
        return this;
    }
    /**
     * Returns int as 4 bytes.
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public int getInt() throws IOException
    {
        checkIn();
        position += 4;
        return in.readInt();
    }
    /**
     * Puts int as 4 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putInt(int value) throws IOException
    {
        checkOut();
        out.writeInt(value);
        position += 4;
        return this;
    }
    /**
     * Returns long as 8 bytes
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public long getLong() throws IOException
    {
        checkIn();
        position += 8;
        return in.readLong();
    }
    /**
     * Puts long as 8 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putLong(long value) throws IOException
    {
        checkOut();
        out.writeLong(value);
        position += 8;
        return this;
    }
    /**
     * Returns float as 4 bytes
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public float getFloat() throws IOException
    {
        checkIn();
        position += 4;
        return in.readFloat();
    }
    /**
     * Puts float as 4 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putFloat(float value) throws IOException
    {
        checkOut();
        out.writeFloat(value);
        position += 4;
        return this;
    }
    /**
     * Returns double as 8 bytes
     * @return
     * @throws IOException 
     * @throws EOFException If get request couldn't be filled. 
     */
    public double getDouble() throws IOException
    {
        checkIn();
        position += 8;
        return in.readDouble();
    }
    /**
     * Puts double as 8 bytes.
     * @param value
     * @return
     * @throws IOException 
     * @throws BufferOverflowException If not enough room in ByteBuffer.
     */
    public FilterByteBuffer putDouble(double value) throws IOException
    {
        checkOut();
        out.writeDouble(value);
        position += 8;
        return this;
    }
    /**
     * Closes underlying streams.
     * @throws Exception 
     */
    @Override
    public void close() throws Exception
    {
        if (out != null)
        {
            out.close();
            out = null;
        }
        if (in != null)
        {
            in.close();
            in = null;
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
