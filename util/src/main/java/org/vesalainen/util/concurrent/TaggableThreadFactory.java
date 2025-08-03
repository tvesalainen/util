/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.ThreadFactory;

/**
 * TaggableThreadFactory creates TaggableThreads. Threads properties can be
 * changed by using methods in this class.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TaggableThreadFactory implements ThreadFactory
{
    private ThreadGroup threadGroup;
    private String name;
    private boolean daemon;
    private int priority;
    
    @Override
    public Thread newThread(Runnable runnable)
    {
        TaggableThread taggableThread = new TaggableThread(threadGroup, runnable);
        if (name != null)
        {
            taggableThread.setName(name+" ["+taggableThread.getId()+"]");
        }
        taggableThread.setDaemon(daemon);
        if (priority > 0)
        {
            taggableThread.setPriority(priority);
        }
        return taggableThread;
    }

    public ThreadGroup getThreadGroup()
    {
        return threadGroup;
    }

    public void setThreadGroup(ThreadGroup threadGroup)
    {
        this.threadGroup = threadGroup;
    }

    public String getName()
    {
        return name;
    }
    /**
     * Sets created thread's name as 'name [id]'
     * @param name 
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isDaemon()
    {
        return daemon;
    }

    public void setDaemon(boolean daemon)
    {
        this.daemon = daemon;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    
    
}
