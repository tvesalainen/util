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
package org.vesalainen.code;

import java.util.Set;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 * PropertySetterDispatcher is a PropertySetter which can dispatch property
 * settings to several PropertySetters acting as observers.
 * @author tkv
 */
public class PropertySetterDispatcher implements PropertySetter
{
    private final MapList<String,PropertySetter> mapList = new HashMapList<>();
    
    @Override
    public String[] getPrefixes()
    {
        Set<String> keySet = mapList.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    public void addObserver(String key, PropertySetter ps)
    {
        mapList.add(key, ps);
    }
    
    public void removeObserver(String key, PropertySetter ps)
    {
        mapList.removeItem(key, ps);
    }
    
    @Override
    public void set(String property, boolean arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, char arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, short arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, int arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, long arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, float arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, double arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        for (PropertySetter ps : mapList.get(property))
        {
            ps.set(property, arg);
        }
    }

    public boolean isEmpty()
    {
        return mapList.isEmpty();
    }
    
    public boolean containsProperty(String property)
    {
        return mapList.containsKey(property);
    }
}
