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
package org.vesalainen.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Recycler class is used to recycle Recyclable objects.
 * <p>This kind of recycling is for special cases only. Implementation has to take
 * care that recycled object is not referenced. This kind of recycling can leed
 * to hard to find bugs.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Recycler
{
    private static final MapList<Class<?>,Recyclable> mapList = new HashMapList<>();
    private static final Lock lock = new ReentrantLock();
    private static ArrayBlockingQueue<Recyclable> queue;
    private static Runner runner = new Runner();
    private static final JavaLogging log = new JavaLogging(Recycler.class);
    private static int Size = 1024;
    /**
     * Set size of inner queue. Default is 1024.
     * @param Size 
     */
    public static void setSize(int Size)
    {
        Recycler.Size = Size;
    }

    /**
     * Returns new or recycled uninitialized object.
     * @param <T>
     * @param cls
     * @return 
     */
    public static final <T extends Recyclable> T get(Class<T> cls)
    {
        return get(cls, null);
    }
    /**
     * Returns new or recycled initialized object.
     * @param <T>
     * @param cls
     * @param initializer
     * @return 
     */
    public static final <T extends Recyclable> T get(Class<T> cls, Consumer<T> initializer)
    {
        T recyclable = null;
        lock.lock();
        try
        {
            List<Recyclable> list = mapList.get(cls);
            if (list != null && !list.isEmpty())
            {
                recyclable = (T) list.remove(list.size()-1);
                log.debug("get recycled %s", recyclable);
            }
        }
        finally
        {
            lock.unlock();
        }
        if (recyclable == null)
        {
            try
            {
                recyclable = cls.newInstance();
                log.debug("create new recycled %s", recyclable);
            }
            catch (InstantiationException | IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        if (initializer != null)
        {
            initializer.accept(recyclable);
        }
        return (T) recyclable;
    }
    /**
     * Add objects to be recycled.
     * @param <T>
     * @param recyclables 
     */
    public static final <T extends Recyclable> void recycle(Collection<T> recyclables)
    {
        recyclables.stream().forEach((recyclable) ->
        {
            recycle(recyclable);
        });
    }
    /**
     * Add objects to be recycled.
     * @param <T>
     * @param recyclables 
     */
    public static final <T extends Recyclable> void recycle(T... recyclables)
    {
        for (Recyclable recyclable : recyclables)
        {
            recycle(recyclable);
        }
    }
    /**
     * Add object to be recycled.
     * @param <T>
     * @param recyclable 
     */
    public static final <T extends Recyclable> void recycle(T recyclable)
    {
        lock.lock();
        try
        {
            if (queue == null)
            {
                queue = new ArrayBlockingQueue<>(Size);
                Thread thread = new Thread(runner, Recycler.class.getSimpleName());
                thread.start();
                log.info("start thread %s", Recycler.class.getSimpleName());
            }
            if (!queue.offer(recyclable))
            {
                log.warning("failed to recycle %s, queue is full", recyclable);
            }
            else
            {
                log.debug("put to recycle queue %s", recyclable);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    
    static final <T extends Recyclable> boolean isRecycled(T recyclable)
    {
        return mapList.contains(recyclable.getClass(), recyclable);
    }

    private static class Runner implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    Recyclable recyclable = queue.poll(1, TimeUnit.MINUTES);
                    lock.lock();
                    try
                    {
                        if (recyclable == null)
                        {
                            queue = null;
                            log.info("stop thread %s", Recycler.class.getSimpleName());
                            return;
                        }
                        if (recyclable.isRecycled())
                        {
                            queue = null;
                            log.severe("recycling %s again", recyclable);
                            throw new IllegalArgumentException("recycling "+recyclable+" again");
                        }
                        recyclable.clear();
                        mapList.add(recyclable.getClass(), recyclable);
                        log.debug("recycled %s", recyclable);
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                catch (InterruptedException ex)
                {
                }
            }
        }
    }
}
