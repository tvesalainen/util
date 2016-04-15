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

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author tkv
 * @param <T>
 */
public class WaiterList<T> extends ConcurrentLinkedDeque<T>
{
    public enum Result {Ok, Timeout, Interrupted};
    private final Semaphore semaphore = new Semaphore(0);
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Adds waiter to queue and waits until releaseAll method call or thread is 
     * interrupted.
     * @param waiter
     * @return 
     */
    public Result wait(T waiter)
    {
        return wait(waiter, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * Adds waiter to queue and waits until releaseAll method call or thread is 
     * interrupted or timeout.
     * @param waiter
     * @param timeout
     * @param unit
     * @return 
     */
    public Result wait(T waiter, long timeout, TimeUnit unit)
    {
        try
        {
            Locks.locked(lock, waiter, (w)->{add(w);});
            if (semaphore.tryAcquire(timeout, unit))
            {
                remove(waiter);
                return Result.Ok;
            }
            else
            {
                remove(waiter);
                return Result.Timeout;
            }
        }
        catch (InterruptedException ex)
        {
            remove(waiter);
            return Result.Interrupted;
        }
    }
    /**
     * Releases all waiters.
     */
    public void releaseAll()
    {
        Locks.locked(lock, semaphore, (s)->{s.release(size());s.drainPermits();});  // it is possible that deque was changed during release
    }
}
