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
package org.vesalainen.util;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 *
 * @author tkv
 */
public class Recycler
{
    private static final MapList<Class<?>,Recyclable> mapList = new HashMapList<>();
    private static final Lock lock = new ReentrantLock();
    
    public static final <T extends Recyclable> T get(Class<T> cls)
    {
        return get(cls, null);
    }
    public static final <T extends Recyclable> T get(Class<T> cls, Consumer<T> initializer)
    {
        T recyclable = null;
        lock.lock();
        try
        {
            List<Recyclable> list = mapList.get(cls);
            if (list != null && !list.isEmpty())
            {
                recyclable = (T) list.remove(list.size()-1);
            }
        }
        finally
        {
            lock.unlock();
        }
        if (recyclable == null)
        {
            try
            {
                recyclable = cls.newInstance();
            }
            catch (InstantiationException | IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        if (initializer != null)
        {
            initializer.accept(recyclable);
        }
        return (T) recyclable;
    }
    
    public static final <T extends Recyclable> void recycle(T recyclable)
    {
        recyclable.clear();
        lock.lock();
        try
        {
            mapList.add(recyclable.getClass(), recyclable);
        }
        finally
        {
            lock.unlock();
        }
    }
}
