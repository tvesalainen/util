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

import java.util.concurrent.SynchronousQueue;
import org.vesalainen.util.FloatReference;
import org.vesalainen.util.Recycler;

/**
 * This class is intended to generate Streams from observers. Observer calls 
 * provide while generate is used as supplier.
 * <p>This class simplifies SynchronousQueue by wrapping exceptions and hiding not
 * needed methods.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see java.util.stream.Stream#generate(java.util.function.Supplier) 
 */
public class FloatGenerator<T>
{
    private SynchronousQueue<FloatReference> queue = new SynchronousQueue<>();
    /**
     * Provides new item to the generator 
     * @param value
     */
    public boolean provide(float value)
    {
        FloatReference ref = new FloatReference(value);
        boolean res = queue.offer(ref);
        return res;
    }
    /**
     * Returns item provided in different thread
     * @return 
     */
    public float generate()
    {
        try
        {
            FloatReference ref = queue.take();
            float value = ref.getValue();
            return value;
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
