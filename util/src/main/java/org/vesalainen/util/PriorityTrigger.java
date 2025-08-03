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
package org.vesalainen.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * PriorityTrigger has key/action pairs in priority order. Action is triggered
 * only if last key was lower or equal than given key. This works fine if lower
 * priority events are triggered with same or lower frequency. If that is not
 * the case, the lower priority events will go through occasionally.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T> key type
 */
public class PriorityTrigger<T>
{
    private final T[] keys;
    private final Consumer<T>[] actions;
    private T last;
    private int lastIndex;

    private PriorityTrigger(List<T> keys, List<Consumer<T>> actions)
    {
        this.lastIndex = keys.size()-1;
        T lst = keys.get(lastIndex);
        this.keys = keys.toArray((T[]) Array.newInstance(lst.getClass(), keys.size()));
        this.actions = actions.toArray((Consumer[]) (Array.newInstance(Consumer.class, keys.size())));
        this.last = lst;
    }
    
    private void fire(T key)
    {
        if (last.equals(key))
        {
            actions[lastIndex].accept(key);
        }
        else
        {
            int indexOf = ArrayHelp.indexOf(keys, key);
            if (indexOf == -1)
            {
                throw new IllegalArgumentException(key+" is not allowed as a key");
            }
            if (indexOf < lastIndex)
            {
                actions[indexOf].accept(key);
            }
            last = key;
            lastIndex = indexOf;
        }
    }
    public static <T> Builder<T> builder()
    {
        return new Builder<>();
    }
    public static class Builder<T>
    {
        private final List<T> keys = new ArrayList<>();
        private final List<Consumer<T>> actions = new ArrayList<>();
        /**
         * Returns action triggered by key.
         * @return 
         */
        public Consumer<T> build()
        {
            if (keys.isEmpty())
            {
                return (k)->{};
            }
            if (keys.size() > 1)
            {
                PriorityTrigger<T> priorityTrigger = new PriorityTrigger<>(keys, actions);
                return priorityTrigger::fire;
            }
            else
            {
                return actions.get(0);
            }
        }
        /**
         * Add key/action pair in priority order
         * @param key
         * @param action 
         */
        public void add(T key, Runnable action)
        {
            add(key, (t)->action.run());
        }
        /**
         * Add key/action pair in priority order
         * @param key
         * @param action 
         */
        public void add(T key, Consumer<T> action)
        {
            keys.add(key);
            actions.add(action);
        }
    }
}
