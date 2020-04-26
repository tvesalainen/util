/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicObservable<B> implements Observable
{
    private List<WeakReference<InvalidationListener>> observables = new ArrayList<>();
    protected B bean;
    protected String key;
    protected boolean valid;

    public BasicObservable(B bean, String name)
    {
        this.bean = bean;
        this.key = name;
    }
    
    @Override
    public void addListener(InvalidationListener listener)
    {
        observables.add(new WeakReference(listener));
    }

    @Override
    public void removeListener(InvalidationListener listener)
    {
        Iterator<WeakReference<InvalidationListener>> iterator = observables.iterator();
        while (iterator.hasNext())
        {
            WeakReference<InvalidationListener> wref = iterator.next();
            if (listener.equals(wref.get()))
            {
                iterator.remove();
            }
        }
    }
    /**
     * Calls invalidate and set's valid again.
     */
    public void signal()
    {
        invalidate();
        valid = true;
    }
    public void invalidate()
    {
        fireInvalidate(this);
    }
    protected void fireInvalidate(Observable observable)
    {
        if (valid)
        {
            Iterator<WeakReference<InvalidationListener>> iterator = observables.iterator();
            while (iterator.hasNext())
            {
                WeakReference<InvalidationListener> wref = iterator.next();
                InvalidationListener il = wref.get();
                if (il == null)
                {
                    iterator.remove();
                }
                else
                {
                    il.invalidated(this);
                }
            }
            valid = false;
        }
    }

    public Object getBean()
    {
        return bean;
    }

    public String getName()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "BasicObservable{" + bean + ": " + key + '}';
    }
    
}
