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
package org.vesalainen.util.concurrent;

import java.util.Spliterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A Spliterator implementation for a case where implementation of tryAdvance
 * is difficult. This wrapper class implements tryAdvance by using another thread.
 * <p>
 * Timeout is how long we wait between actions without giving up.
 * @author tkv
 */
public class TryAdvanceSpliterator<T> implements Spliterator<T>, Runnable
{
    private Spliterator<T> spliterator;
    private SynchronousQueue<T> queue;
    private long timeout;
    private TimeUnit unit;
    private Thread caller;

    public TryAdvanceSpliterator(Spliterator<T> spliterator, long timeout, TimeUnit unit)
    {
        this.spliterator = spliterator;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        if (queue == null)
        {
            queue = new SynchronousQueue<>();
            caller = Thread.currentThread();
            Thread thread = new Thread(this, TryAdvanceSpliterator.class.getSimpleName());
            thread.start();
        }
        try
        {
            T t = queue.take();
            action.accept(t);
            return true;
        }
        catch (InterruptedException ex)
        {
            return false;
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action)
    {
        spliterator.forEachRemaining(action);
    }

    @Override
    public Spliterator<T> trySplit()
    {
        return spliterator.trySplit();
    }

    @Override
    public long estimateSize()
    {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics()
    {
        return spliterator.characteristics();
    }

    @Override
    public void run()
    {
        try
        {
            spliterator.forEachRemaining((t)->
            {
                try
                {
                    if (!queue.offer(t, timeout, unit))
                    {
                        caller.interrupt();
                        Thread.currentThread().interrupt();
                    }
                }
                catch (InterruptedException ex)
                {
                    throw new ThreadStoppedException(ex);
                }
            });
            caller.interrupt();
        }
        catch (ThreadStoppedException ex)
        {
        }
    }

}
