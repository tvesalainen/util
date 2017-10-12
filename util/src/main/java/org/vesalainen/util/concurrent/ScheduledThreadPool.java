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
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.vesalainen.util.logging.AttachedLogger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ScheduledThreadPool extends ThreadPoolExecutor implements ScheduledExecutorService, AttachedLogger
{
    private Clock clock;
    private DelayQueue<RunnableScheduledFuture<?>> delayQueue = new DelayQueue<>();

    public ScheduledThreadPool()
    {
        this(Clock.systemUTC());
    }
    
    public ScheduledThreadPool(Clock clock)
    {
        super(0, Integer.MAX_VALUE, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.clock = clock;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void waiter()
    {
        while (true)
        {
            try
            {
                RunnableScheduledFuture<?> runnable = delayQueue.take();
                Runner runner = new Runner(runnable);
                execute(runner);
            }
            catch (InterruptedException ex)
            {
                severe("interrupted");
                return;
            }
        }
    }
    private class Runner implements Runnable
    {
        private RunnableScheduledFuture<?> runnable;

        public Runner(RunnableScheduledFuture<?> runnable)
        {
            this.runnable = runnable;
        }
        
        @Override
        public void run()
        {
            runnable.run();
        }
    }
    private class RunnableScheduledFutureImpl<V> extends FutureTask<V> implements RunnableScheduledFuture<V>
    {
        private boolean fixedDelay;
        private long periodic;
        private long delay;
        private long time;

        public RunnableScheduledFutureImpl(Callable<V> callable, boolean fixedDelay, long periodic, long time)
        {
            super(callable);
            this.fixedDelay = fixedDelay;
            this.periodic = periodic;
            this.time = time;
        }

        public RunnableScheduledFutureImpl(Runnable runnable, V result, boolean fixedDelay, long periodic, long time)
        {
            super(runnable, result);
            this.fixedDelay = fixedDelay;
            this.periodic = periodic;
            this.time = time;
        }

        @Override
        public boolean isPeriodic()
        {
            return periodic > 0;
        }

        @Override
        public long getDelay(TimeUnit unit)
        {
            return time - clock.millis();
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
                super.runAndReset();
            }
            else
            {
                super.run();
            }
        }

    }
}
