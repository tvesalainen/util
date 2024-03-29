/*
 * Copyright (C) 2014 Timo Vesalainen
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

import org.vesalainen.util.Transactional;

/**
 * A class that implements every set method by calling setProperty which throws 
 * exception for every not handled property
 * @author Timo Vesalainen
 */
public abstract class AbstractPropertySetter implements PropertySetter, Transactional
{
    private String[] prefixes;

    public AbstractPropertySetter()
    {
        this(new String[]{});
    }

    public AbstractPropertySetter(String... prefixes)
    {
        this.prefixes = prefixes;
    }
    
    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void commit(String reason)
    {
    }

    @Override
    public String[] getProperties()
    {
        return prefixes;
    }
    
}
