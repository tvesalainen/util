/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs.attributes;

import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import static org.vesalainen.vfs.attributes.FileAttributeName.normalize;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileAttributeViewImpl implements FileAttributeView
{
    private String name;
    private Map<String,Object> map;

    public FileAttributeViewImpl(String name, Map<String, Object> map)
    {
        this.name = name;
        this.map = map;
    }

    protected Object get(String name)
    {
        return map.get(normalize(name));
    }

    protected void put(String name, Object value)
    {
        map.put(normalize(name), value);
    }
    @Override
    public String name()
    {
        return name;
    }
}
