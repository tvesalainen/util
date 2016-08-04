/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nio.channels.vc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class VirtualCircuitTest
{
    public VirtualCircuitTest()
    {
    }

    @Test
    public void testSelectable() throws IOException, InterruptedException, ExecutionException
    {
        test((SocketChannel sc1, SocketChannel sc2)->{return new SelectableVirtualCircuit(sc1, sc2, 1024, false);});
    }
    @Test
    public void testByteChannel() throws IOException, InterruptedException, ExecutionException
    {
        test((SocketChannel sc1, SocketChannel sc2)->{return new ByteChannelVirtualCircuit(sc1, sc2, 1024, false);});
    }
    public void test(BiFunction<SocketChannel,SocketChannel,VirtualCircuit> supplier) throws IOException, InterruptedException, ExecutionException
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        ByteBuffer bb = ByteBuffer.allocate(1024);
        
        SocketAcceptor sa1 = new SocketAcceptor();
        Future<SocketChannel> f1 = executor.submit(sa1);
        SocketChannel sc11 = SocketChannel.open(new InetSocketAddress("localhost", sa1.getPort()));
        SocketChannel sc12 = f1.get();
        
        SocketAcceptor sa2 = new SocketAcceptor();
        Future<SocketChannel> f2 = executor.submit(sa2);
        SocketChannel sc21 = SocketChannel.open(new InetSocketAddress("localhost", sa2.getPort()));
        SocketChannel sc22 = f2.get();
        
        VirtualCircuit vc = supplier.apply(sc12, sc21);
        
        executor.submit(new Echo(sc22));
        
        vc.start(executor);
        
        byte[] exp = new byte[1024];
        Random random = new Random(98765);
        random.nextBytes(exp);
        bb.put(exp);
        bb.flip();
        sc11.write(bb);
        bb.clear();
        sc11.read(bb);
        byte[] array = bb.array();
        assertArrayEquals(exp, array);
        sc11.close();
        sc12.close();
        sc21.close();
        sc22.close();
        executor.shutdownNow();
    }
    private static class SocketAcceptor implements Callable<SocketChannel>
    {
        private final ServerSocketChannel ssc;

        public SocketAcceptor() throws IOException
        {
            ssc = ServerSocketChannel.open();
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            ssc.bind(null);
        }

        public int getPort() throws IOException
        {
            InetSocketAddress local = (InetSocketAddress) ssc.getLocalAddress();
            return local.getPort();
        }
        
        @Override
        public SocketChannel call() throws Exception
        {
            SocketChannel sc = ssc.accept();
            ssc.close();
            return sc;
        }
        
    }
    private static class Echo implements Callable<Void>
    {
        private ByteChannel rc;
        private ByteBuffer bb = ByteBuffer.allocate(256);

        public Echo(ByteChannel rc)
        {
            this.rc = rc;
        }

        @Override
        public Void call() throws Exception
        {
            while (true)
            {
                bb.clear();
                rc.read(bb);
                bb.flip();
                rc.write(bb);
            }
        }
    }
}
