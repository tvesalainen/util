/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.vesalainen.util.logging.JavaLogging;

/**
 * RefreshableTimer is a timer which can be refreshed.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RefreshableTimer extends JavaLogging
{
    private long nanos;
    private Thread thread;
    private long start;

    public RefreshableTimer()
    {
        super(RefreshableTimer.class);
    }
    /**
     * Wait until timeout or interrupted. Waiting time can be longer than timeout
     * if refresh method has been called.
     * @param timeout
     * @param unit
     * @return Returns true if timeout. False if interrupted.
     */
    public boolean wait(long timeout, TimeUnit unit)
    {
        if (thread != null)
        {
            throw new IllegalStateException("already waiting");
        }
        this.nanos = unit.NANOSECONDS.convert(timeout, unit);
        this.start = System.nanoTime();
        this.thread = Thread.currentThread();
        long sleep = nanos-(System.nanoTime()-start);
        while (sleep > 0)
        {
            finest("waiting for %d nanos", sleep);
            LockSupport.parkNanos(this, sleep);
            if (thread.isInterrupted())
            {
                info("interrupted");
                return false;
            }
            sleep = nanos-(System.nanoTime()-start);
        }
        return true;
    }
    /**
     * Starts waiting from beginning.
     */
    public void refresh()
    {
        if (thread == null)
        {
            throw new IllegalStateException("not waiting");
        }
        this.start = System.nanoTime();
        LockSupport.unpark(thread);
        finest("refreshed timer");
    }
}
