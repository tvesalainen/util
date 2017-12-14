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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PredicateSynchronizerTest
{
    
    public PredicateSynchronizerTest()
    {
    }

    @Test
    public void testReturnImmediately() throws InterruptedException
    {
        PredicateSynchronizer s = new PredicateSynchronizer("test");
        long now = System.currentTimeMillis();
        boolean ok = s.waitUntil(()->true);
        assertTrue(System.currentTimeMillis()-now < 100);
        assertTrue(ok);
    }
    @Test
    public void testTimeout() throws InterruptedException
    {
        PredicateSynchronizer s = new PredicateSynchronizer("test");
        long now = System.currentTimeMillis();
        boolean ok = s.waitUntil(()->false, 100, TimeUnit.MILLISECONDS);
        assertTrue(System.currentTimeMillis()-now >= 100);
        assertFalse(ok);
    }
    @Test
    public void testUpdate() throws InterruptedException
    {
        PredicateSynchronizer s = new PredicateSynchronizer("test");
        long now = System.currentTimeMillis();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(()->s.update(), 150, TimeUnit.MILLISECONDS);
        boolean ok = s.waitUntil(()->now+100 <= System.currentTimeMillis());
        assertTrue(System.currentTimeMillis()-now >= 100);
        assertTrue(ok);
    }
    @Test
    public void testInterrupted()
    {
        PredicateSynchronizer s = new PredicateSynchronizer("test");
        long now = System.currentTimeMillis();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Thread current = Thread.currentThread();
        scheduler.schedule(()->current.interrupt(), 150, TimeUnit.MILLISECONDS);
        try
        {
            s.waitUntil(()->now+100 <= System.currentTimeMillis());
            fail("should have thrown InterruptedException");
        }
        catch (InterruptedException ex)
        {
            // ok
        }
        assertTrue(System.currentTimeMillis()-now >= 100);
    }
    
}
