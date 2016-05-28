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

import java.util.concurrent.SynchronousQueue;

/**
 * This class is intended to generate Streams from observers. Observer calls 
 * provide while generate is used as supplier.
 * <p>This class simplifies SynchronousQueue by wrapping exceptions and hiding not
 * needed methods.
 * @author tkv
 * @param <T>
 * @see java.util.stream.Stream#generate(java.util.function.Supplier) 
 */
public class Generator<T>
{
    private SynchronousQueue<T> queue = new SynchronousQueue<>();
    /**
     * Provides new item to the generator. Return true if item was consumed. 
     * False if another thread was not waiting for the item.
     * @param t 
     * @return  
     */
    public boolean provide(T t)
    {
        return queue.offer(t);
    }
    /**
     * Returns item provided in different thread
     * @return 
     */
    public T generate()
    {
        try
        {
            return queue.take();
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
