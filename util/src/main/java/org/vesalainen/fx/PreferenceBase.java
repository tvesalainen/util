/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class PreferenceBase<T> implements Property<T>
{
    protected final Preferences preferences;
    protected final String key;
    protected final T def;
    private final List<InvalidationListener> invalidationListeners = new ArrayList<>();

    public PreferenceBase(Preferences preferences, String key, T def)
    {
        this.preferences = preferences;
        this.key = key;
        this.def = def;
    }
    protected void invalidate()
    {
        invalidationListeners.forEach((obs) ->
        {
            obs.invalidated(this);
        });
    }
    @Override
    public void bind(ObservableValue<? extends T> observable)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unbind()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBound()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bindBidirectional(Property<T> other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unbindBidirectional(Property<T> other)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getBean()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return key;
    }

    @Override
    public void addListener(ChangeListener<? super T> listener)
    {
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener)
    {
    }

    @Override
    public void addListener(InvalidationListener listener)
    {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener)
    {
        invalidationListeners.remove(listener);
    }

}
