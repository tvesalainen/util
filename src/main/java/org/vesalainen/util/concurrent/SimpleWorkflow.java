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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SimpleWorkflow creates a workflow using several threads. 
 * 
 * <p>Running thread activates another by using switchTo or fork method. 
 * Parallel excecution is possible between fork and join methods. Using switchTo
 * is same as calling fork and join.
 * @author tkv
 * @param <K> Thread key
 */
public abstract class SimpleWorkflow<K>
{
    private final K id;
    private final Map<K,Thread> threadMap;
    private final Map<Thread,Semaphore> semaphoreMap;
    private final Semaphore stopSemaphore;
    private final int maxParallelism;
    private final int timeout;
    private final TimeUnit timeUnit;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    
    /**
     * Creates a new workflow. Current thread is named to start.
     * Maximum number of parallel running threads = 1. No timeout for idle threads.
     * @param start Name of current thread.
     */
    public SimpleWorkflow(K start)
    {
        this(start, 1, 0, TimeUnit.MILLISECONDS);
    }
    /**
     * Creates a new workflow. Current thread is named to start.
     * @param start Name of current thread.
     * @param maxParallelism Maximum number of parallel running threads.
     * @param timeout Timeout for idle thread to be stopped.
     * @param timeUnit Time unit for idle thread to be stopped.
     */
    public SimpleWorkflow(K start, int maxParallelism, int timeout, TimeUnit timeUnit)
    {
        if (maxParallelism < 0)
        {
            throw new IllegalArgumentException("maxParallelism < 0");
        }
        this.id = start;
        this.maxParallelism = maxParallelism;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.stopSemaphore = new Semaphore(maxParallelism);
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
        if (maxParallelism == 0)
        {
            throw new IllegalArgumentException("maxParallelism == 0, fork() not allowed! Use switchTo.");
        }
        try
        {
            stopSemaphore.acquire();
            //System.err.println("forked="+stopSemaphore.availablePermits());
            doFork(to);
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private void doFork(K to)
    {
        writeLock.lock();
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            Thread nextThread = threadMap.get(to);
            if (nextThread == null)
            {
                Runnable runnable = create(to);
                runnable = new Wrapper(runnable);
                nextThread = new Thread(runnable, to.toString());
                Semaphore semaphore = new Semaphore(0);
                threadMap.put(to, nextThread);
                semaphoreMap.put(nextThread, semaphore);
                nextThread.start();
                //System.err.println("created="+stopSemaphore.availablePermits());
            }
            else
            {
                Semaphore semaphore = semaphoreMap.get(nextThread);
                semaphore.release();
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }
    public void join()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        stopSemaphore.release();
        //System.err.println("joined="+stopSemaphore.availablePermits());
        doJoin();
    }
    private void doJoin()
    {
        try
        {
            Thread currentThread = Thread.currentThread();
            Semaphore currentSemaphore = semaphoreMap.get(currentThread);
            if (currentSemaphore == null)
            {
                throw new IllegalStateException("Current thread is not workflow thread");
            }
            boolean success = currentSemaphore.tryAcquire(timeout, timeUnit);
            if (!success)
            {
                kill(id);
            }
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
        doFork(to);
        doJoin();
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
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        writeLock.lock();
        try
        {
            Thread thread = threadMap.get(key);
            threadMap.remove(key);
            semaphoreMap.remove(thread);
            thread.interrupt();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    /**
     * Wait until parallel excecuting threads have joined. After that interrupt
     * all other than the calling thread.
     */
    public void waitAndStopThreads()
    {
        readLock.lock();
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            //System.err.println("waiting stopping="+stopSemaphore.availablePermits());
            stopSemaphore.acquire(maxParallelism);
            //System.err.println("released stopping="+stopSemaphore.availablePermits());
            stopThreads();
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        finally
        {
            readLock.unlock();
        }
    }
    /**
     * Interrupt
     * all other than the calling thread.
     */
    public void stopThreads()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        writeLock.lock();
        try
        {
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
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Creates a Runnable implementation for key.
     * @param key Key for Runnable object
     * @return new Runnable implementation associated with the key.
     */
    protected abstract Runnable create(K key);

    public class Wrapper implements Runnable
    {
        private Runnable runner;

        public Wrapper(Runnable runner)
        {
            this.runner = runner;
        }
        
        @Override
        public void run()
        {
            try
            {
                runner.run();
            }
            catch (Throwable oex)
            {
                Throwable ex = oex;
                while (ex != null)
                {
                    if (ex instanceof ThreadStoppedException)
                    {
                        return;
                    }
                    ex = ex.getCause();
                }
                throw oex;
            }
        }
        
    }
}
