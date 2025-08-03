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
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConvertUtilityException extends RuntimeException
{

    /**
     * Creates a new instance of <code>ConvertUtilityException</code> without detail message.
     */
    public ConvertUtilityException()
    {
    }
    public ConvertUtilityException(Class<?> expectedReturnType, Object object, Throwable thr)
    {
        super("expectedReturnType="+expectedReturnType.getName()+" object="+object.getClass().getName(), thr);
    }

    /**
     * Constructs an instance of <code>ConvertUtilityException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConvertUtilityException(String msg)
    {
        super(msg);
    }
}
