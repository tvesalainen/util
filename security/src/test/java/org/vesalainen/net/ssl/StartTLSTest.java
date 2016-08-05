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
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class StartTLSTest
{
    private static SSLContext sslCtx;
    
    public StartTLSTest() throws IOException
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINEST);
        Security.addProvider(new BouncyCastleProvider());
        sslCtx = TestSSLContext.getInstance();
    }

    @Test
    public void test() throws IOException, InterruptedException, ExecutionException
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        ByteBuffer bb = ByteBuffer.allocate(2048);
        
        PassiveServer sa1 = new PassiveServer();
        Future<SSLSocketChannel> f1 = executor.submit(sa1);
        SocketChannel sc11 = SocketChannel.open(new InetSocketAddress("localhost", sa1.getPort()));
        
        bb.put("Start TLS\r\n".getBytes());
        bb.flip();
        sc11.write(bb);
        SSLSocketChannel ssc1 = SSLSocketChannel.open(sc11, sslCtx, null, true);
        sc11 = null;
        ssc1.setUseClientMode(true);
        bb.clear();
        byte[] exp = new byte[1024];
        Random random = new Random(98765);
        random.nextBytes(exp);
        bb.put(exp);
        bb.flip();
        ssc1.write(bb);
        bb.clear();
        int rc = ssc1.read(bb);
        assertEquals(1024, rc);
        byte[] array = bb.array();
        byte[] got = Arrays.copyOf(array, 1024);
        assertArrayEquals(exp, got);
        ssc1.close();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }
    private static class PassiveServer extends Server
    {

        public PassiveServer() throws IOException
        {
        }
        
        @Override
        public SSLSocketChannel call() throws Exception
        {
            try
            {
                boolean tls = false;
                SocketChannel sc = sss.accept();
                ByteChannel bc = sc;
                ByteBuffer bb = ByteBuffer.allocate(2048);
                while (true)
                {
                    int rc = bc.read(bb);
                    if (rc == -1)
                    {
                        return null;
                    }
                    System.err.println(HexDump.toHex(bb.array(), 0, bb.position()));
                    bb.flip();
                    while (!tls)
                    {
                        byte cc = bb.get();
                        if (cc == '\n')
                        {
                            tls = true;
                            bc = SSLSocketChannel.open(sc, sslCtx, bb, true);
                        }
                    }
                    bc.write(bb);
                    bb.clear();
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
        
    }
    private static abstract class Server implements Callable<SSLSocketChannel>
    {
        protected final ServerSocketChannel sss;

        public Server() throws IOException
        {
            sss = ServerSocketChannel.open();
            sss.bind(null);
            sss.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        }

        public int getPort() throws IOException
        {
            InetSocketAddress local = (InetSocketAddress) sss.getLocalAddress();
            return local.getPort();
        }
        
    }
}
