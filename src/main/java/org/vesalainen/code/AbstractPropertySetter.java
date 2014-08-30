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

/**
 * A class throwing exception for every not handled property
 * @author Timo Vesalainen
 */
public class AbstractPropertySetter implements PropertySetter
{

    @Override
    public void set(String property, boolean arg)
    {
        throw new UnsupportedOperationException("Not supported for boolean property '"+property+"'");
    }

    @Override
    public void set(String property, byte arg)
    {
        throw new UnsupportedOperationException("Not supported for byte property '"+property+"'");
    }

    @Override
    public void set(String property, char arg)
    {
        throw new UnsupportedOperationException("Not supported for char property '"+property+"'");
    }

    @Override
    public void set(String property, short arg)
    {
        throw new UnsupportedOperationException("Not supported for short property '"+property+"'");
    }

    @Override
    public void set(String property, int arg)
    {
        throw new UnsupportedOperationException("Not supported for int property '"+property+"'");
    }

    @Override
    public void set(String property, long arg)
    {
        throw new UnsupportedOperationException("Not supported for long property '"+property+"'");
    }

    @Override
    public void set(String property, float arg)
    {
        throw new UnsupportedOperationException("Not supported for float property '"+property+"'");
    }

    @Override
    public void set(String property, double arg)
    {
        throw new UnsupportedOperationException("Not supported for double property '"+property+"'");
    }

    @Override
    public void set(String property, Object arg)
    {
        throw new UnsupportedOperationException("Not supported for Object property '"+property+"'");
    }
    
}
