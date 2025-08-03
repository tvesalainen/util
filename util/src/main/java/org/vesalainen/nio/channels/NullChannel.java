/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.locks.LockSupport;

/**
 * Writing to NullChannel is like writing to a channel that can handle all
 * remaining bytes. Maximum remaining bytes is returned and position is moved
 * to limit.
 * <p>Reading from NullChannel block until thread is interrupted. When that 
 * happens ClosedByInterruptException is thrown.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NullChannel implements Closeable, AutoCloseable, ByteChannel, GatheringByteChannel, InterruptibleChannel, WritableByteChannel, ReadableByteChannel, ScatteringByteChannel
{

    private boolean closed;

    @Override
    public boolean isOpen()
    {
        return !closed;
    }

    @Override
    public void close() throws IOException
    {
        this.closed = true;
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        int len = src.remaining();
        src.position(src.limit());
        return len;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        long len = 0;
        for (int ii=0;ii<length;ii++)
        {
            ByteBuffer src = srcs[ii+offset];
            len += src.remaining();
            src.position(src.limit());
        }
        return len;
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        waitForInterrupt();
        return -1;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        waitForInterrupt();
        return -1;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        waitForInterrupt();
        return -1;
    }

    private void waitForInterrupt() throws IOException
    {
        while (true)
        {
            LockSupport.park(this);
            if (Thread.interrupted())
            {
                throw new ClosedByInterruptException();
            }
        }
    }
    
}
