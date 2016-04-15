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

import java.util.Deque;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * 
 * @author tkv
 * @param <T>
 */
public class WaiterList<T>
{
    public enum Reason {Release, Timeout, Interrupt};
    private Deque<T> list = new ConcurrentLinkedDeque<>();
    private final Semaphore semaphore = new Semaphore(0);
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Adds waiter to queue and waits until releaseAll method call or thread is 
     * interrupted.
     * @param waiter
     * @return 
     */
    public Reason wait(T waiter)
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
    public Reason wait(T waiter, long timeout, TimeUnit unit)
    {
        try
        {
            Locks.locked(lock, waiter, (w)->{list.add(w);});
            if (semaphore.tryAcquire(timeout, unit))
            {
                list.remove(waiter);
                return Reason.Release;
            }
            else
            {
                list.remove(waiter);
                return Reason.Timeout;
            }
        }
        catch (InterruptedException ex)
        {
            list.remove(waiter);
            return Reason.Interrupt;
        }
    }
    /**
     * Releases all waiters.
     */
    public void releaseAll()
    {
        Locks.locked(lock, semaphore, (s)->{s.release(list.size());s.drainPermits();});  // it is possible that deque was changed during release
    }
    public Stream<T> stream()
    {
        return list.stream();
    }
    public Spliterator<T> spliterator()
    {
        return list.spliterator();
    }
    public int size()
    {
        return list.size();
    }
    public boolean isEmpty()
    {
        return list.isEmpty();
    }
}
