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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class TaggableThread extends Thread
{
    private Map tags = new HashMap<>();
    private long start;

    public TaggableThread(ThreadGroup group, Runnable target)
    {
        super(group, target);
    }
    
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

    Map getTags()
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
