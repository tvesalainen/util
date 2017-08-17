/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util;

/**
 * Interface for classes supporting conversion between T and C types.
 * T type is usually the type of implementing class and type C is most
 * often String
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface Convertable<T,C>
{
    /**
     * Convert this object to object type of C
     * @return
     */
    C convertTo();
}
