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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.logging.JavaLogging;

/**
 * Name changed so that test is not run automatically
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CachedScheduledThreadPoolT
{
    private Clock clock = Clock.systemUTC();
    private List<Long> times;
    private CachedScheduledThreadPool pool;
    
    public CachedScheduledThreadPoolT()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.ALL);
    }
    @Before
    public void before()
    {
        pool = new CachedScheduledThreadPool();
        pool.setLogLevel(ALL);
        times = new ArrayList<>();
    }
    @After
    public void after() throws InterruptedException
    {
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MILLISECONDS);
    }
    @Test
    public void testSchedule() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.schedule(this::command, 10, TimeUnit.MILLISECONDS);
        assertEquals(10000000, future.getDelay(TimeUnit.NANOSECONDS));
        future.get();
        assertEquals(2, times.size());
        assertTrue(times.get(1)-times.get(0) >= 10);
    }
    @Test
    public void testScheduleAtZonedDataTime() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ZonedDateTime zdt = ZonedDateTime.now(clock).plusSeconds(5);
        ScheduledFuture<?> future = pool.schedule(this::command, zdt);
        future.get();
        assertEquals(2, times.size());
        long gap = times.get(1)-times.get(0);
        assertTrue("gap="+gap,  gap >= 5000);
    }
    @Test
    public void testScheduleAtInstant() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        Instant instant = clock.instant();
        ScheduledFuture<?> future = pool.schedule(this::command, instant.plusMillis(10));
        future.get();
        assertEquals(2, times.size());
        assertTrue("times="+times.size(), times.get(1)-times.get(0) >= 10);
    }
    @Test
    public void testScheduleAtFixedRate() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.scheduleAtFixedRate(this::sleeper, 10, 20, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        future.cancel(false);
        assertTrue("times="+times.size(), times.size()>15);
    }
    @Test
    public void testScheduleWithFixedDelay() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.scheduleWithFixedDelay(this::sleeper, 10, 20, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        future.cancel(false);
        assertTrue("times="+times.size(), times.size()>10 && times.size()<20);
    }
    @Test
    public void testSubmitCascading() throws InterruptedException, ExecutionException
    {
        final List<Integer> l = new ArrayList<>();
        Runnable runnable = pool.concat(
                ()->l.add(1),
                ()->l.add(2),
                ()->l.add(3),
                ()->l.add(4)
        );
        pool.submit(runnable);
        Thread.sleep(500);
        assertEquals(4, l.size());
        assertEquals(1, (int)l.get(0));
        assertEquals(2, (int)l.get(1));
        assertEquals(3, (int)l.get(2));
        assertEquals(4, (int)l.get(3));
    }
    @Test
    public void testSubmitAfter1() throws InterruptedException, ExecutionException
    {
        Future<?> future = pool.submit(this::command);
        future.get();
        final IntReference ref = new IntReference(0);
        Future<?> after = pool.submitAfter(Waiter.wrap(future), ()->ref.setValue(1));
        after.get();
        assertEquals(1, ref.getValue());
    }
    @Test
    public void testSubmitAfter2() throws InterruptedException, ExecutionException
    {
        Future<?> future = pool.submit(this::intrpt);
        final IntReference ref = new IntReference(0);
        Future<?> after = pool.submitAfter(Waiter.wrap(future), ()->ref.setValue(1));
        after.get();
        assertEquals(1, ref.getValue());
    }
    @Test
    public void testSubmitAfter3() throws InterruptedException, ExecutionException
    {
        Future<?> future = pool.submit(this::excp);
        final IntReference ref = new IntReference(0);
        Future<?> after = pool.submitAfter(Waiter.wrap(future), ()->ref.setValue(1));
        after.get();
        assertEquals(1, ref.getValue());
    }
    @Test
    public void testSubmitAfter4() throws InterruptedException, ExecutionException
    {
        long m1 = System.currentTimeMillis();
        Future<?> future = pool.submit(this::sleeper);
        final IntReference ref = new IntReference(0);
        Future<?> after = pool.submitAfter(Waiter.wrap(future), ()->ref.setValue(1));
        after.get();
        long m2 = System.currentTimeMillis();
        long elapsed = m2-m1;
        assertTrue(elapsed+"<10", elapsed >= 10);
        assertEquals(1, ref.getValue());
    }
    @Test
    public void testIterateAtFixedDelay() throws InterruptedException, ExecutionException
    {
        final List<Integer> l = new ArrayList<>();
        pool.iterateAtFixedDelay(1, 100, TimeUnit.NANOSECONDS, ()->l.add(1), ()->l.add(2), ()->l.add(3), ()->l.add(4));
        Thread.sleep(500);
        assertEquals(4, l.size());
        assertEquals(1, (int)l.get(0));
        assertEquals(2, (int)l.get(1));
        assertEquals(3, (int)l.get(2));
        assertEquals(4, (int)l.get(3));
    }
    private void command()
    {
        times.add(clock.millis());
    }
    private void intrpt()
    {
        Thread.currentThread().interrupt();
    }
    private void excp()
    {
        throw new RuntimeException();
    }
    private void sleeper()
    {
        try
        {
            times.add(clock.millis());
            Thread.sleep(10);
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
