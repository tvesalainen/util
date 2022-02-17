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
import java.time.Duration;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.NANOS;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
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
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import org.vesalainen.util.ArrayIterator;
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
    /**
     * Creates ScheduledThreadPool with 0 corePoolSize, given maximumPoolSize,
     * keepAlive 1 minute and SynchronousQueue
     */
    public CachedScheduledThreadPool(int maximumPoolSize)
    {
        this(0, maximumPoolSize, 1, TimeUnit.MINUTES, new SynchronousQueue<>());
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

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        if (t != null)
        {
            log(WARNING, t, "unhandled %s", t.getMessage());
        }
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
    /**
     * Schedule command to be run not earlier than time.
     * @param command
     * @param time Temporal that supports INSTANT_SECONDS and NANO_OF_SECOND.
     * @return 
     */
    public ScheduledFuture<?> schedule(Runnable command, TemporalAccessor time)
    {
        ensureWaiterRunning();
        log(logLevel, "schedule(%s, %s)", command, time);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, Instant.from(time), null, false);
        delayQueue.add(future);
        return future;
    }
    /**
     * Schedule callable to be run not earlier than time.
     * @param <V>
     * @param callable
     * @param time TemporalAccessor that supports INSTANT_SECONDS and NANO_OF_SECOND.
     * @return 
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, TemporalAccessor time)
    {
        ensureWaiterRunning();
        log(logLevel, "schedule(%s, %s)", callable, time);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(callable, Instant.from(time), null, false);
        delayQueue.add(future);
        return future;
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
     * After initialDelay executes commands with period delay or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable... commands)
    {
        return iterateAtFixedRate(initialDelay, period, unit, new ArrayIterator<>(commands));
    }
    /**
     * After initialDelay executes commands with period delay or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedDelay(long initialDelay, long period, TimeUnit unit, Runnable... commands)
    {
        return iterateAtFixedDelay(initialDelay, period, unit, new ArrayIterator<>(commands));
    }
    /**
     * After initialDelay executes commands using collections iterator until either iterator has no more commands or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedRate(long initialDelay, long period, TimeUnit unit, Collection<Runnable> commands)
    {
        return iterateAtFixedRate(initialDelay, period, unit, commands.iterator());
    }
    /**
     * After initialDelay executes commands using collections iterator until either iterator has no more commands or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedDelay(long initialDelay, long period, TimeUnit unit, Collection<Runnable> commands)
    {
        return iterateAtFixedDelay(initialDelay, period, unit, commands.iterator());
    }
    /**
     * After initialDelay executes commands from iterator until either iterator has no more or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedRate(long initialDelay, long period, TimeUnit unit, Iterator<Runnable> commands)
    {
        ensureWaiterRunning();
        log(logLevel, "iterateAtFixedRate(%d, %d, %s)", initialDelay, period, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(new RunnableIterator(commands), initialDelay, period, unit, false);
        delayQueue.add(future);
        return future;
    }
    /**
     * After initialDelay executes commands from iterator until either iterator has no more or command throws exception.
     * @param initialDelay
     * @param period
     * @param unit
     * @param commands
     * @return 
     * @see java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit) 
     */
    public ScheduledFuture<?> iterateAtFixedDelay(long initialDelay, long period, TimeUnit unit, Iterator<Runnable> commands)
    {
        ensureWaiterRunning();
        log(logLevel, "iterateAtFixedRate(%d, %d, %s)", initialDelay, period, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(new RunnableIterator(commands), initialDelay, period, unit, true);
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
                warning("waiter interrupted");
                return;
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "waiter %s", ex.getMessage());
            }
        }
    }
    /**
     * Concatenates tasks to one task. After first tasks run is completed,
     * next start is submitted and so on, until all tasks are run or exception 
     * is thrown.
     * run is completed.
     * @param runnables
     * @return 
     */
    public Runnable concat(Runnable... runnables)
    {
        if (runnables.length == 0)
        {
            throw new IllegalArgumentException("empty");
        }
        Runnable r = runnables[runnables.length-1];
        for (int ii=runnables.length-2;ii>=0;ii--)
        {
            r = concat(runnables[ii], r);
        }
        return r;
    }
    private Runnable concat(Runnable r1, Runnable r2)
    {
        return ()->{r1.run();submit(r2);};
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
        private Duration period;
        private Instant expires;
        private Throwable throwable;

        public RunnableScheduledFutureImpl(Runnable command, long delay, TimeUnit unit)
        {
            super(command, null);
            this.expires = clock.instant().plus(Duration.ofNanos(unit.toNanos(delay)));
        }

        public RunnableScheduledFutureImpl(Callable<V> callable, long delay, TimeUnit unit)
        {
            super(callable);
            this.expires = clock.instant().plus(Duration.ofNanos(unit.toNanos(delay)));
        }

        public RunnableScheduledFutureImpl(Runnable command, long initialDelay, long period, TimeUnit unit, boolean fixedDelay)
        {
            this(
                    command, 
                    clock.instant().plus(Duration.ofNanos(unit.toNanos(initialDelay))), 
                    Duration.ofNanos(unit.toNanos(period)), fixedDelay
            );
        }

        public RunnableScheduledFutureImpl(Callable<V> callable, Instant expires, Duration period, boolean fixedDelay)
        {
            super(callable);
            this.expires = expires;
            this.period = period;
            this.fixedDelay = fixedDelay;
        }

        public RunnableScheduledFutureImpl(Runnable runnable, Instant expires, Duration period, boolean fixedDelay)
        {
            super(runnable, null);
            this.expires = expires;
            this.period = period;
            this.fixedDelay = fixedDelay;
        }

        @Override
        public boolean isPeriodic()
        {
            return period != null;
        }

        @Override
        public long getDelay(TimeUnit unit)
        {
            return unit.convert(clock.instant().until(expires, NANOS), TimeUnit.NANOSECONDS);
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
                    expires = clock.instant().plus(period);
                }
                else
                {
                    Instant next = clock.instant().plus(period);
                    ok = super.runAndReset();
                    expires = next;
                }
                if (ok)
                {
                    delayQueue.add(this);
                }
                else
                {
                    if (throwable != null && !(throwable instanceof CancelMeException))
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
    /**
     * If CancelMeException is thrown from task it will be silently cancelled.
     */
    public static class CancelMeException extends RuntimeException
    {
        
    }
    private class RunnableIterator implements Runnable
    {
        private Iterator<Runnable> iterator;

        public RunnableIterator(Iterator<Runnable> iterator)
        {
            this.iterator = iterator;
        }

        @Override
        public void run()
        {
            if (iterator.hasNext())
            {
                iterator.next().run();
            }
            else
            {
                throw new CancelMeException();
            }
        }
        
    }
}
