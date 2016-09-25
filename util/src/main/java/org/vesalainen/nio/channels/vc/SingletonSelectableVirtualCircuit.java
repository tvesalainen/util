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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.logging.JavaLogging;

/**
 * SingletonSelectableVirtualCircuit handles several SelectableChannel
 * virtual circuits using only one thread.
 * @author tkv
 */
public class SingletonSelectableVirtualCircuit extends JavaLogging implements Runnable
{
    private Thread thread;
    private AbstractSelector selector;
    private Map<ByteChannel,ByteChannel> channelMap;
    private Map<ByteChannel,SelectionKey> selectionKeyMap;
    private ByteBuffer bb;
    private int capacity;
    private boolean direct;
    /**
     * Creates new SingletonSelectableVirtualCircuit.
     * @param capacity ByteBuffer size
     * @param direct If true ByteBuffer is direct.
     */
    public SingletonSelectableVirtualCircuit(int capacity, boolean direct)
    {
        super(SingletonSelectableVirtualCircuit.class);
        this.capacity = capacity;
        this.direct = direct;
    }
    /**
     * Join channel pair to this virtual circuit.
     * <p>Note! Channels have to implement ByteChannel interface.
     * @param ch1
     * @param ch2
     * @throws IOException 
     */
    public void join(SelectableChannel ch1, SelectableChannel ch2) throws IOException
    {
        join(ch1, ch2, (ByteChannel)ch1, (ByteChannel)ch2);
    }
    /**
     * Join channel pair to this virtual circuit
     * @param ch1
     * @param ch2
     * @throws IOException 
     */
    public void join(SelectableBySelector ch1, SelectableBySelector ch2) throws IOException
    {
        join(ch1.getSelector(), ch2.getSelector(), (ByteChannel)ch1, (ByteChannel)ch2);
    }
    private synchronized void join(SelectableChannel ch1, SelectableChannel ch2, ByteChannel bc1, ByteChannel bc2) throws IOException
    {
        start();
        channelMap.put(bc1, bc2);
        channelMap.put(bc2, bc1);
        ch1.configureBlocking(false);
        SelectionKey sk1 = ch1.register(selector, OP_READ, bc2);
        ch2.configureBlocking(false);
        SelectionKey sk2 = ch2.register(selector, OP_READ, bc1);
        selectionKeyMap.put(bc1, sk1);
        selectionKeyMap.put(bc2, sk2);
    }
    private synchronized void start() throws IOException
    {
        if (thread == null)
        {
            SelectorProvider provider = SelectorProvider.provider();
            channelMap = new HashMap<>();
            selectionKeyMap = new HashMap<>();
            if (direct)
            {
                this.bb = ByteBuffer.allocateDirect(capacity);
            }
            else
            {
                this.bb = ByteBuffer.allocate(capacity);
            }
            selector = provider.openSelector();
            thread = new Thread(this, SingletonSelectableVirtualCircuit.class.getSimpleName());
            thread.start();
        }
    }
    private synchronized boolean stop()
    {
        if (selector.keys().isEmpty())
        {
            thread.interrupt();
            assert channelMap.isEmpty();
            channelMap = null;
            assert selectionKeyMap.isEmpty();
            selectionKeyMap = null;
            bb = null;
            selector = null;
            thread = null;
            return true;
        }
        else
        {
            return false;
        }
    }
    @Override
    public void run()
    {
        do
        {
            try
            {
                while (!selector.keys().isEmpty())
                {
                    int count = selector.select();
                    if (count > 0)
                    {
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext())
                        {
                            try
                            {
                                SelectionKey selectionKey = iterator.next();
                                iterator.remove();
                                ByteChannel target = (ByteChannel) selectionKey.attachment();
                                ByteChannel source = channelMap.get(target);
                                bb.clear();
                                int rc = source.read(bb);
                                debug("VC read=%d from %s", rc, source);
                                while (rc > 0)
                                {
                                    bb.flip();
                                    debug("VC \n%s", HexDump.remainingToHex(bb));
                                    while (bb.hasRemaining())
                                    {
                                        int wr = target.write(bb);
                                        debug("VC wrote=%d to %s", wr, target);
                                    }
                                    bb.clear();
                                    rc = source.read(bb);
                                    debug("VC read=%d from %s", rc, source);
                                }
                                if (rc == -1)
                                {
                                    fine("VC %s quit", source);
                                    SelectionKey skt = selectionKeyMap.get(target);
                                    SelectionKey sks = selectionKeyMap.get(source);
                                    channelMap.remove(target);
                                    channelMap.remove(source);
                                    selectionKeyMap.remove(target);
                                    selectionKeyMap.remove(source);
                                    skt.cancel();
                                    sks.cancel();
                                    target.close();
                                    source.close();
                                }
                            }
                            catch (Exception ex)
                            {
                                log(Level.SEVERE, ex, "SVC %s", ex.getMessage());
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                log(Level.SEVERE, ex, "SVC %s", ex.getMessage());
            }
        } while (!stop());
        fine("VC loop ended");
    }
    
}
