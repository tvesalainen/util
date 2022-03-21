/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.j1939;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Poller<T> extends JavaLogging implements Runnable
{
    private final CachedScheduledThreadPool executor;
    private final Map<T,AtomicInteger> map = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Consumer<T> action;
    private final int limit;
    private final long millis;
    private Future<?> future;
    private int count;

    public Poller(Consumer<T> action, int limit, long period, TimeUnit unit)
    {
        this(new CachedScheduledThreadPool(), action, limit, period, unit);
    }
    public Poller(CachedScheduledThreadPool executor, Consumer<T> action, int limit, long period, TimeUnit unit)
    {
        super(Poller.class);
        this.executor = executor;
        this.action = action;
        this.limit = limit;
        this.millis = unit.toMillis(period);
        if (limit <= 0)
        {
            throw new IllegalArgumentException("illegal limit");
        }
    }

    public void enable(T item)
    {
        if (!map.containsKey(item))
        {
            lock.lock();
            try
            {
                map.put(item, new AtomicInteger());
                if (future == null)
                {
                    future = executor.submit(this);
                }
            }
            finally
            {
                lock.unlock();
            }
        }
    }
    public void disable(T item)
    {
        if (map.containsKey(item))
        {
            AtomicInteger ai = map.get(item);
            ai.set(limit);
        }
    }
    public void remove(T item)
    {
        map.remove(item);
    }
    @Override
    public void run()
    {
        int cnt = count;
        map.forEach((item, ai)->
        {
            try
            {
                if (ai.get() < limit)
                {
                    action.accept(item);
                    addCount();
                }
            }
            catch (Throwable ex)
            {
                ai.set(limit);
                log(SEVERE, ex, "run %s", ex.getMessage());
            }
            finally
            {
                ai.incrementAndGet();
            }
        });
        lock.lock();
        try
        {
            if (cnt != count)
            {
                future = executor.schedule(this, millis, TimeUnit.MILLISECONDS);
            }
            else
            {
                future = null;
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    private void addCount()
    {
        count++;
    }
}
