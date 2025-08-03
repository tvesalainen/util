/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import org.vesalainen.util.logging.JavaLogging;

/**
 * A class that stores AutoCloseable objects. This class implements AutoCloseable.
 * When this classes close methods is called it will close all stored objects.
 * 
 * <p>This class has weak references to stored object. Therefore it is not necessary
 * to remove objects not-in-use.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class AutoCloseableList<T extends AutoCloseable> extends JavaLogging implements AutoCloseable
{
    private final List<WeakReference<T>> list = new ArrayList<>();

    public AutoCloseableList()
    {
        setLogger(this.getClass());
    }

    public AutoCloseableList(Collection<T> collection)
    {
        setLogger(this.getClass());
        for (T t : collection)
        {
            add(t);
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
                fine("removed autocloseable");
                iterator.remove();
            }
        }
        fine("add autocloseable %s", item);
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
                    fine("closing %s", t);
                    t.close();
                    fine("closed %s", t);
                }
            }
            catch (Exception ex)
            {
            }
        }
    }
    
}
