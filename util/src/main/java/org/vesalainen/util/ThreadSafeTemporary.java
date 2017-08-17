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

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * A thread-local weak-reference to object. 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class ThreadSafeTemporary<T>
{
    private final Supplier<T> factory;
    private final ThreadLocal<WeakReference<T>> threadLocal = new ThreadLocal<>();

    public ThreadSafeTemporary(Supplier<T> factory)
    {
        this.factory = factory;
    }
    
    public T get()
    {
        T t = null;
        WeakReference<T> wr = threadLocal.get();
        if (wr == null)
        {
            t = factory.get();
            wr = new WeakReference<>(t);
            threadLocal.set(wr);
        }
        else
        {
            t = wr.get();
        }
        if (t == null)
        {
            t = factory.get();
            wr = new WeakReference<>(t);
            threadLocal.set(wr);
        }
        return t;
    }
}
