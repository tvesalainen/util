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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A functional interface for wait
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface Waiter
{
    /**
     * Wait event to happen and returns true. On timeout returns false.
     * If calling thread is interrupted throws InterruptedException.
     * @param time
     * @param unit
     * @return
     * @throws InterruptedException 
     */
    boolean wait(long time, TimeUnit unit) throws InterruptedException;
    /**
     * Wraps Future to waiter. Returned Waiter returns true after success get.
     * It also returns true when CancellationException, InterruptedException, 
     * ExecutionException. However when TimeoutException it returns false.
     * <p>
     * In other words returns true when task is no longer running. Because 
     * task interruption is not reliable this method is also always reliably.
     * @param future
     * @return 
     */
    static Waiter wrap(Future<?> future)
    {
        return (long time, TimeUnit unit)->
        {
            try
            {
                future.get(time, unit);
                return true;
            }
            catch (CancellationException | InterruptedException | ExecutionException ex)
            {
                return true;
            }
            catch (TimeoutException ex)
            {
                return false;
            }
        };
    }
}
