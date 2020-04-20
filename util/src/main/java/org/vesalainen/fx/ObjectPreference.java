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

import java.util.prefs.Preferences;
import javafx.util.StringConverter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectPreference<T> extends PreferenceBase<T>
{
    private final StringConverter<T> converter;
    
    public ObjectPreference(Preferences preferences, String key, T def, StringConverter<T> converter)
    {
        super(preferences, key, def);
        this.converter = converter;
    }

    @Override
    public T getValue()
    {
        return converter.fromString(preferences.get(key, converter.toString(def)));
    }

    @Override
    public void setValue(T def)
    {
        if (def != null)
        {
            preferences.put(key, converter.toString(def));
        }
        else
        {
            preferences.remove(key);
        }
        fireValueChangedEvent();
    }
    
}
