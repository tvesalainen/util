/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @deprecated InterfaceDispatcher implements same functionality but faster.
 * A PropertySetter that can dispatch property set events to other PropertySetter's.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface PropertySetterDispatcher extends PropertySetter
{
    /**
     * Adds PropertySetter observer for given property. When this PropertySetter's
     * set method is called, it will dispatch the call to observing PropertySetter.
     * @param key
     * @param ps 
     */
    void addObserver(String key, PropertySetter ps);
    /**
     * Returns true if there is an observer for given property.
     * @param property
     * @return 
     */
    boolean containsProperty(String property);
    /**
     * Returns true if there are no observer PropertySetter's.
     * @return 
     */
    boolean isEmpty();
    /**
     * Removes PropertySetter observer from given property.
     * @param key
     * @param ps 
     */
    void removeObserver(String key, PropertySetter ps);
    
}
