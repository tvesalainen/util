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
package org.vesalainen.util.stream;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @deprecated Bad idea
 * This class is intended to generate Streams from observers. Observer calls 
 offer while generate is used as supplier.
 <p>This class simplifies SynchronousQueue by wrapping exceptions and hiding not
 * needed methods.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see java.util.stream.Stream#generate(java.util.function.Supplier) 
 */
public class ObserverSpliterator<T> implements Spliterator<T>
{
    private SynchronousQueue<T> queue = new SynchronousQueue<>();
    private long estimatedSize;
    private int characteristics;
    private Consumer<ObserverSpliterator> initializer;
    private Comparator<T> comparator;
    private long offerTimeout;
    private long takeTimeout;
    private TimeUnit timeUnit;
    /**
     * Creates infinite ObserverSpliterator without initializer.
     */
    public ObserverSpliterator()
    {
        this(Long.MAX_VALUE, 0, 0, Long.MAX_VALUE, TimeUnit.MILLISECONDS, null, null);
    }
    /**
     * Creates ObserverSpliterator with initializer
     * @param estimatedSize
     * @param characteristics
     * @param initializer 
     */
    public ObserverSpliterator(long estimatedSize, int characteristics, long offerTimeout, long takeTimeout, TimeUnit timeUnit, Comparator<T> comparator, Consumer<ObserverSpliterator> initializer)
    {
        this.estimatedSize = estimatedSize;
        this.characteristics = characteristics;
        this.offerTimeout = offerTimeout;
        this.takeTimeout = takeTimeout;
        this.timeUnit = timeUnit;
        this.comparator = comparator;
        this.initializer = initializer;
    }
    /**
     * Offers new item. Return true if item was consumed. 
     * False if another thread was not waiting for the item.
     * @param t 
     * @return  
     */
    public boolean offer(T t)
    {
        try
        {
            return queue.offer(t, offerTimeout, timeUnit);
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        try
        {
            if (initializer != null)
            {
                initializer.accept(this);
                initializer = null;
            }
            T item = queue.poll(takeTimeout, timeUnit);
            if (item != null)
            {
                action.accept(item);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (InterruptedException ex)
        {
            return false;
        }
    }

    @Override
    public Spliterator<T> trySplit()
    {
        if (initializer != null)
        {
            initializer.accept(this);
            initializer = null;
        }
        T item = queue.poll();
        if (item != null)
        {
            return new SingleSpliterator<>(item);
        }
        return null;
    }

    @Override
    public long estimateSize()
    {
        return estimatedSize;
    }

    @Override
    public int characteristics()
    {
        return characteristics;
    }

    @Override
    public Comparator<? super T> getComparator()
    {
        return comparator;
    }
    
}
