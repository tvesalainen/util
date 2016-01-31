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
package org.vesalainen.bean;

/**
 * Simple implementation where base object can be changed
 * @author tkv
 * @param <T>
 */
public class SimpleBeanField<T> extends AbstractBeanField<T>
{
    private T base;

    public SimpleBeanField(T base, String fieldname)
    {
        super((Class<? extends T>)base.getClass(), fieldname);
        this.base = base;
    }

    public void setBase(T base)
    {
        this.base = base;
    }
    
    @Override
    protected T getBase()
    {
        return base;
    }
    
}
