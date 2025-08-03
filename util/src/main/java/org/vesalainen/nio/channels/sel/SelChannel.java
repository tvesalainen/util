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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public abstract class SelChannel<T extends Channel> extends JavaLogging implements Channel, Runnable, AutoCloseable
{
    protected T channel;
    protected Op validOp;
    
    private ChannelSelector selector;
    private SelKey key;

    private SelChannel(T channel, Op op, Class<?> cls)
    {
        super(cls);
        this.channel = channel;
        this.validOp = op;
    }
    /**
     * Throws exception.
     * @return 
     */
    public SelectorProvider provider()
    {
        throw new UnsupportedOperationException("Not supported.");
    }
    /**
     * Returns valid ops.
     * @return 
     */
    public final Op validOps()
    {
        return validOp;
    }
    /**
     * Returns true if channel is registered.
     * @return 
     */
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
    /**
     * Registers channel for selection.
     * @param sel
     * @param ops
     * @param att
     * @return 
     */
    public SelKey register(ChannelSelector sel, Op ops, Object att)
    {
        if (selector != null)
        {
            throw new IllegalStateException("already registered with "+selector);
        }
        if (validOp != ops)
        {
            throw new IllegalArgumentException("expected "+validOp+" got "+ops);
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
        
        public ReadSelectChannel(T channel)
        {
            super(channel, Op.OP_READ, ReadSelectChannel.class);
            bb = ByteBuffer.allocate(1);
            bb.flip();
        }

        @Override
        protected void select() throws IOException
        {
            if (!bb.hasRemaining())
            {
                bb.clear();
                channel.configureBlocking(true);
                int rc = channel.read(bb);
                if (rc != -1)
                {
                    channel.configureBlocking(false);
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
            int count = 0;
            if (bb.hasRemaining() && dst.hasRemaining())
            {
                dst.put(bb);
                fine("read(1)");
                count = 1;
            }
            int rc = channel.read(dst);
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
            return count + rc;
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
            super(channel, Op.OP_ACCEPT, AcceptSelectChannel.class);
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
