/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractServer extends JavaLogging implements Runnable
{
    protected final ExecutorService executor;
    private CountDownLatch latch = new CountDownLatch(1);
    private Future<?> future;
    protected String name;
    protected Level level = CONFIG;

    protected AbstractServer(Class<? extends AbstractServer> me, ExecutorService executor)
    {
        super(me);
        this.executor = executor;
        this.name = me.getSimpleName();
    }

    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException(name+" already running");
        }
        future = executor.submit(this);
        log(level, "%s started", name);
    }
    public void waitUntilRunning() throws InterruptedException
    {
        latch.await();
    }
    public void waitUntilRunning(long timeout, TimeUnit unit) throws InterruptedException
    {
        latch.await(timeout, unit);
    }
    protected void running()
    {
        latch.countDown();
    }
    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException(name+" not running");
        }
        Future<?> f = future;
        future = null;
        f.cancel(true);
        log(level, "%s stopped", name);
    }

    @Override
    public void run()
    {
        try
        {
            doRun();
        }
        catch (ClosedByInterruptException ex)
        {
            if (future != null)
            {
                log(SEVERE, ex, "interrupted %s %s", name, ex.getMessage());
            }
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s %s", name, ex.getMessage());
        }
        finally
        {
            log(level, "%s stopped running", name);
        }
    }

    public void setLogLevel(Level level)
    {
        this.level = level;
    }
    
    protected abstract void doRun() throws IOException;
}
