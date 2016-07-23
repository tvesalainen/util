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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.vesalainen.lang.Primitives;

/**
 * TimerThreadPoolExecutor differs from ScheduledThreadPoolExecutor. main 
 * difference is that work queue can be assigned in constructor. If work queue
 * characteristics is not important, use ScheduledThreadPoolExecutor.
 * 
 * <p>Implementation uses Timer!
 * @author tkv
 * @see java.util.Timer
 */
public class TimerThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService
{
    private final Timer timer = new Timer();
    
    public TimerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TimerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TimerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TimerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        delay = unit.convert(delay, TimeUnit.MILLISECONDS);
        Task task = new Task(Executors.callable(command), delay);
        timer.schedule(task, delay);
        return task;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        delay = unit.convert(delay, TimeUnit.MILLISECONDS);
        Task<V> task = new Task<>(callable, delay);
        timer.schedule(task, delay);
        return task;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        initialDelay = unit.convert(initialDelay, TimeUnit.MILLISECONDS);
        period = unit.convert(period, TimeUnit.MILLISECONDS);
        Task task = new Task(Executors.callable(command), initialDelay);
        timer.scheduleAtFixedRate(task, initialDelay, period);
        return task;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        initialDelay = unit.convert(initialDelay, TimeUnit.MILLISECONDS);
        delay = unit.convert(delay, TimeUnit.MILLISECONDS);
        Task task = new Task(Executors.callable(command), initialDelay);
        timer.schedule(task, initialDelay, delay);
        return task;
    }
    
    private class Task<V> extends TimerTask implements ScheduledFuture<V>
    {
        private Callable<V> callable;
        private long scheduled;
        private Future<V> future;
        private Semaphore semaphore = new Semaphore(0);

        public Task(Callable<V> callable, long delay)
        {
            this.callable = Objects.requireNonNull(callable);
            this.scheduled = System.currentTimeMillis() + delay;
        }
        
        @Override
        public void run()
        {
            future = submit(callable);
            if (semaphore != null)
            {
                semaphore.release(10000);
                semaphore = null;
            }
        }

        @Override
        public long getDelay(TimeUnit unit)
        {
            return unit.convert(scheduled - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o)
        {
            return Primitives.signum(getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            if (future != null)
            {
                return future.cancel(mayInterruptIfRunning);
            }
            else
            {
                return super.cancel();
            }
        }

        @Override
        public boolean isCancelled()
        {
            if (future != null)
            {
                return future.isCancelled();
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean isDone()
        {
            if (future != null)
            {
                return future.isDone();
            }
            else
            {
                return false;
            }
        }

        @Override
        public V get() throws InterruptedException, ExecutionException
        {
            if (future != null)
            {
                return future.get();
            }
            else
            {
                semaphore.acquire();
                return future.get();
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            if (future != null)
            {
                return future.get();
            }
            else
            {
                if (semaphore.tryAcquire(timeout, unit))
                {
                    return future.get();
                }
                else
                {
                    throw new TimeoutException();
                }
            }
        }
        
    }
}
