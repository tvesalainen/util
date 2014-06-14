/*
 * Copyright (C) 2014 tkv
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * SimpleWorkflow creates a workflow using several threads. Mostly single
 * thread is allowed to run at the same time.
 * @author tkv
 * @param <K> Thread key
 */
public abstract class SimpleWorkflow<K>
{
    private final Map<K,Thread> threadMap;
    private final Map<Thread,Semaphore> semaphoreMap;
    private final Semaphore stopSemaphore = new Semaphore(1);
    /**
     * Constructs a new workflow. Current thread is named to start.
     * @param start Name of current thread.
     */
    public SimpleWorkflow(K start)
    {
        this.semaphoreMap = new HashMap<>();
        this.threadMap = new HashMap<>();
        Thread currentThread = Thread.currentThread();
        threadMap.put(start, currentThread);
        semaphoreMap.put(currentThread, new Semaphore(0));
    }
    
    /**
     * Fork executing thread. 
     * @param to Next executing
     */
    public void fork(K to)
    {
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            stopSemaphore.acquire();
            System.err.println("acquire="+stopSemaphore.availablePermits());
            Thread nextThread = threadMap.get(to);
            if (nextThread == null)
            {
                Runnable runnable = create(to);
                nextThread = new Thread(runnable, to.toString());
                Semaphore semaphore = new Semaphore(0);
                threadMap.put(to, nextThread);
                semaphoreMap.put(nextThread, semaphore);
                nextThread.start();
                stopSemaphore.release();
            }
            else
            {
                Semaphore semaphore = semaphoreMap.get(nextThread);
                semaphore.release();
            }
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public void join()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        try
        {
            stopSemaphore.release();
            System.err.println("release="+stopSemaphore.availablePermits());
            Thread currentThread = Thread.currentThread();
            Semaphore currentSemaphore = semaphoreMap.get(currentThread);
            if (currentSemaphore == null)
            {
                throw new IllegalStateException("Current thread is not workflow thread");
            }
            currentSemaphore.acquire();
        }
        catch (InterruptedException ex)
        {
            throw new ThreadStoppedException(ex);
        }
    }
    /**
     * Switch executing thread. 
     * @param to Next executing
     */
    public void switchTo(K to)
    {
        fork(to);
        join();
    }
    
    public int getThreadCount()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        return threadMap.size();
    }
    public void kill(K key)
    {
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            Thread thread = threadMap.get(key);
            threadMap.remove(key);
            semaphoreMap.remove(thread);
            thread.interrupt();
            stopSemaphore.acquire();
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public void stopThreads()
    {
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            System.err.println("permits="+stopSemaphore.availablePermits());
            stopSemaphore.acquire(threadMap.size());
            Thread currentThread = Thread.currentThread();
            for (Thread thread : threadMap.values())
            {
                if (!currentThread.equals(thread))
                {
                    thread.interrupt();
                }
            }
            threadMap.clear();
            semaphoreMap.clear();
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Creates a Runnable implementation for key.
     * @param key Key for Runnable object
     * @return new Runnable implementation associated with the key.
     */
    protected abstract Runnable create(K key);

}
