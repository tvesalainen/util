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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * BufferedFileBuilder implements ByteBuffer type writing to file. It is like
 * mapping a file-channel.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BufferedFileBuilder implements AutoCloseable
{
    private FileChannel channel;
    private ByteBuffer buffer;

    public BufferedFileBuilder(int bufferSize, boolean direct, Path file, Set<? extends OpenOption> options, FileAttribute<?> attrs) throws IOException
    {
        this(bufferSize, direct, FileChannel.open(file, options, attrs));
    }
    public BufferedFileBuilder(int bufferSize, boolean direct, Path file, OpenOption... options) throws IOException
    {
        this(bufferSize, direct, FileChannel.open(file, options));
    }
    public BufferedFileBuilder(int bufferSize, boolean direct, FileChannel channel)
    {
        this.channel = channel;
        if (direct)
        {
            this.buffer = ByteBuffer.allocateDirect(bufferSize);
        }
        else
        {
            this.buffer = ByteBuffer.allocate(bufferSize);
        }
    }
    /**
     * Returns number of bytes put
     * @return
     * @throws IOException 
     */
    public long position() throws IOException
    {
        return channel.position()+buffer.remaining();
    }
    /**
     * Flushes and set's file-channel position.
     * @param newPosition
     * @return
     * @throws IOException 
     */
    public BufferedFileBuilder position(long newPosition) throws IOException
    {
        flush();
        channel.position(newPosition);
        return this;
    }
    /**
     * Flushes and set's file-channel position.
     * @param skip
     * @return
     * @throws IOException 
     */
    public BufferedFileBuilder skip(long skip) throws IOException
    {
        position(position()+skip);
        return this;
    }
    /**
     * Flushes and returns file size.
     * @return
     * @throws IOException 
     */
    public long size() throws IOException
    {
        flush();
        return channel.size();
    }
    
    public BufferedFileBuilder put(byte b) throws IOException
    {
        ensureRemaining(1);
        buffer.put(b);
        return this;
    }

    public BufferedFileBuilder put(ByteBuffer src) throws IOException
    {
        ensureRemaining(src.remaining());
        buffer.put(src);
        return this;
    }

    public BufferedFileBuilder put(byte[] src, int offset, int length) throws IOException
    {
        ensureRemaining(length);
        buffer.put(src, offset, length);
        return this;
    }

    public final BufferedFileBuilder put(byte[] src) throws IOException
    {
        ensureRemaining(src.length);
        buffer.put(src);
        return this;
    }

    public boolean isDirect()
    {
        return buffer.isDirect();
    }

    public final ByteOrder order()
    {
        return buffer.order();
    }

    public final BufferedFileBuilder order(ByteOrder bo)
    {
        buffer.order(bo);
        return this;
    }

    public BufferedFileBuilder putChar(char value) throws IOException
    {
        ensureRemaining(2);
        buffer.putChar(value);
        return this;
    }

    public BufferedFileBuilder putShort(short value) throws IOException
    {
        ensureRemaining(2);
        buffer.putShort(value);
        return this;
    }

    public BufferedFileBuilder putInt(int value) throws IOException
    {
        ensureRemaining(4);
        buffer.putInt(value);
        return this;
    }

    public BufferedFileBuilder putLong(long value) throws IOException
    {
        ensureRemaining(8);
        buffer.putLong(value);
        return this;
    }

    public BufferedFileBuilder putFloat(float value) throws IOException
    {
        ensureRemaining(4);
        buffer.putFloat(value);
        return this;
    }

    public BufferedFileBuilder putDouble(double value) throws IOException
    {
        ensureRemaining(8);
        buffer.putDouble(value);
        return this;
    }

    public BufferedFileBuilder put(CharSequence seq, Charset charset) throws IOException
    {
        CharsetEncoder encoder = charset.newEncoder();
        ensureRemaining((int) (encoder.maxBytesPerChar()*seq.length()));
        encoder.encode(CharBuffer.wrap(seq), buffer, true);
        return this;
    }
    private void ensureRemaining(int needed) throws IOException
    {
        if (buffer.remaining() < needed)
        {
            flush();
        }
    }
    @Override
    public void close() throws IOException
    {
        flush();
        channel.close();
    }

    private void flush() throws IOException
    {
        if (buffer.position() != 0)
        {
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        }
    }
    
}
