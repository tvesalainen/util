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
 * Recycler class is used to recycle Recyclable objects.
 * <p>This kind of recycling is for special cases only. Implementation has to take
 * care that recycled object is not referenced. This kind of recycling can leed
 * to hard to find bugs.
 * @author tkv
 */
public class Recycler
{
    private static final MapList<Class<?>,Recyclable> mapList = new HashMapList<>();
    private static final Lock lock = new ReentrantLock();
    /**
     * Returns new or recycled uninitialized object.
     * @param <T>
     * @param cls
     * @return 
     */
    public static final <T extends Recyclable> T get(Class<T> cls)
    {
        return get(cls, null);
    }
    /**
     * Returns new or recycled initialized object.
     * @param <T>
     * @param cls
     * @param initializer
     * @return 
     */
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
    /**
     * Add object to be recycled.
     * @param <T>
     * @param recyclable 
     */
    public static final <T extends Recyclable> void recycle(T recyclable)
    {
        if (recyclable.isRecycled())
        {
            throw new IllegalArgumentException("recycling "+recyclable+" again");
        }
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
    
    static final <T extends Recyclable> boolean isRecycled(T recyclable)
    {
        return mapList.contains(recyclable.getClass(), recyclable);
    }
}
