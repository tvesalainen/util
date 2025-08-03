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

import java.util.Deque;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * This class is for case where several threads are waiting an event. When that
 * event occurs, all threads are released. During the waiting time waiter items
 * are accessible using stream, parallelStream and spliterator methods.
 * 
 * <p>After releaseAll all wait methods return Reason.Release and all other methods
 * throw IllegalStateException.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class WaiterList<T>
{
    public enum Reason {Release, Timeout, Interrupt};
    private Deque<T> list = new ConcurrentLinkedDeque<>();
    private Semaphore semaphore = new Semaphore(0);
    private ReentrantLock lock = new ReentrantLock();
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
     * <p>If releaseAll is called already return Release.
     * @param waiter
     * @param timeout
     * @param unit
     * @return 
     */
    public Reason wait(T waiter, long timeout, TimeUnit unit)
    {
        if (list == null)
        {
            return Reason.Release;
        }
        try
        {
            Locks.locked(lock, waiter, (w)->{list.add(w);});
            if (semaphore.tryAcquire(timeout, unit))
            {
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
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        Locks.locked(lock, semaphore, (s)->{s.release(Integer.MAX_VALUE);});
        list = null;
        semaphore = null;
        lock = null;
    }
    public Stream<T> stream()
    {
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        return list.stream();
    }
    public Stream<T> parallelStream()
    {
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        return list.parallelStream();
    }
    public Spliterator<T> spliterator()
    {
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        return list.spliterator();
    }
    public int size()
    {
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        return list.size();
    }
    public boolean isEmpty()
    {
        if (list == null)
        {
            throw new IllegalStateException("releaseAll is called already");
        }
        return list.isEmpty();
    }
}
