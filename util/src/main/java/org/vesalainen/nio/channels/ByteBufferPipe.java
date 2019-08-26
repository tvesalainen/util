/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.nio.RingByteBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferPipe
{
    
    private final Sink sink;
    private final Source source;
    private RingByteBuffer buffer;
    private final ReentrantLock lock;
    private final Condition hasRoom;
    private final Condition hasData;
    /**
     * Creates new ByteBufferPipe with 4096 buffer, heap ByteBuffer and unfair
     * locking policy.
     */
    public ByteBufferPipe()
    {
        this(4096, false);
    }
    /**
     * Creates new ByteBufferPipe with size buffer and unfair locking policy.
     * @param size
     * @param direct 
     */
    public ByteBufferPipe(int size, boolean direct)
    {
        this(size, direct, false);
    }
    /**
     * Creates new ByteBufferPipe with size buffer and unfair locking policy.
     * @param size Buffer size
     * @param direct Direct or heap ByteBuffer
     * @param fair Locking policy
     */
    public ByteBufferPipe(int size, boolean direct, boolean fair)
    {
        this.buffer = new RingByteBuffer(size, direct);
        this.sink = new Sink();
        this.source = new Source();
        lock = new ReentrantLock(fair);
        hasRoom = lock.newCondition();
        hasData = lock.newCondition();
    }

    public Sink sink()
    {
        return sink;
    }

    public Source source()
    {
        return source;
    }
    
    public class Sink implements GatheringByteChannel
    {
        private long timeout = Long.MAX_VALUE;
        private TimeUnit unit = MILLISECONDS;

        public void setTimeout(long timeout, TimeUnit unit)
        {
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
        {
            int remaining = ByteBuffers.remaining(srcs, offset, length);
            if (remaining > buffer.capacity())
            {
                throw new IOException(remaining+" is more than buffers capacity");
            }
            lock.lock();
            try
            {
                while(remaining > buffer.free())
                {
                    if (!hasRoom.await(timeout, unit))
                    {
                        return 0;
                    }
                }
                int rc = buffer.fill(srcs, offset, length);
                hasData.signal();
                return rc;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public int write(ByteBuffer src) throws IOException
        {
            int remaining = src.remaining();
            if (remaining > buffer.capacity())
            {
                throw new IOException(remaining+" is more than buffers capacity");
            }
            lock.lock();
            try
            {
                while(remaining > buffer.free())
                {
                    if (!hasRoom.await(timeout, unit))
                    {
                        return 0;
                    }
                }
                int rc = buffer.fill(src);
                hasData.signal();
                return rc;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public long write(ByteBuffer[] srcs) throws IOException
        {
            return write(srcs, 0, srcs.length);
        }

        @Override
        public boolean isOpen()
        {
            return buffer != null;
        }

        @Override
        public void close() throws IOException
        {
            buffer = null;
        }
    }
    
    public class Source implements ScatteringByteChannel
    {
        private long timeout = Long.MAX_VALUE;
        private TimeUnit unit = MILLISECONDS;

        public void setTimeout(long timeout, TimeUnit unit)
        {
            this.timeout = timeout;
            this.unit = unit;
        }

        
        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
        {
            lock.lock();
            try
            {
                while (!buffer.hasRemaining())
                {
                    if (!hasData.await(timeout, unit))
                    {
                        return 0;
                    }
                }
                int remaining = ByteBuffers.remaining(dsts, offset, length);
                buffer.mark(remaining);
                int rc = buffer.writeTo(dsts, offset, length);
                buffer.discard();
                hasRoom.signal();
                return rc;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }            
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            lock.lock();
            try
            {
                while (!buffer.hasRemaining())
                {
                    if (!hasData.await(timeout, unit))
                    {
                        return 0;
                    }
                }
                int remaining = dst.remaining();
                buffer.mark(remaining);
                int rc = buffer.writeTo(dst);
                buffer.discard();
                hasRoom.signal();
                return rc;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }            
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public long read(ByteBuffer[] dsts) throws IOException
        {
            return read(dsts, 0, dsts.length);
        }
        /**
         * Waits until at least one byte is available and then writes all
         * available bytes to channel. This is same as reading and then writing
         * to channel, but more efficient.
         * @param channel
         * @return
         * @throws IOException 
         */
        public int writeTo(GatheringByteChannel channel) throws IOException
        {
            lock.lock();
            try
            {
                while (!buffer.hasRemaining())
                {
                    if (!hasData.await(timeout, unit))
                    {
                        return 0;
                    }
                }
                int remaining = buffer.remaining();
                buffer.mark(remaining);
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }            
            finally
            {
                lock.unlock();
            }
            int rc = buffer.writeTo(channel);
            lock.lock();
            try
            {
                buffer.discard();
                hasRoom.signal();
                return rc;
            }
            finally
            {
                lock.unlock();
            }
        }

        @Override
        public boolean isOpen()
        {
            return buffer != null;
        }

        @Override
        public void close() throws IOException
        {
            buffer = null;
        }
    }    
}
