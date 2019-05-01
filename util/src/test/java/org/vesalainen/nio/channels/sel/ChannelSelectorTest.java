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

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.nio.channels.sel.SelChannel.AcceptSelectChannel;
import org.vesalainen.nio.channels.sel.SelChannel.ReadSelectChannel;
import org.vesalainen.nio.channels.sel.SelKey.Op;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChannelSelectorTest
{
    public static final int BUFSIZE = 256;
    private final byte[] exp;
    public ChannelSelectorTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.ALL);
        exp = new byte[BUFSIZE];
        Random random = new Random(98765);
        random.nextBytes(exp);
    }

    @Test
    public void testWakeup() throws IOException
    {
        try (ChannelSelector selector = new ChannelSelector())
        {
            selector.wakeup();
            int cnt = selector.select();
            assertEquals(0, cnt);
            assertTrue(selector.keys().isEmpty());
            assertTrue(selector.selectedKeys().isEmpty());
        }
    }
    
    //@Test TODO why failng
    public void testSelect() throws IOException
    {
        try (ChannelSelector selector = new ChannelSelector())
        {
            Listener listener = new Listener(selector);
            listener.register();
            assertEquals(1, selector.keys().size());
            SocketAddress localAddress = listener.getLocalAddress();
            int cnt = selector.selectNow();
            assertEquals(0, cnt);
            assertEquals(0, selector.selectedKeys().size());
            Client client = new Client(selector, localAddress);
            client.register();
            assertEquals(2, selector.keys().size());
            while (client.getCount() < BUFSIZE)
            {
                cnt = selector.select(100);
                Iterator<SelKey> it = selector.selectedKeys().iterator();
                while (it.hasNext())
                {
                    SelKey sk = it.next();
                    Handler h = (Handler) sk.attachment();
                    h.handle();
                    it.remove();
                }
            }
            assertArrayEquals(exp, client.getGot());
            listener.unregister();
            assertEquals(1, selector.keys().size());
            client.unregister();
            assertEquals(0, selector.keys().size());
        }
    }
    
    @Test
    public void testForEach() throws IOException
    {
        try (ChannelSelector selector = new ChannelSelector())
        {
            Listener listener = new Listener(selector);
            listener.register();
            assertEquals(1, selector.keys().size());
            SocketAddress localAddress = listener.getLocalAddress();
            int cnt = selector.selectNow();
            assertEquals(0, cnt);
            assertEquals(0, selector.selectedKeys().size());
            Client client = new Client(selector, localAddress);
            client.register();
            assertEquals(2, selector.keys().size());
            selector.forEach((s)->
            {
                Handler h = (Handler)s.attachment();
                try
                {
                    h.handle();
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }, 500);
            assertEquals(BUFSIZE, client.getCount());
            assertArrayEquals(exp, client.getGot());
            listener.unregister();
            assertEquals(1, selector.keys().size());
            client.unregister();
            assertEquals(0, selector.keys().size());
        }
    }
    
    private interface Handler
    {
        void register() throws IOException;
        void handle() throws IOException;
        void unregister() throws IOException;
    }
    private class Client implements Handler
    {
        private ChannelSelector selector;
        private SocketChannel sc;
        private ReadSelectChannel channel;
        private ByteBuffer bb = ByteBuffer.allocate(10);
        private byte[] got = new byte[BUFSIZE];
        private int count;
        private SelKey selKey;

        public Client(ChannelSelector selector, SocketAddress sa) throws IOException
        {
            this.selector = selector;
            sc = SocketChannel.open(sa);
            channel = new ReadSelectChannel(sc);
        }

        @Override
        public void register() throws IOException
        {
            selKey = channel.register(selector, Op.OP_READ, this);
        }

        @Override
        public void unregister() throws IOException
        {
            selKey.cancel();
        }
        
        @Override
        public void handle() throws IOException
        {
            bb.clear();
            int rc = channel.read(bb);
            if (rc == -1)
            {
                throw new EOFException();
            }
            bb.flip();
            bb.get(got, count, rc);
            count += rc;
        }

        public byte[] getGot()
        {
            return got;
        }

        public int getCount()
        {
            return count;
        }
        
    }
    private class Listener implements Handler
    {
        private ChannelSelector selector;
        private ServerSocketChannel ssc;
        private AcceptSelectChannel channel;
        private SelKey selKey;

        public Listener(ChannelSelector selector) throws IOException
        {
            this.selector = selector;
            ssc = ServerSocketChannel.open();
            ssc.bind(null);
            channel = new AcceptSelectChannel(ssc);
        }

        @Override
        public void register() throws IOException
        {
            selKey = channel.register(selector, Op.OP_ACCEPT, this);
        }
        
        @Override
        public void unregister() throws IOException
        {
            selKey.cancel();
        }
        
        public SocketAddress getLocalAddress() throws IOException
        {
            return ssc.getLocalAddress();
        }
        
        @Override
        public void handle() throws IOException
        {
            SocketChannel sc = channel.accept();
            ByteBuffer bb = ByteBuffer.wrap(exp);
            ChannelHelper.writeAll(sc, bb);
        }
        
    }
}
