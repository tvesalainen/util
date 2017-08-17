/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.vc.ByteChannelVirtualCircuit;
import org.vesalainen.nio.channels.vc.SelectableVirtualCircuit;
import org.vesalainen.nio.channels.vc.VirtualCircuit;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualCircuitTest
{
    private static SSLContext sslCtx;
    public VirtualCircuitTest() throws IOException
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINEST);
        Security.addProvider(new BouncyCastleProvider());
        sslCtx = TestSSLContext.getInstance();
    }

    @Test
    public void testSelectableChannel() throws IOException, InterruptedException, ExecutionException
    {
        test((SSLSocketChannel sc1, SSLSocketChannel sc2)->{return new SelectableVirtualCircuit(sc1.getSelector(), sc2.getSelector(), sc1, sc2, 4096, false);});
    }
    @Test
    public void testByteChannel() throws IOException, InterruptedException, ExecutionException
    {
        test((SSLSocketChannel sc1, SSLSocketChannel sc2)->{return new ByteChannelVirtualCircuit(sc1, sc2, 4096, false);});
    }
    public void test(BiFunction<SSLSocketChannel,SSLSocketChannel,VirtualCircuit> supplier) throws IOException, InterruptedException, ExecutionException
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        ByteBuffer bb = ByteBuffer.allocate(2048);
        
        SocketAcceptor sa1 = new SocketAcceptor();
        Future<SSLSocketChannel> f1 = executor.submit(sa1);
        SSLSocketChannel sc11 = SSLSocketChannel.open("localhost", sa1.getPort(), sslCtx);
        SSLSocketChannel ss12 = f1.get();
        // sc11 <--> ss12
        
        SocketAcceptor sa2 = new SocketAcceptor();
        Future<SSLSocketChannel> f2 = executor.submit(sa2);
        SSLSocketChannel sc21 = SSLSocketChannel.open("localhost", sa2.getPort(), sslCtx);
        SSLSocketChannel ss22 = f2.get();
        // sc21 <--> ss22
        
        VirtualCircuit vc = supplier.apply(ss12, sc21);
        // sc11 <--> ss12 <--> VC <--> sc21 <--> ss22
        
        executor.submit(new Echo(ss22));
        // sc11 <--> ss12 <--> VC <--> sc21 <--> ss22 <--> Echo
        
        vc.start(()->{return executor;});
        
        byte[] exp = new byte[1024];
        Random random = new Random(98765);
        random.nextBytes(exp);
        bb.put(exp);
        bb.flip();
        sc11.write(bb);
        bb.clear();
        int rc  = sc11.read(bb);
        assertEquals(1024, rc);
        byte[] array = bb.array();
        byte[] got = Arrays.copyOf(array, 1024);
        assertArrayEquals(exp, got);
        sc11.close();
        executor.shutdown();
        assertTrue(executor.awaitTermination(1000, TimeUnit.SECONDS));
    }
    private static class SocketAcceptor implements Callable<SSLSocketChannel>
    {
        private final SSLServerSocketChannel ssc;

        public SocketAcceptor() throws IOException
        {
            ssc = SSLServerSocketChannel.open(null, sslCtx);
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        }

        public int getPort() throws IOException
        {
            InetSocketAddress local = (InetSocketAddress) ssc.getLocalAddress();
            return local.getPort();
        }
        
        @Override
        public SSLSocketChannel call() throws Exception
        {
            SSLSocketChannel sc = ssc.accept();
            ssc.close();
            return sc;
        }
        
    }
    private static class Echo implements Callable<Void>
    {
        private ByteChannel rc;
        private ByteBuffer bb = ByteBuffer.allocateDirect(2048);
        private int count;

        public Echo(ByteChannel rc)
        {
            this.rc = rc;
        }

        @Override
        public Void call() throws Exception
        {
            try
            {
                while (true)
                {
                    bb.clear();
                    if (rc.read(bb) == -1)
                    {
                        return null;
                    }
                    bb.flip();
                    count += bb.remaining();
                    while (bb.hasRemaining())
                    {
                        rc.write(bb);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                rc.close();
            }
            return null;
        }
    }
}
