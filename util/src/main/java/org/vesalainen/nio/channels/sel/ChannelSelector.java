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
 *
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

    public ChannelSelector()
    {
        super(ChannelSelector.class);
    }
    
    public boolean isOpen()
    {
        return !executor.isShutdown();
    }

    public SelectorProvider provider()
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Set<SelKey> keys()
    {
        return unmodifiableKeys;
    }

    public Set<SelKey> selectedKeys()
    {
        return selectedKeys;
    }

    public int selectNow() throws IOException
    {
        fine("selectNow()");
        return select(0);
    }

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

    public int select() throws IOException
    {
        fine("select()");
        return select(Long.MAX_VALUE);
    }

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
