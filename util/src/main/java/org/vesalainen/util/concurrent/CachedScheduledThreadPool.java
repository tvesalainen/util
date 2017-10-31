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
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
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
    private static Level LEVEL = DEBUG;
    private Clock clock = Clock.systemUTC();
    private DelayQueue<RunnableScheduledFuture<?>> delayQueue = new DelayQueue<>();
    private Future<?> waiterFuture;
    private Map<Future<?>,FutureTask<?>> afterMap = new WeakHashMap<>();
    private Map<Collection<Future<?>>,CountDownLatch> waitMap = new IdentityHashMap<>();
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
    
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(LEVEL, "schedule(%s, %d, %s)", command, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, delay, unit);
        delayQueue.add(future);
        return future;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(LEVEL, "schedule(%s, %d, %s)", callable, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(callable, delay, unit);
        delayQueue.add(future);
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(LEVEL, "scheduleAtFixedRate(%s, %d, %d, %s)", command, initialDelay, period, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, initialDelay, period, unit, false);
        delayQueue.add(future);
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        ensureWaiterRunning();
        log(LEVEL, "scheduleWithFixedDelay(%s, %d, %d, %s)", command, initialDelay, delay, unit);
        RunnableScheduledFutureImpl future = new RunnableScheduledFutureImpl(command, initialDelay, delay, unit, true);
        delayQueue.add(future);
        return future;
    }
    /**
     * submits callable after waiting future to complete
     * @param <V>
     * @param future
     * @param callable
     * @return 
     */
    public <V> Future<V> submitAfter(Future<V> future, Callable<V> callable)
    {
        return submitAfter(future, callable, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * submits callable after waiting future to complete
     * @param future
     * @param runnable
     * @return 
     */
    public Future<?> submitAfter(Future<?> future, Runnable runnable)
    {
        return submitAfter(future, runnable, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    private void ensureWaiterRunning()
    {
        if (waiterFuture == null || waiterFuture.isDone())
        {
            waiterFuture = submit(this::waiter);
            log(LEVEL, "waiter started");
        }
    }
    private void waiter()
    {
        while (true)
        {
            try
            {
                RunnableScheduledFuture<?> runnable = delayQueue.take();
                log(LEVEL, "activated %s", runnable);
                execute(runnable);
            }
            catch (InterruptedException ex)
            {
                severe("waiter interrupted");
                return;
            }
        }
    }
    /**
     * submits callable after waiting future to complete or timeout to exceed.
     * If timeout exceeds task is cancelled.
     * @param <V>
     * @param future
     * @param callable
     * @param timeout
     * @param unit
     * @return 
     */
    public <V> Future<V> submitAfter(Future<V> future, Callable<V> callable, long timeout, TimeUnit unit)
    {
        AfterTask<V> task = new AfterTask<>(future, callable, timeout, unit);
        return (Future<V>) addAfterTask(task);
    }
    /**
     * submits runnable after waiting future to complete or timeout to exceed. 
     * If timeout exceeds task is cancelled.
     * @param future
     * @param runnable
     * @param timeout
     * @param unit
     * @return 
     */
    public Future<?> submitAfter(Future<?> future, Runnable runnable, long timeout, TimeUnit unit)
    {
        AfterTask<?> task = new AfterTask<>(future, runnable, timeout, unit);
        return addAfterTask(task);
    }
    /**
     * Given collection contains future instances. Those instances will be 
     * removed from when they are completed.
     * @param collection 
     */
    public synchronized void setRemoveCompleted(Collection<Future<?>> collection)
    {
        collection.removeIf((f)->f.isDone());
        waitMap.put(collection, null);
    }
    /**
     * Remove the collections.
     * @param collection 
     */
    public synchronized void removeRemoveCompleted(Collection<Future<?>> collection)
    {
        collection.removeIf((f)->f.isDone());
        waitMap.remove(collection);
    }
    /**
     * Waits until all given futures are completed or thread is interrupted.
     * Note that completed futures are removed from list!
     * @param collection
     * @throws InterruptedException 
     */
    public void waitforAllCompleted(Collection<Future<?>> collection) throws InterruptedException
    {
        waitforAllCompleted(collection, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    /**
     * Waits until all given futures are completed, thread is interrupted or
     * timeout exceeds.
     * Note that completed futures are removed from list!
     * @param collection
     * @param timeout
     * @param unit
     * @throws InterruptedException 
     */
    public void waitforAllCompleted(Collection<Future<?>> collection, long timeout, TimeUnit unit) throws InterruptedException
    {
        CountDownLatch latch;
        synchronized(this)
        {
            collection.removeIf((f)->f.isDone());
            if (collection.isEmpty())
            {
                return;
            }
            latch = waitMap.get(collection);
            if (latch == null)
            {
                latch = new CountDownLatch(1);
                waitMap.put(collection, latch);
            }
        }
        latch.await(timeout, unit);
    }
    private synchronized Future<?> addAfterTask(AfterTask<?> afterTask)
    {
        Future<?> future = afterTask.getFuture();
        if (afterMap.containsKey(future))
        {
            submit(afterTask);
        }
        else
        {
            afterMap.put(future, afterTask);
        }
        return afterTask;
    }
    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        if (r instanceof Future)
        {
            Future<?> future = (Future<?>) r;
            FutureTask<?> task = afterMap.get(future);
            if (task != null)
            {
                submit(task);
            }
            else
            {
                afterMap.put(future, null);
            }
            Iterator<Entry<Collection<Future<?>>, CountDownLatch>> iterator = waitMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<Collection<Future<?>>, CountDownLatch> entry = iterator.next();
                Collection<Future<?>> key = entry.getKey();
                key.remove(future);
                if (key.isEmpty())
                {
                    CountDownLatch latch = entry.getValue();
                    if (latch != null)
                    {
                        latch.countDown();
                        iterator.remove();
                    }
                }
            }
        }
    }
    
    private class AfterTask<V> extends FutureTask<V>
    {
        private Future<V> future;
        private Future<?> timeoutFuture;
        
        public AfterTask(Future<V> future, Callable<V> callable, long timeout, TimeUnit unit)
        {
            super(callable);
            this.future = future;
            triggerTimer(timeout, unit);
        }

        public AfterTask(Future<?> future, Runnable runnable, long timeout, TimeUnit unit)
        {
            super(runnable, null);
            this.future = (Future<V>) future;
            triggerTimer(timeout, unit);
        }

        private void triggerTimer(long timeout, TimeUnit unit)
        {
            if (timeout < Long.MAX_VALUE)
            {
                this.timeoutFuture = schedule(()->setException(new TimeoutException()), timeout, unit);
            }
        }
        private Future<V> getFuture()
        {
            return future;
        }

        @Override
        public void run()
        {
            if (timeoutFuture != null)
            {
                timeoutFuture.cancel(false);
            }
            super.run();
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
                        log(LEVEL,"runAndReset failed (cancelled)");
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
