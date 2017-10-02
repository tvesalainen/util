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

import java.nio.file.attribute.FileAttribute;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 */
public class FileAttributeImpl<T> implements FileAttribute<T>
{
    private String name;
    private T value;

    public FileAttributeImpl(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public T value()
    {
        return value;
    }
    
}
