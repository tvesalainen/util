/*
 * Copyright (C) 2025 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.EnumSet;
import org.vesalainen.util.logging.JavaLogging;

/**
 * CheckList is a EnumSet based utility which allows controlling that each individual
 * task is done. Enum acts as a list of tasks.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CheckList<E extends Enum<E>>
{
    private final EnumSet<E> all;
    private EnumSet<E> work;
    private final Runnable action;
    /**
     * 
     * @param cls Enum class
     */
    public CheckList(Class<E> cls)
    {
        this(cls, ()->{});
    }
    
    /**
     * 
     * @param cls Enum class
     * @param name Used only for logging.
     */
    public CheckList(Class<E> cls, Runnable action)
    {
        this.all = EnumSet.allOf(cls);
        this.work = EnumSet.noneOf(cls);
        this.action = action;
    }
    /**
     * Called to register that given task is done.
     * @param deed
     * @return 
     */
    public boolean done(E deed)
    {
        boolean done = isDone(deed);
        boolean ready = ready();
        work.add(deed);
        if (ready() && !ready)
        {
            action.run();
        }
        return !done;
    }
    /**
     * Returns true if all given tasks has been done.
     * @param deeds
     * @return 
     */
    public boolean isDone(E... deeds)
    {
        for (E e : deeds)
        {
            if (!work.contains(e))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Returns true if all tasks have been done.
     * @return 
     */
    public boolean ready()
    {
        return all.equals(work);
    }
    /**
     * Marks given task as undone.
     * @param deed
     * @return 
     */
    public boolean clear(E deed)
    {
        return work.remove(deed);
    }
    /**
     * Clear all tasks that have registered as done.
     */
    public void clear()
    {
        work.clear();
    }

    @Override
    public String toString()
    {
        if (ready())
        {
            return "CheckList{ready}";
        }
        else
        {
            EnumSet<E> clone = all.clone();
            clone.removeAll(work);
            return "CheckList{"+clone+"}";
        }
    }
}  
