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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * TaggableThread can be tagged or modified while running. It's intended use
 * with TaggableThreadPoolExecutor. Thread get tagged etc while running. Before
 * put back in pool TaggableThreadPoolExecutor afterExecute processes tags etc.
 * @author tkv
 */
public class TaggableThread extends Thread
{
    private Map<Object,Object> tags = new HashMap<>();
    private long start;
    private List<BiConsumer<Map<Object,Object>,Long>> completers;

    public TaggableThread(ThreadGroup group, Runnable target)
    {
        super(group, target);
    }
    /**
     * Add a completer whose parameters are tag map and elapsed time in milliseconds.
     * After thread run completers are called.
     * @param completer 
     */
    public static void addCompleter(BiConsumer<Map<Object,Object>,Long> completer)
    {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof TaggableThread)
        {
            TaggableThread taggableThread = (TaggableThread) currentThread;
            taggableThread.addMe(completer);
        }
    }
    private void addMe(BiConsumer<Map<Object,Object>,Long> completer)
    {
        if (completers == null)
        {
            completers = new ArrayList<>();
        }
        completers.add(completer);
    }

    List<BiConsumer<Map<Object, Object>, Long>> getCompleters()
    {
        return completers;
    }
    
    /**
     * Adds a tag, if current thread is TaggableThread, otherwise does nothing.
     * @param key
     * @param value 
     */
    public static void tag(Object key, Object value)
    {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof TaggableThread)
        {
            TaggableThread taggableThread = (TaggableThread) currentThread;
            taggableThread.tagMe(key, value);
        }
    }

    private void tagMe(Object key, Object value)
    {
        tags.put(key, value);
    }

    Map<Object,Object> getTags()
    {
        return tags;
    }

    long getStart()
    {
        return start;
    }

    void setStart(long start)
    {
        this.start = start;
    }

}
