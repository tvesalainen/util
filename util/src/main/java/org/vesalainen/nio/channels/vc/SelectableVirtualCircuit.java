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
package org.vesalainen.nio.channels.vc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_READ;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.logging.JavaLogging;

/**
 * SelectableVirtualCircuit implements VirtualCircuit by using selector. Uses 
 * one thread.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SelectableVirtualCircuit extends JavaLogging implements VirtualCircuit, Callable<Void>
{
    private SelectableChannel c1;
    private SelectableChannel c2;
    private ByteBuffer bb;
    private Future<Void> future;
    private ByteChannel[] bc;
    /**
     * Creates SelectableVirtualCircuit
     * @param c1
     * @param c2
     * @param capacity Buffer size
     * @param direct ByteBuffer type
     */
    public SelectableVirtualCircuit(SelectableChannel c1, SelectableChannel c2, int capacity, boolean direct)
    {
        this(c1, c2, (ByteChannel)c1, (ByteChannel)c2, capacity, direct);
    }
    /**
     * Creates SelectableVirtualCircuit between two ByteChannels. Both 
     * ByteChannels are selectable by using SelectableChannel. Connected
     * ByteChannel and SelectableChannel can be the same.
     * @param c1
     * @param c2
     * @param bc1
     * @param bc2
     * @param capacity
     * @param direct 
     */
    public SelectableVirtualCircuit(SelectableChannel c1, SelectableChannel c2, ByteChannel bc1, ByteChannel bc2, int capacity, boolean direct)
    {
        super(SelectableVirtualCircuit.class);
        this.c1 = c1;
        this.c2 = c2;
        this.bc = new ByteChannel[] {bc1, bc2};
        if (direct)
        {
            this.bb = ByteBuffer.allocateDirect(capacity);
        }
        else
        {
            this.bb = ByteBuffer.allocate(capacity);
        }
    }
    /**
     * Start virtual circuit
     * @param executorFactory
     * @throws IOException 
     */
    @Override
    public void start(Supplier<ExecutorService> executorFactory) throws IOException
    {
        future = executorFactory.get().submit(this);
    }
    /**
     * Wait until other peer has closed connection or virtual circuit is closed 
     * by interrupt.
     * @throws IOException 
     */
    @Override
    public void waitForFinish() throws IOException
    {
        if (future == null)
        {
            throw new IllegalStateException("not started");
        }
        try
        {
            future.get();
        }
        catch (InterruptedException | ExecutionException ex)
        {
            throw new IOException(ex);
        }
    }
    /**
     * Cancel the virtual circuit.
     * @throws IOException 
     */
    @Override
    public void stop() throws IOException
    {
        if (future == null)
        {
            throw new IllegalStateException("not started");
        }
        future.cancel(true);
    }

    @Override
    public void join(Supplier<ExecutorService> executorFactory) throws IOException
    {
        try
        {
            call();
        }
        catch (Exception ex)
        {
            throw new IOException(ex);
        }
    }

    @Override
    public Void call() throws Exception
    {
        int up = 0;
        int down = 0;
        Selector selector = null;
        try
        {
            fine("start: %s / %s", bc[0], bc[1]);
            SelectorProvider provider = SelectorProvider.provider();
            selector = provider.openSelector();
            c1.configureBlocking(false);
            c1.register(selector, OP_READ, bc[1]);
            c2.configureBlocking(false);
            c2.register(selector, OP_READ, bc[0]);
            while (!selector.keys().isEmpty())
            {
                int count = selector.select();
                if (count > 0)
                {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext())
                    {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        ByteChannel target = (ByteChannel) selectionKey.attachment();
                        ByteChannel source = target == bc[0] ? bc[1] : bc[0];
                        boolean upld = source == c1;
                        bb.clear();
                        int rc = source.read(bb);
                        debug("VC read=%d from %s", rc, source);
                        int cnt = 0;
                        while (rc > 0)
                        {
                            cnt += rc;
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
                            if (upld)
                            {
                                up += cnt;
                                debug("VC %s --> %d %s", source, cnt, target);
                            }
                            else
                            {
                                down += cnt;
                                debug("VC %s <-- %d %s", target, cnt, source);
                            }
                        }
                        if (rc == -1)
                        {
                            fine("VC %s quit", source);
                            return null;
                        }
                    }
                }
            }
            return null;
        }
        catch (Exception ex)
        {
            log(Level.SEVERE, ex, "SVC %s", ex.getMessage());
            throw ex;
        }
        finally
        {
            fine("VC end: up=%d down=%d %s / %s", up, down, bc[0], bc[1]);
            if (selector != null)
            {
                selector.close();
            }
            bc[0].close();
            bc[1].close();
        }
    }
    
}
