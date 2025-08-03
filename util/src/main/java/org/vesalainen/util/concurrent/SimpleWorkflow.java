/*
 * Copyright (C) 2014 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SimpleWorkflow creates a workflow using several threads. 
 * 
 * <p>Running thread activates another by using switchTo or fork method. 
 * Parallel excecution is possible between fork and join methods. Using switchTo
 * is same as calling fork and join.
 * <p>
 * Thread can pass messages.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <K> Thread key
 * @param <M> Message type
 * @param <C> Context type
 */
public abstract class SimpleWorkflow<K,M,C>
{
    private final Map<K,Thread> threadMap;
    private final Map<Thread,Semaphore> semaphoreMap;
    private final Semaphore stopSemaphore;
    private final int maxParallelism;
    private final long timeout;
    private final TimeUnit timeUnit;
    private final ReentrantLock lock = new ReentrantLock();
    private ReentrantLock contextLock;
    private C context;
    private final Map<K,M> messageMap = (Map<K,M>) Collections.synchronizedMap(new HashMap<>());
    private final Set<K> parallelSet = (Set<K>) Collections.synchronizedSet(new HashSet<>());
    
    /**
     * Creates a new workflow. Current thread is named to start.
     * Maximum number of parallel running threads = 1. No timeout for idle threads.
     * @param start Name of current thread.
     */
    public SimpleWorkflow(K start)
    {
        this(start, null, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * Creates a new workflow. Current thread is named to start.
     * Maximum number of parallel running threads = 1. No timeout for idle threads.
     * @param start Name of current thread.
     * @param context a context object for all threads to share
     */
    public SimpleWorkflow(K start, C context)
    {
        this(start, context, 1, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * Creates a new workflow. Current thread is named to start.
     * @param start Name of starter thread.
     * @param context a context object for all threads to share
     * @param maxParallelism Maximum number of parallel running threads.
     * @param timeout Timeout for idle thread to be stopped.
     * @param timeUnit Time unit for idle thread to be stopped.
     */
    public SimpleWorkflow(K start, C context, int maxParallelism, long timeout, TimeUnit timeUnit)
    {
        if (maxParallelism < 0)
        {
            throw new IllegalArgumentException("maxParallelism < 0");
        }
        this.context = context;
        this.maxParallelism = maxParallelism;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.stopSemaphore = new Semaphore(maxParallelism);
        this.semaphoreMap = new HashMap<>();
        this.threadMap = new HashMap<>();
        this.contextLock = new ReentrantLock();
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
        fork(to, null);
    }
    /**
     * Fork executing thread. 
     * @param to Next executing
     * @param msg Message to
     */
    public void fork(K to, M msg)
    {
        if (maxParallelism == 0)
        {
            throw new IllegalArgumentException("maxParallelism == 0, fork() not allowed! Use switchTo.");
        }
        try
        {
            stopSemaphore.acquire();
            parallelSet.add(getCurrentKey());
            doFork(to, msg);
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private void doFork(K to, M msg)
    {
        lock.lock();
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            messageMap.put(to, msg);
            Thread nextThread = threadMap.get(to);
            if (nextThread == null)
            {
                Runnable runnable = create(to);
                runnable = new Wrapper(runnable);
                nextThread = new Thread(runnable, to.toString());
                nextThread.setDaemon(true);
                Semaphore semaphore = new Semaphore(0);
                threadMap.put(to, nextThread);
                semaphoreMap.put(nextThread, semaphore);
                nextThread.start();
            }
            else
            {
                Semaphore semaphore = semaphoreMap.get(nextThread);
                semaphore.release();
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    /**
     * Thread waits until another thread calls fork, switchTo or endTo method
     * using threads key. Returns message from another thread.
     * @return 
     */
    public M join()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        Thread currentThread = Thread.currentThread();
        Semaphore currentSemaphore = semaphoreMap.get(currentThread);
        if (currentSemaphore == null)
        {
            throw new IllegalStateException("Current thread is not workflow thread");
        }
        stopSemaphore.release();
        parallelSet.remove(getCurrentKey());
        return doJoin();
    }
    private M doJoin()
    {
        try
        {
            Thread currentThread = Thread.currentThread();
            Semaphore currentSemaphore = semaphoreMap.get(currentThread);
            if (currentSemaphore == null)
            {
                throw new ThreadStoppedException("stopped");
            }
            boolean success = currentSemaphore.tryAcquire(timeout, timeUnit);
            if (!success)
            {
                lock.lock();
                try
                {
                    semaphoreMap.remove(currentThread);
                    Iterator<Map.Entry<K, Thread>> it = threadMap.entrySet().iterator();
                    while (it.hasNext())
                    {
                        Map.Entry<K, Thread> entry = it.next();
                        if (currentThread.equals(entry.getValue()))
                        {
                            it.remove();
                            break;
                        }
                    }
                    throw new ThreadStoppedException("timeout");
                }
                finally
                {
                    lock.unlock();
                }
            }
            return getMessage();
        }
        catch (InterruptedException ex)
        {
            throw new ThreadStoppedException(ex);
        }
    }
    /**
     * Returns current message or null if there's no message. This is the same
     * message that is returned from join or switchTo method. For starting thread
     * this is the only way to get the message.
     * @return 
     */
    public M getMessage()
    {
        return messageMap.get(getCurrentKey());
    }
    /**
     * Switch executing thread. 
     * @param to Next executing
     * @return 
     */
    public M switchTo(K to)
    {
        return switchTo(to, null);
    }
    /**
     * Switch executing thread. 
     * @param to Next executing
     * @param msg
     * @return 
     */
    public M switchTo(K to, M msg)
    {
        doFork(to, msg);
        return doJoin();
    }
    
    public int getThreadCount()
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        return threadMap.size();
    }
    /**
     * Ends the current thread and switches to
     * @param to 
     */
    public void endTo(K to)
    {
        endTo(to, null);
    }
    /**
     * Ends the current thread and switches to
     * @param to 
     * @param msg 
     */
    public void endTo(K to, M msg)
    {
        K key = getCurrentKey();
        if (key.equals(to))
        {
            throw new IllegalArgumentException("current and to are equals");
        }
        if (key != null)
        {
            kill(key);
            doFork(to, msg);
            throw new ThreadStoppedException("suicide");
        }
        else
        {
            throw new IllegalArgumentException("called from wrong thread");
        }
    }
    /**
     * Returns key for current thread. Returns null if current thread is not
     * part of the workflow.
     * @return 
     */
    public K getCurrentKey()
    {
        lock.lock();
        try
        {
            Thread currentThread = Thread.currentThread();
            for (Entry<K,Thread> entry : threadMap.entrySet())
            {
                if (currentThread.equals(entry.getValue()))
                {
                    return entry.getKey();
                }
            }
            return null;
        }
        finally
        {
            lock.unlock();
        }
    }
    public void kill(K key)
    {
        if (threadMap.isEmpty())
        {
            throw new IllegalStateException("threads are already interrupted");
        }
        lock.lock();
        try
        {
            Thread thread = threadMap.get(key);
            threadMap.remove(key);
            semaphoreMap.remove(thread);
            if (parallelSet.contains(key))
            {
                parallelSet.remove(key);
                stopSemaphore.release();
            }
            thread.interrupt();
        }
        finally
        {
            lock.unlock();
        }
    }
    /**
     * Wait until parallel excecuting threads have joined. After that interrupt
     * all other than the calling thread.
     */
    public void waitAndStopThreads()
    {
        try
        {
            if (threadMap.isEmpty())
            {
                throw new IllegalStateException("threads are already interrupted");
            }
            stopSemaphore.acquire(maxParallelism);
            stopThreads();
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
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
        lock.lock();
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
            lock.unlock();
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
    /**
     * Provides thread save access to the context object. ContextAccess.access 
     * method is called inside a lock to prevent concurrent modification to
     * the object.
     * @param <R> Return type
     * @param access 
     * @return  
     */
    public <R> R accessContext(ContextAccess<C,R> access)
    {
        contextLock.lock();
        try
        {
            return access.access(context);
        }
        finally
        {
            contextLock.unlock();
        }
    }
    public interface ContextAccess<C,R>
    {
        R access(C context);
    }
}
