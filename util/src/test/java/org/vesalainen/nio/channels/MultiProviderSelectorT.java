/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.net.InetSocketAddress;
import static java.net.StandardSocketOptions.IP_MULTICAST_LOOP;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.comm.channel.SerialChannel.Builder;
import org.vesalainen.comm.channel.SerialChannel.Speed;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiProviderSelectorT
{
    
    public MultiProviderSelectorT()
    {
    }

    @Test
    public void testSelect()
    {
        //SerialChannel.debug(true);
        final ExecutorService exec = Executors.newCachedThreadPool();
        List<String> ports = SerialChannel.getFreePorts();
        assertNotNull(ports);
        assertTrue(ports.size() >= 2);
        try
        {
            MultiProviderSelector selector = new MultiProviderSelector();
            Builder builder1 = new Builder(ports.get(0), Speed.B1200)
                    .setBlocking(false);
            Builder builder2 = new Builder(ports.get(1), Speed.B1200)
                    .setBlocking(false);
            try (SerialChannel c1 = builder1.get();
                SerialChannel c2 = builder2.get();
                DatagramChannel dc = DatagramChannel.open();
                    )
            {
                
                dc.setOption(SO_BROADCAST, true);
                InetSocketAddress ba = new InetSocketAddress(10110);
                dc.bind(ba);
                dc.setOption(IP_MULTICAST_LOOP, false);
                dc.configureBlocking(false);
                dc.register(selector, OP_READ);
                final int count = 1000;
                SelectionKey skr1 = c1.register(selector, OP_READ, new Object[] {c1, new RandomChar(), ByteBuffer.allocateDirect(101), count});
                SelectionKey skr2 = c2.register(selector, OP_READ, new Object[] {c2, new RandomChar(), ByteBuffer.allocateDirect(102), count});
                TimerTask task = new TimerTask() {

                    @Override
                    public void run()
                    {
                        Transmitter tra1 = new Transmitter(c1, count);
                        Future<Void> ftra1 = exec.submit(tra1);
                        Transmitter tra2 = new Transmitter(c2, count);
                        Future<Void> ftra2 = exec.submit(tra2);
                        try
                        {
                            ftra1.get();
                            ftra2.get();
                        }
                        catch (InterruptedException ex)
                        {
                            fail(ex.getMessage());
                        }
                        catch (ExecutionException ex)
                        {
                            fail(ex.getMessage());
                        }
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000);
                while (selector.isOpen())
                {
                    int cnt = selector.select();
                    if (cnt > 0)
                    {
                        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                        while(keyIterator.hasNext())
                        {
                            SelectionKey sk = keyIterator.next();
                            if (sk.isReadable())
                            {
                                Object[] arr = (Object[]) sk.attachment();
                                SerialChannel sc = (SerialChannel) arr[0];
                                RandomChar rc = (RandomChar) arr[1];
                                ByteBuffer bb = (ByteBuffer) arr[2];
                                int c = (int) arr[3];
                                bb.clear();
                                sc.read(bb);
                                bb.flip();
                                while (bb.hasRemaining())
                                {
                                    int next = rc.next();
                                    byte cc = bb.get();
                                    assertEquals((byte)next, cc);
                                    c--;
                                }
                                assertTrue(c >= 0);
                                if (c == 0)
                                {
                                    sk.cancel();
                                }
                                arr[3] = c;
                            }
                            keyIterator.remove();
                        }
                    }
                    else
                    {
                        selector.close();
                    }
                }
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
}
