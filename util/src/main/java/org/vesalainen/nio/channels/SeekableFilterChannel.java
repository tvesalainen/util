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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.lang.Casts;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.function.IOFunction;

/**
 * SeekableFilterChannel provides channel interface for stream filtering.
 * <p>
 * Example:
 * <code>
   try (SeekableFilterChannel xzChannel = new SeekableFilterChannel(channel, 4096, 512, XZInputStream::new, null))
   {
       load(xzChannel, root);
   }
 </code>
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SeekableFilterChannel extends FilterChannel implements SeekableByteChannel
{
    private int maxSkipSize;
    private ByteBuffer skipBuffer;
    /**
     * Creates FilterSeekableByteChannel. Only one of in/out functions is allowed.
     * @param channel
     * @param bufSize Buffer size
     * @param maxSkipSize Maximum forward position size
     * @param fin
     * @param fout
     * @throws IOException 
     */
    public SeekableFilterChannel(
            SeekableByteChannel channel, 
            int bufSize, 
            int maxSkipSize, 
            IOFunction<? super InputStream,? extends InputStream> fin,
            IOFunction<? super OutputStream,? extends OutputStream> fout
    ) throws IOException
    {
        super(channel, bufSize, fin, fout);
        this.maxSkipSize = maxSkipSize;
        if (maxSkipSize > 0)
        {
            skipBuffer = ByteBuffer.allocate(maxSkipSize);
        }
    }
    /**
     * Returns unfiltered position.
     * @return
     * @throws IOException 
     */
    @Override
    public long position() throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        return position;
    }
    /**
     * Changes unfiltered position. Only forward direction is allowed with
     * small skips. This method is for alignment purposes mostly.
     * @param newPosition
     * @return
     * @throws IOException 
     */
    @Override
    public SeekableFilterChannel position(long newPosition) throws IOException
    {
        if (!isOpen())
        {
            throw new ClosedChannelException();
        }
        int skip = (int) (newPosition - position());
        if (skip < 0)
        {
            throw new UnsupportedOperationException("backwards position not supported");
        }
        if (skip > skipBuffer.capacity())
        {
            throw new UnsupportedOperationException(skip+" skip not supported maxSkipSize="+maxSkipSize);
        }
        if (skip > 0)
        {
            if (skipBuffer == null)
            {
                throw new UnsupportedOperationException("skip not supported maxSkipSize="+maxSkipSize);
            }
            skipBuffer.clear();
            skipBuffer.limit(skip);
            if (in != null)
            {
                read(skipBuffer);
            }
            else
            {
                write(skipBuffer);
            }
        }
        return this;
    }
    /**
     * Throws UnsupportedOperationException
     * @return
     * @throws IOException 
     */
    @Override
    public long size() throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    /**
     * 
     * @param size
     * @return
     * @throws IOException 
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
