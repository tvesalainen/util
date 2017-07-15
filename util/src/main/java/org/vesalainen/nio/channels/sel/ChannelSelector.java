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
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.vesalainen.nio.channels.sel.SelKey.Op;
import org.vesalainen.util.concurrent.ConcurrentArraySet;
import org.vesalainen.util.logging.JavaLogging;

/**
 * ChannelSelector is replacement for java.nio.channels.Selector. Behavior is 
 * similar but differs in many points.
 * @author tkv
 */
public class ChannelSelector extends JavaLogging implements AutoCloseable
{
    private static final SelKey WAKEUP_KEY = new SelKey(null, null, null, Op.OP_READ);
    private BlockingQueue<SelKey> queue = new LinkedBlockingQueue<>();
    private Map<SelChannel,Future> runMap = new ConcurrentHashMap<>();
    private Set<SelKey> keys = new ConcurrentArraySet<>();
    private final Set<SelKey> unmodifiableKeys = Collections.unmodifiableSet(keys);
    private Set<SelKey> selectedKeys = new ConcurrentArraySet<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Creates ChannelSelector
     */
    public ChannelSelector()
    {
        super(ChannelSelector.class);
    }
    /**
     * Returns true if selector is not closed.
     * @return 
     */
    public boolean isOpen()
    {
        return !executor.isShutdown();
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
     * Returns unmodifiable keys-set.
     * @return 
     */
    public Set<SelKey> keys()
    {
        return unmodifiableKeys;
    }
    /**
     * Returns set of selected keys after one-of select methods is called.
     * It is allowed and desired to remove entries after processed.
     * @return 
     */
    public Set<SelKey> selectedKeys()
    {
        return selectedKeys;
    }
    /**
     * Selects and returns immediately.
     * @return
     * @throws IOException 
     */
    public int selectNow() throws IOException
    {
        fine("selectNow()");
        return select(0);
    }
    /**
     * This method calls func as channels becomes ready as long as time
     * between calls doesn't exceed timeout and thread is not interrupted.
     * <p>
     * Most other methods are blocked during this call.
     * @param func
     * @param timeout
     * @throws IOException 
     */
    public void forEach(Consumer<SelKey> func, long timeout) throws IOException
    {
        fine("forEach("+timeout+")");
        lock.lock();
        try
        {
            ensureAllRunning();
            while (true)
            {
                try
                {
                    SelKey sk = queue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (sk != null)
                    {
                        if (sk == WAKEUP_KEY)
                        {
                            fine("forEach() got wakeup()");
                            return;
                        }
                        func.accept(sk);
                        SelChannel channel = sk.channel();
                        Future<?> future = executor.submit(channel);
                        runMap.put(channel, future);
                    }
                    else
                    {
                        fine("forEach() got timeout");
                        return;
                    }
                }
                catch (InterruptedException ex)
                {
                    fine("forEach() interrupted");
                    return;
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    private void ensureAllRunning()
    {
        for (SelKey sk : keys)
        {
            SelChannel channel = sk.channel();
            Future future = runMap.get(channel);
            if (future == null || future.isDone())
            {
                if (isLoggable(Level.FINEST))
                {
                    if (future == null)
                    {
                        finest("first start of %s", channel);
                    }
                    else
                    {
                        if (future.isCancelled())
                        {
                            finest("restart after cancel of %s", channel);
                        }
                        else
                        {
                            if (future.isDone())
                            {
                                finest("restart of %s", channel);
                            }
                        }
                    }
                }
                future = executor.submit(channel);
                runMap.put(channel, future);
            }
        }
    }
    /**
     * Selects and waits .
     * @param timeout Milliseconds.
     * @return
     * @throws IOException 
     */
    public int select(long timeout) throws IOException
    {
        fine("select("+timeout+")");
        lock.lock();
        try
        {
            ensureAllRunning();
            try
            {
                SelKey sk = queue.poll(timeout, TimeUnit.MILLISECONDS);
                if (sk != null)
                {
                    selectedKeys.add(sk);
                    queue.drainTo(selectedKeys);
                    selectedKeys.remove(WAKEUP_KEY);
                    return selectedKeys.size();
                }
                else
                {
                    fine("select() timeout");
                    return 0;
                }
            }
            catch (InterruptedException ex)
            {
                fine("select() interrupted");
                return 0;
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    /**
     * Selects and wait.
     * @return
     * @throws IOException 
     */
    public int select() throws IOException
    {
        fine("select()");
        return select(Long.MAX_VALUE);
    }
    /**
     * Causes running select/forEach to stop immediately. If select/forEach was
     * not running while this method was called the the next call will stop.
     * @return 
     */
    public ChannelSelector wakeup()
    {
        fine("wakeup()");
        queue.add(WAKEUP_KEY);
        return this;
    }

    @Override
    public void close() throws IOException
    {
        fine("close()");
        lock.lock();
        try
        {
            executor.shutdownNow();
        }
        finally
        {
            lock.unlock();
        }
    }

    SelKey register(SelChannel channel, Op ops, Object att)
    {
        config("register(%s, %s, %s)", channel, ops, att);
        lock.lock();
        try
        {
            SelKey sk = new SelKey(channel, this, att, ops);
            keys.add(sk);
            return sk;
        }
        finally
        {
            lock.unlock();
        }
    }

    void unregister(SelChannel channel)
    {
        config("unregister(%s)", channel);
        lock.lock();
        try
        {
            Future future = runMap.get(channel);
            if (future != null)
            {
                future.cancel(true);
                runMap.remove(channel);
            }
            SelKey sk = channel.getKey();
            keys.remove(sk);
            selectedKeys.remove(sk);
        }
        finally
        {
            lock.unlock();
        }
    }

    void ready(SelChannel channel)
    {
        fine("ready(%s)", channel);
        queue.add(channel.getKey());
    }

    void exception(SelChannel channel, IOException ex)
    {
        log(Level.SEVERE, ex, "exception(%s, %s)", channel, ex.getMessage());
    }
    
}
