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
package org.vesalainen.util;

import java.util.Collection;

/**
 * Lists class contains methods to construct often used lists
 * @author tkv
 */
public class Lists
{
    /**
     * Populates collection with items
     * @param <T>
     * @param collection
     * @param items 
     */
    public static final <T> void populate(Collection<T> collection, T... items)
    {
        for (T t : items)
        {
            collection.add(t);
        }
    }
    /**
     * Returns collection items delimited
     * @param delim
     * @param collection
     * @return 
     */
    public static final String print(String delim, Collection<?> collection)
    {
        return print(null, delim, null, null, null, collection);
    }
    /**
     * Returns collection items delimited with given strings. If any of delimiters
     * is null, it is ignored.
     * @param start Start
     * @param delim Delimiter
     * @param quotStart Start of quotation
     * @param quotEnd End of quotation
     * @param end End
     * @param collection
     * @return 
     */
    public static final String print(String start, String delim, String quotStart, String quotEnd, String end, Collection<?> collection)
    {
        StringBuilder sb = new StringBuilder();
        append(start, sb);
        boolean first = true;
        for (Object ob : collection)
        {
            if (!first)
            {
                append(delim, sb);
            }
            else
            {
                first=false;
            }
            append(quotStart, sb);
            sb.append(ob.toString());
            append(quotEnd, sb);
        }
        append(end, sb);
        return sb.toString();
    }
    /**
     * Returns array items delimited
     * @param delim
     * @param array
     * @return 
     */
    public static final String print(String delim, Object... array)
    {
        return print(null, delim, null, null, null, array);
    }
    /**
     * Returns array items delimited with given strings. If any of delimiters
     * is null, it is ignored.
     * @param start
     * @param delim
     * @param quotStart
     * @param quotEnd
     * @param end
     * @param array
     * @return 
     */
    public static final String print(String start, String delim, String quotStart, String quotEnd, String end, Object... array)
    {
        StringBuilder sb = new StringBuilder();
        append(start, sb);
        boolean first = true;
        for (Object ob : array)
        {
            if (!first)
            {
                append(delim, sb);
            }
            else
            {
                first=false;
            }
            append(quotStart, sb);
            sb.append(ob.toString());
            append(quotEnd, sb);
        }
        append(end, sb);
        return sb.toString();
    }
    private static final void append(String str, StringBuilder out)
    {
        if (str != null)
        {
            out.append(str);
        }
    }
}
