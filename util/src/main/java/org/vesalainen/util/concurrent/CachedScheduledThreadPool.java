/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.Clock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.util.logging.AttachedLogger;
import static org.vesalainen.util.logging.BaseLogging.DEBUG;

/**
 * A ScheduledExecutorService which unlike 
 * ScheduledThreadPoolExecutor is not fixed-sized.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CachedScheduledThreadPool extends ThreadPoolExecutor implements ScheduledExecutorService, AttachedLogger
{
    private Level logLevel = DEBUG;
    private Clock clock = Clock.systemUTC();
    private DelayQueue<RunnableScheduledFuture<?>> delayQueue = new DelayQueue<>();
    private Future<?> waiterFuture;
    /**
     * Creates ScheduledThreadPool with 0 corePoolSize, unlimited maximumPoolSize,
     * keepAlive 1 minute and SynchronousQueue
     */
    public CachedScheduledThreadPool()
    {
        this(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new SynchronousQueue<>());
    }

    public CachedScheduledThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public CachedScheduledThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public CachedScheduledThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public CachedScheduledThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public Clock getClock()
    {
        return clock;
    }

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    public void setLogLevel(Level level)
    {
        this.logLevel = level;
    }
    
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(logLevel, "schedule(%s, %d, %s)", command, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, delay, unit);
        delayQueue.add(future);
        return future;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(logLevel, "schedule(%s, %d, %s)", callable, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(callable, delay, unit);
        delayQueue.add(future);
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(logLevel, "scheduleAtFixedRate(%s, %d, %d, %s)", command, initialDelay, period, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, initialDelay, period, unit, false);
        delayQueue.add(future);
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(logLevel, "scheduleWithFixedDelay(%s, %d, %d, %s)", command, initialDelay, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, initialDelay, delay, unit, true);
        delayQueue.add(future);
        return future;
    }
    /**
     * submits callable after waiting future to complete
     * @param <V>
     * @param waiter
     * @param callable
     * @return 
     */
    public <V> Future<V> submitAfter(Waiter waiter, Callable<V> callable)
    {
        return submitAfter(waiter, callable, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * submits callable after waiting future to complete
     * @param waiter
     * @param runnable
     * @return 
     */
    public Future<?> submitAfter(Waiter waiter, Runnable runnable)
    {
        return submitAfter(waiter, runnable, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    private void ensureWaiterRunning()
    {
        if (waiterFuture == null || waiterFuture.isDone())
        {
            waiterFuture = submit(this::waiter);
            log(logLevel, "waiter started");
        }
    }
    private void waiter()
    {
        while (true)
        {
            try
            {
                RunnableScheduledFuture<?> runnable = delayQueue.take();
                log(logLevel, "activated %s", runnable);
                execute(runnable);
            }
            catch (InterruptedException ex)
            {
                log(logLevel, "waiter interrupted");
                return;
            }
        }
    }
    /**
     * submits callable after waiting future to complete or timeout to exceed.
     * If timeout exceeds task is cancelled.
     * @param <V>
     * @param waiter
     * @param callable
     * @param timeout
     * @param unit
     * @return 
     */
    public <V> Future<V> submitAfter(Waiter waiter, Callable<V> callable, long timeout, TimeUnit unit)
    {
        AfterTask<V> task = new AfterTask<>(waiter, callable, timeout, unit);
        log(logLevel, "submit after task %s", task);
        return (Future<V>) submit(task);
    }
    /**
     * submits runnable after waiting future to complete or timeout to exceed. 
     * If timeout exceeds task is cancelled.
     * @param waiter
     * @param runnable
     * @param timeout
     * @param unit
     * @return 
     */
    public Future<?> submitAfter(Waiter waiter, Runnable runnable, long timeout, TimeUnit unit)
    {
        AfterTask<?> task = new AfterTask<>(waiter, runnable, timeout, unit);
        log(logLevel, "submit after task %s", task);
        return submit(task);
    }
    private class AfterTask<V> implements Callable<V>
    {
        private Waiter waiter;
        private Callable<V> task;
        private long timeout;
        private TimeUnit unit;

        public AfterTask(Waiter waiter, Callable<V> task, long timeout, TimeUnit unit)
        {
            this.waiter = waiter;
            this.task = task;
            this.timeout = timeout;
            this.unit = unit;
        }
        
        public AfterTask(Waiter waiter, Runnable runnable, long timeout, TimeUnit unit)
        {
            this.waiter = waiter;
            this.task = (Callable<V>) Executors.callable(runnable);
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public V call() throws Exception
        {
            try
            {
                log(logLevel, "wait future %s", waiter);
                if (waiter.wait(timeout, unit))
                {
                    log(logLevel, "enter after task %s", task);
                    return task.call();
                }
                else
                {
                    warning("waited task %s timeout -> task %s rejected", task, waiter);
                    return null;
                }
            }
            catch (Exception ex)
            {
                log(SEVERE, ex, "after task got %s", ex.getMessage());
                throw ex;
            }
        }

        @Override
        public String toString()
        {
            return "AfterTask{" + "future=" + waiter + ", task=" + task + ", timeout=" + timeout + ", unit=" + unit + '}';
        }

    }
    private class RunnableScheduledFutureImpl<V> extends FutureTask<V> implements RunnableScheduledFuture<V>
    {
        private boolean fixedDelay;
        private long period;
        private long expires;
        private Throwable throwable;

        public RunnableScheduledFutureImpl(Runnable command, long delay, TimeUnit unit)
        {
            super(command, null);
            this.expires = clock.millis()+unit.toMillis(delay);
        }

        public RunnableScheduledFutureImpl(Callable<V> callable, long delay, TimeUnit unit)
        {
            super(callable);
            this.expires = clock.millis()+unit.toMillis(delay);
        }

        public RunnableScheduledFutureImpl(Runnable command, long initialDelay, long period, TimeUnit unit, boolean fixedDelay)
        {
            super(command, null);
            this.expires = clock.millis()+unit.toMillis(initialDelay);
            this.period = unit.toMillis(period);
            this.fixedDelay = fixedDelay;
        }

        @Override
        public boolean isPeriodic()
        {
            return period > 0;
        }

        @Override
        public long getDelay(TimeUnit unit)
        {
            return expires - clock.millis();
        }

        @Override
        public int compareTo(Delayed o)
        {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public void run()
        {
            if (isPeriodic())
            {
                boolean ok = true;
                if (fixedDelay)
                {
                    ok = super.runAndReset();
                    expires = clock.millis()+period;
                }
                else
                {
                    long next = clock.millis()+period;
                    ok = super.runAndReset();
                    expires = next;
                }
                if (ok)
                {
                    delayQueue.add(this);
                }
                else
                {
                    if (throwable != null)
                    {
                        log(SEVERE, throwable, "runAndReset failed %s", throwable.getMessage());
                    }
                    else
                    {
                        log(logLevel,"runAndReset failed (cancelled)");
                    }
                }
            }
            else
            {
                super.run();
            }
        }

        @Override
        protected void setException(Throwable t)
        {
            super.setException(t);
            throwable = t;
        }

        @Override
        public String toString()
        {
            return "RunnableScheduledFutureImpl{" + "task=" + super.toString() + "fixedDelay=" + fixedDelay + ", period=" + period + ", expires=" + expires + '}';
        }

    }
}
