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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/**
 * PredicateSynchronizer synchronizes threads upon a predicate. Thread waits
 * until predicate is true. Use update method when predicate conditions change.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PredicateSynchronizer
{
    private Object blocker = this;
    private final Queue<Thread> waiters = new ConcurrentLinkedQueue<>();
    /**
     * Creates PredicateSynchronizer with blocker as self.
     */
    public PredicateSynchronizer()
    {
    }
    /**
     * Creates PredicateSynchronizer with given blocker.
     * @param blocker 
     */
    public PredicateSynchronizer(Object blocker)
    {
        this.blocker = blocker;
    }
    
    public boolean waitUntil(BooleanSupplier predicate) throws InterruptedException
    {
        return waitUntil(predicate, -1, TimeUnit.MILLISECONDS);
    }
    /**
     * Waits until predicate is true or after timeout.
     * <p>
     * If predicate is true returns immediately true.
     * <p>
     * If timeout passed returns false
     * <p>
     * If thread is interrupted throws InterruptedException
     * @param predicate
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException 
     */
    public boolean waitUntil(BooleanSupplier predicate, long timeout, TimeUnit unit) throws InterruptedException
    {
        if (predicate.getAsBoolean())
        {
            return true;
        }
        long deadline = timeout > 0 ? System.currentTimeMillis() + unit.toMillis(timeout) : Long.MAX_VALUE;
        waiters.add(Thread.currentThread());
        try
        {
            while (true)
            {
                LockSupport.parkUntil(blocker, deadline);
                if (Thread.interrupted())
                {
                    throw new InterruptedException();
                }
                if (predicate.getAsBoolean())
                {
                    return true;
                }
                if (deadline <= System.currentTimeMillis())
                {
                    return false;
                }
            }
        }
        finally
        {
            boolean removed = waiters.remove(Thread.currentThread());
            assert removed;
        }
    }
    /**
     * Causes waiting threads predicates to be evaluated and possibly causing
     * threads to continue there operating if predicates return true.
     */
    public void update()
    {
        for (Thread thread : waiters)
        {
            LockSupport.unpark(thread);
        }
    }
}
