/*
 * Copyright (C) 2015 tkv
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A class that stores AutoCloseable objects. This class implements AutoCloseable.
 * When this classes close methods is called it will close all stored object.
 * 
 * <p>This class has weak references to stored object. Therefore it is not necessary
 * to remove objects not-in-use.
 * @author tkv
 * @param <T>
 */
public class AutoCloseableList<T extends AutoCloseable> implements AutoCloseable
{
    private final List<WeakReference<T>> list = new ArrayList<>();

    public AutoCloseableList()
    {
    }

    public AutoCloseableList(Collection<T> collection)
    {
        for (T t : collection)
        {
            list.add(new WeakReference<>(t));
        }
    }
    
    public final void add(T item)
    {
        Iterator<WeakReference<T>> iterator = list.iterator();
        while (iterator.hasNext())
        {
            WeakReference<T> next = iterator.next();
            if (next.get() == null)
            {
                iterator.remove();
            }
        }
        list.add(new WeakReference<>(item));
    }
    
    @Override
    public void close() throws IOException
    {
        for (WeakReference<T> r : list)
        {
            try
            {
                T t = r.get();
                if (t != null)
                {
                    t.close();
                }
            }
            catch (Exception ex)
            {
                throw new IOException(ex);
            }
        }
    }
    
}
