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

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Routines run in locked mode.
 * @author tkv
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
}
