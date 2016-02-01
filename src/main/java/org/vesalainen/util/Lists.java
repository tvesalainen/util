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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Lists class contains methods to construct often used lists
 * @author tkv
 */
public class Lists
{
    /**
     * Creates a list that is populated with items
     * @param <T>
     * @param items
     * @return 
     */
    public static final <T> List<T> create(T... items)
    {
        List<T> list = new ArrayList<>();
        populate(list, items);
        return list;
    }
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
     * Removes items from collection
     * @param <T>
     * @param collection
     * @param items 
     */
    public static final <T> void remove(Collection<T> collection, T... items)
    {
        for (T t : items)
        {
            collection.remove(t);
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
        try
        {
            StringBuilder out = new StringBuilder();
            print(out, start, delim, quotStart, quotEnd, end, collection);
            return out.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static final void print(Appendable out, String start, String delim, String quotStart, String quotEnd, String end, Collection<?> collection) throws IOException
    {
        append(start, out);
        boolean first = true;
        for (Object ob : collection)
        {
            if (!first)
            {
                append(delim, out);
            }
            else
            {
                first=false;
            }
            append(quotStart, out);
            out.append(ob.toString());
            append(quotEnd, out);
        }
        append(end, out);
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
        try
        {
            StringBuilder out = new StringBuilder();
            print(out, start, delim, quotStart, quotEnd, end, array);
            return out.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static final void print(Appendable out, String start, String delim, String quotStart, String quotEnd, String end, Object... array) throws IOException
    {
        append(start, out);
        boolean first = true;
        for (Object ob : array)
        {
            if (!first)
            {
                append(delim, out);
            }
            else
            {
                first=false;
            }
            append(quotStart, out);
            out.append(ob.toString());
            append(quotEnd, out);
        }
        append(end, out);
    }
    private static void append(String str, Appendable out) throws IOException
    {
        if (str != null)
        {
            out.append(str);
        }
    }
}
