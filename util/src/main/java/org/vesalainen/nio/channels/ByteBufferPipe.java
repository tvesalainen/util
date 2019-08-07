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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.nio.RingByteBuffer;
import org.vesalainen.util.concurrent.PredicateSynchronizer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferPipe
{
    
    private Sink sink;
    private Source source;
    private RingByteBuffer buffer;
    private ReentrantLock lock = new ReentrantLock();
    private Condition hasRoom = lock.newCondition();
    private Condition hasData = lock.newCondition();

    public ByteBufferPipe(int size, boolean direct)
    {
        this.buffer = new RingByteBuffer(size, direct);
        this.sink = new Sink();
        this.source = new Source();
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
