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
package org.vesalainen.util;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * A thread-local strong-reference to object. 
 * @author tkv
 * @param <T>
 */
public class ThreadSafe<T>
{
    private final Supplier<T> factory;
    private final ThreadLocal<T> threadLocal = new ThreadLocal<>();

    public ThreadSafe(Supplier<T> factory)
    {
        this.factory = factory;
    }
    
    public T get()
    {
        T t = threadLocal.get();
        if (t == null)
        {
            t = factory.get();
            threadLocal.set(t);
        }
        return t;
    }
    
    public void remove()
    {
        threadLocal.remove();
    }
}
