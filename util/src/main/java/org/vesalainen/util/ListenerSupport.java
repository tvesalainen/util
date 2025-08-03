/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A support class for listeners
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ListenerSupport<T>
{
    private List<Listener<T>> listeners = new ArrayList<>();
    
    public void addListener(Listener<T> listener)
    {
        listeners.add(listener);
    }
    public void addListeners(Collection<Listener<T>> listeners)
    {
        this.listeners.addAll(listeners);
    }
    public void removeListener(Listener<T> listener)
    {
        listeners.remove(listener);
    }

    public List<Listener<T>> getListeners()
    {
        return listeners;
    }
    
    public void fire(T item)
    {
        for (Listener<T> listener : listeners)
        {
            listener.update(item);
        }
    }

}
