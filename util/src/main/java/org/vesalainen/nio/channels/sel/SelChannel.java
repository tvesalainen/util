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
package org.vesalainen.nio.channels.sel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import org.vesalainen.nio.channels.sel.SelKey.Op;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 * @param <T>
 */
public abstract class SelChannel<T extends Channel> extends JavaLogging implements Channel, Runnable, AutoCloseable
{
    protected T channel;
    
    private ChannelSelector selector;
    private SelKey key;

    private SelChannel(T channel, Class<?> cls)
    {
        super(cls);
        this.channel = channel;
    }
    
    public SelectorProvider provider()
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public abstract Op validOps();

    public boolean isRegistered()
    {
        return selector != null;
    }

    public SelKey keyFor(ChannelSelector sel)
    {
        if (selector == sel)
        {
            return key;
        }
        else
        {
            return null;
        }
    }

    public SelKey register(ChannelSelector sel, Op ops, Object att)
    {
        if (selector != null)
        {
            throw new IllegalStateException("already registered with "+selector);
        }
        key = sel.register(this, ops, att);
        selector = sel;
        return key;
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        if (selector != null)
        {
            selector.unregister(this);
        }
        selector = null;
        key = null;
        channel.close();
    }

    void unregister(ChannelSelector sel)
    {
        if (selector != sel)
        {
            throw new IllegalStateException("not registered with "+sel);
        }
        if (selector != null)
        {
            selector.unregister(this);
        }
        selector = null;
        key = null;
    }

    @Override
    public void run()
    {
        try
        {
            select();
            selector.ready(this);
        }
        catch (IOException ex)
        {
            selector.exception(this, ex);
        }
    }

    SelKey getKey()
    {
        return key;
    }

    protected abstract void select() throws IOException;
    
    public static class ReadSelectChannel<T extends SelectableChannel & ReadableByteChannel> extends SelChannel<T> implements ScatteringByteChannel
    {
        private ByteBuffer bb;
        
        public ReadSelectChannel(T channel, int size, boolean direct)
        {
            super(channel, ReadSelectChannel.class);
            if (size < 1)
            {
                throw new IllegalArgumentException("size "+size+" not valid");
            }
            if (direct)
            {
                bb = ByteBuffer.allocateDirect(size);
            }
            else
            {
                bb = ByteBuffer.allocate(size);
            }
            bb.flip();
        }

        @Override
        public Op validOps()
        {
            return Op.OP_READ;
        }

        @Override
        protected void select() throws IOException
        {
            if (!bb.hasRemaining())
            {
                bb.clear();
                int rc = channel.read(bb);
                if (rc != -1)
                {
                    fine("selected() %s", bb);
                    bb.flip();
                }
                else
                {
                    fine("selected() EOF");
                    bb = null;
                }
            }
            else
            {
                fine("selected() has remaining %s", bb);
            }
        }   

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
        {
            if (bb == null)
            {
                fine("read() EOF");
                return -1;
            }
            long count = 0;
            for (int ii=0;ii<length && bb.hasRemaining();ii++)
            {
                int rc = read(dsts[ii]);    // can't be -1
                count += rc;
            }
            fine("read() = %d", count);
            return count;
        }

        @Override
        public long read(ByteBuffer[] dsts) throws IOException
        {
            long count = read(dsts, 0, dsts.length);
            fine("read() = %d", count);
            return count;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException
        {
            if (bb == null)
            {
                fine("read() EOF");
                return -1;
            }
            int srcrem = bb.remaining();
            int dstrem = dst.remaining();
            if (srcrem <= dstrem)
            {
                dst.put(bb);
                fine("read() = %d %s", srcrem, bb);
                return srcrem;
            }
            else
            {
                int delta = srcrem - dstrem;
                bb.limit(bb.limit() - delta);
                dst.put(bb);
                bb.limit(bb.limit() + delta);
                fine("read() = %d %s", dstrem, bb);
                return dstrem;
            }
        }

        @Override
        public String toString()
        {
            return "ReadSelectChannel{" +channel+ '}';
        }
        
    }
    public static class AcceptSelectChannel extends SelChannel<ServerSocketChannel> implements Acceptor<SocketChannel>
    {

        private SocketChannel socketChannel;

        public AcceptSelectChannel(ServerSocketChannel channel)
        {
            super(channel, AcceptSelectChannel.class);
        }

        @Override
        public Op validOps()
        {
            return Op.OP_ACCEPT;
        }

        @Override
        protected void select() throws IOException
        {
            socketChannel = channel.accept();
            fine("selected() = %s", socketChannel);
        }

        @Override
        public SocketChannel accept() throws IOException
        {
            fine("accepted() = %s", socketChannel);
            return socketChannel;
        }

        @Override
        public String toString()
        {
            return "AcceptSelectChannel{" + channel + '}';
        }
        
    }
}
