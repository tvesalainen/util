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
public class SSLConnectionTest
{
    private static SSLContext sslCtx;
    
    public SSLConnectionTest() throws IOException
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
        SSLSocketChannel sc11 = SSLSocketChannel.open("localhost", sa1.getPort(), sslCtx);
        
        byte[] exp = new byte[1024];
        Random random = new Random(98765);
        random.nextBytes(exp);
        bb.put(exp);
        bb.flip();
        sc11.write(bb);
        bb.clear();
        int rc = sc11.read(bb);
        assertEquals(1024, rc);
        byte[] array = bb.array();
        byte[] got = Arrays.copyOf(array, 1024);
        assertArrayEquals(exp, got);
        sc11.close();
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
            SSLSocketChannel sc = ssc.accept();
            ByteBuffer bb = ByteBuffer.allocate(2048);
            while (true)
            {
                int rc = sc.read(bb);
                if (rc == -1)
                {
                    return null;
                }
                System.err.println(HexDump.toHex(bb.array(), 0, bb.position()));
                bb.flip();
                sc.write(bb);
                bb.clear();
            }
        }
        
    }
    private static abstract class Server implements Callable<SSLSocketChannel>
    {
        protected final SSLServerSocketChannel ssc;

        public Server() throws IOException
        {
            ssc = SSLServerSocketChannel.open(null, sslCtx);
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        }

        public int getPort() throws IOException
        {
            InetSocketAddress local = (InetSocketAddress) ssc.getLocalAddress();
            return local.getPort();
        }
        
    }
    private static class Echo implements Callable<Void>
    {
        private ByteChannel rc;
        private ByteBuffer bb = ByteBuffer.allocate(2048);

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
                    rc.read(bb);
                    bb.flip();
                    rc.write(bb);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
