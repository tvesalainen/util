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
package org.vesalainen.util.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;

/**
 * This class is intended to generate Streams from observers. Observer calls 
 offer while generate is used as supplier.
 <p>This class simplifies SynchronousQueue by wrapping exceptions and hiding not
 * needed methods.
 * @author tkv
 * @param <T>
 * @see java.util.stream.Stream#generate(java.util.function.Supplier) 
 */
public class ObserverSpliterator<T> implements Spliterator<T>
{
    private SynchronousQueue<T> queue = new SynchronousQueue<>();
    private long estimatedSize;
    private int characteristics;

    public ObserverSpliterator()
    {
        this(Long.MAX_VALUE, 0);
    }

    public ObserverSpliterator(long estimatedSize, int characteristics)
    {
        this.estimatedSize = estimatedSize;
        this.characteristics = characteristics;
    }
    
    /**
     * Offers new item. Return true if item was consumed. 
     * False if another thread was not waiting for the item.
     * @param t 
     * @return  
     */
    public boolean offer(T t)
    {
        return queue.offer(t);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        try
        {
            action.accept(queue.take());
            return true;
        }
        catch (InterruptedException ex)
        {
            return false;
        }
    }

    @Override
    public Spliterator<T> trySplit()
    {
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
}
