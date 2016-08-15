/*
 * Copyright (C) 2016 tkv
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * TaggableThreadPoolExecutor is a ThreadPoolExecutor that uses TaggableThreadFactory.
 * @author tkv
 */
public abstract class TaggableThreadPoolExecutor extends ThreadPoolExecutor
{
    public TaggableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory());
    }

    public TaggableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory(), handler);
    }
    
    private static TaggableThreadFactory threadFactory()
    {
        TaggableThreadFactory threadFactory = new TaggableThreadFactory();
        return threadFactory;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof TaggableThread)
        {
            TaggableThread tt = (TaggableThread) currentThread;
            Map<Object,Object> tags = tt.getTags();
            long elapsed = System.currentTimeMillis() - tt.getStart();
            afterExecute(tags, elapsed, t);
            List<BiConsumer<Map<Object, Object>, Long>> completers = tt.getCompleters();
            if (completers != null)
            {
                completers.stream().forEach((completer) ->
                {
                    completer.accept(tags, elapsed);
                });
            }
            if (completers != null)
            {
                completers.clear();
            }
            tags.clear();
        }
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);
        if (t instanceof TaggableThread)
        {
            TaggableThread taggableThread = (TaggableThread) t;
            taggableThread.setStart(System.currentTimeMillis());
        }
    }
    
    protected abstract void afterExecute(Map<Object,Object> tags, long elapsed, Throwable t);
}
