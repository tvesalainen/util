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
package org.vesalainen.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;

/**
 * A class that implements PropertySetter with MapList
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.util.MapList
 */
public class MapListPropertySetter extends AbstractPropertySetter
{
    private final MapList<String,Object> map = new HashMapList<>();
    private Map<String,Object> active = new HashMap<>();
    private Map<String,Object> committed = new HashMap<>();

    public MapListPropertySetter()
    {
    }

    public MapListPropertySetter(String... prefixes)
    {
        super(prefixes);
    }

    @Override
    public void setProperty(String property, Object arg)
    {
        map.add(property, arg);
        active.put(property, arg);
    }
    
    public List<Object> getProperty(String property)
    {
        return map.get(property);
    }

    @Override
    public void commit(String reason)
    {
        Map<String,Object> safe = committed;
        committed = active;
        active = safe;
        active.clear();
        super.commit(reason);
    }

    @Override
    public void rollback(String reason)
    {
        active.forEach((s,o)->map.removeItem(s, o));
        active.clear();
        super.rollback(reason);
    }
    
}
