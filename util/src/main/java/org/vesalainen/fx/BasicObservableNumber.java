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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class BasicObservableNumber extends BasicObservable<Object> implements ObservableNumberValue
{
    private List<WeakReference<ChangeListener<? super Number>>> listeners = new CopyOnWriteArrayList<>();
    
    public BasicObservableNumber(Object bean, String name)
    {
        super(bean, name);
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener)
    {
        listeners.add(new WeakReference(listener));
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener)
    {
        Iterator<WeakReference<ChangeListener<? super Number>>> iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            WeakReference<ChangeListener<? super Number>> wref = iterator.next();
            if (listener.equals(wref.get()))
            {
                iterator.remove();
            }
        }
    }

    
}
