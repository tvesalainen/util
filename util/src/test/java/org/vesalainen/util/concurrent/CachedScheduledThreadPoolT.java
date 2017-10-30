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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }
    @Before
    public void before()
    {
        pool = new CachedScheduledThreadPool();
        times = new ArrayList<>();
    }
    @After
    public void after()
    {
        pool.shutdownNow();
    }
    @Test
    public void testSchedule() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.schedule(this::command, 10, TimeUnit.MILLISECONDS);
        future.get();
        assertEquals(2, times.size());
        assertTrue(times.get(1)-times.get(0) > 10);
    }
    @Test
    public void testScheduleAtFixedRate() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.scheduleAtFixedRate(this::sleeper, 10, 20, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        future.cancel(false);
        assertTrue(times.size()>18);
    }
    @Test
    public void testScheduleWithFixedDelay() throws InterruptedException, ExecutionException
    {
        times.add(clock.millis());
        ScheduledFuture<?> future = pool.scheduleWithFixedDelay(this::sleeper, 10, 20, TimeUnit.MILLISECONDS);
        Thread.sleep(500);
        future.cancel(false);
        assertTrue(times.size()>15 && times.size()<20);
    }
    @Test
    public void testSubmitAfter() throws InterruptedException, ExecutionException
    {
        ScheduledFuture<?> future = pool.schedule(this::sleeper, 10, TimeUnit.MILLISECONDS);
        final IntReference ref = new IntReference(0);
        long m1 = clock.millis();
        Future<?> after = pool.submitAfter(future, ()->ref.setValue(1));
        after.get();
        long m2 = clock.millis();
        assertEquals(1, ref.getValue());
        assertTrue(m2-m1 >= 20);
    }
    private void command()
    {
        times.add(clock.millis());
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
