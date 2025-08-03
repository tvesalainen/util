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
package org.vesalainen.util.concurrent;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.vesalainen.util.function.IOFunction;
import org.vesalainen.util.function.IORunnable;
import org.vesalainen.util.function.IOSupplier;

/**
 * Routines run in locked mode.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Locks
{
    public static <T,R> R locked(Lock lock, T t, Function<T,R> func)
    {
        lock.lock();
        try
        {
            return func.apply(t);
        }
        finally
        {
            lock.unlock();
        }
    }
    public static <T> void locked(Lock lock, T t, Consumer<T> func)
    {
        lock.lock();
        try
        {
            func.accept(t);
        }
        finally
        {
            lock.unlock();
        }
    }
    public static void locked(Lock lock, Runnable func)
    {
        lock.lock();
        try
        {
            func.run();
        }
        finally
        {
            lock.unlock();
        }
    }
    public static <T> T locked(Lock lock, Supplier<T> func)
    {
        lock.lock();
        try
        {
            return func.get();
        }
        finally
        {
            lock.unlock();
        }
    }
    public static <T,R> R lockedIO(Lock lock, T t, IOFunction<T,R> func) throws IOException
    {
        lock.lock();
        try
        {
            return func.apply(t);
        }
        finally
        {
            lock.unlock();
        }
    }
    public static <T> T lockedIO(Lock lock, IOSupplier<T> func) throws IOException
    {
        lock.lock();
        try
        {
            return func.get();
        }
        finally
        {
            lock.unlock();
        }
    }
    public static void lockedIO(Lock lock, IORunnable func) throws IOException
    {
        lock.lock();
        try
        {
            func.run();
        }
        finally
        {
            lock.unlock();
        }
    }
}
