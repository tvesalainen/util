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

import java.util.prefs.Preferences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StringPreference extends PreferenceBase<String>
{

    public StringPreference(Preferences preferences, String key, String def)
    {
        super(preferences, key, def);
    }
    
    @Override
    public String getValue()
    {
        return preferences.get(key, def);
    }

    @Override
    public void setValue(String value)
    {
        if (value != null)
        {
            preferences.put(key, value);
        }
        else
        {
            preferences.remove(key);
        }
        invalidated();
    }
    
}
