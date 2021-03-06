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
package org.vesalainen.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * @deprecated Renamed to CollectionHelp
 * CollectionHelp class contains methods to construct often used lists
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Lists
{
    private static final ThreadLocal<Locale> threadLocale = new ThreadLocal<>();
    private static final ThreadLocal<String> threadFormat = new ThreadLocal<>();
    /**
     * Set Format string and locale for calling thread. List items are formatted
     * using these. It is good practice to call removeFormat after use.
     * @param locale 
     * @see #removeFormat() 
     * @see java.lang.String#format(java.util.Locale, java.lang.String, java.lang.Object...) 
     */
    public static final void setFormat(String format, Locale locale)
    {
        threadFormat.set(format);
        threadLocale.set(locale);
    }
    /**
     * Remove format and locale set in setFormat method.
     * @see #setFormat(java.lang.String, java.util.Locale) 
     */
    public static final void removeFormat()
    {
        threadFormat.remove();
        threadLocale.remove();
    }
    private static String format(Object ob)
    {
        String format = threadFormat.get();
        Locale locale = threadLocale.get();
        if (format != null && locale != null)
        {
            return String.format(locale, format, ob);
        }
        else
        {
            return ob.toString();
        }
    }
    /**
     * Creates a list that is populated with items
     * @param <T>
     * @param items
     * @return 
     */
    public static final <T> List<T> create(T... items)
    {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }
    /**
     * @deprecated Replace with Collections.addAll
     * Populates collection with items
     * @param <T>
     * @param collection
     * @param items 
     * @see java.util.Collections#addAll(java.util.Collection, java.lang.Object...) 
     */
    public static final <T> void populate(Collection<T> collection, T... items)
    {
        Collections.addAll(collection, items);
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
     * @deprecated Use java.util.stream.Collectors.joining
     * @see java.util.stream.Collectors#joining(java.lang.CharSequence) 
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
     * @deprecated Use java.util.stream.Collectors.joining
     * @see java.util.stream.Collectors#joining(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence) 
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
    /**
     * 
     * @param out
     * @param start
     * @param delim
     * @param quotStart
     * @param quotEnd
     * @param end
     * @param collection
     * @throws IOException 
     */
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
            out.append(format(ob));
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
            out.append(format(ob));
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
    /**
     * Returns true if list has equal items as array in same order.
     * @param <T>
     * @param list
     * @param array
     * @return 
     */
    public static <T> boolean equals(List<T> list, T... array)
    {
        if (list.size() != array.length)
        {
            return false;
        }
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (!Objects.equals(list.get(ii), array[ii]))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Adds array members to list
     * @param <T>
     * @param list
     * @param array 
     */
    public static <T> Collection<T> addAll(Collection<T> list, T... array)
    {
        for (T item : array)
        {
            list.add(item);
        }
        return list;
    }
    /**
     * Add arrays members to list starting at index.
     * <p>
     * If list contains a,b,c,d and array 1,2,3. After addAll(2, list, array)
     * list contains a,b,1,2,3,c,d 
     * @param <T>
     * @param index
     * @param list
     * @param array 
     */
    public static <T> List<T> addAll(int index, List<T> list, T... array)
    {
        for (T item : array)
        {
            list.add(index++, item);
        }
        return list;
    }
    /**
     * Converts Collection to array. 
     * @param <T>
     * @param col
     * @param cls
     * @return 
     * @see java.util.Collection#toArray(T[]) 
     */
    public static <T> T[] toArray(Collection<T> col, Class<T> cls)
    {
        T[] arr = (T[]) Array.newInstance(cls, col.size());
        return col.toArray(arr);
    }
    /**
     * Sort list using quick-sort algorithm using ForkJoinPool.
     * <p>Note! This is experimental and is slower than other sorting methods!
     * @param <T>
     * @param list
     * @param comparator 
     */
    public static final <T> void parallelQuickSort(List<T> list, Comparator<T> comparator)
    {
        ForkJoinPool pool = new ForkJoinPool();
        QuickSorter sorter = new QuickSorter(list, 0, list.size()-1, comparator);
        pool.invoke(sorter);
    }
    /**
     * Sort list using quick-sort algorithm
     * <p>Needs a big list to have any benefit to ArrayList.sort!
     * @param <T>
     * @param list
     * @param comparator 
     * @see java.util.ArrayList#sort(java.util.Comparator) 
     */
    public static final <T> void quickSort(List<T> list, Comparator<T> comparator)
    {
        quickSort(list, 0, list.size()-1, comparator);
    }
    private static <T> void quickSort(List<T> list, int lo, int hi, Comparator<T> comparator)
    {
        if (lo < hi)
        {
            int p = partition(list, lo, hi, comparator);
            quickSort(list, lo, p, comparator);
            quickSort(list, p+1, hi, comparator);
        }
    }
    private static <T> int partition(List<T> list, int lo, int hi, Comparator<T> comparator)
    {
        T pivot = list.get(lo);
        int i = lo-1;
        int j = hi+1;
        while (true)
        {
            do
            {
                i++;
            } while (compare(list.get(i), pivot, comparator) < 0);
            do
            {
                j--;
            } while (compare(list.get(j), pivot, comparator) > 0);
            if (i >= j)
            {
                return j;
            }
            T swap = list.get(i);
            list.set(i, list.get(j));
            list.set(j, swap);
        }
    }
    private static <T> int compare(T o1, T o2, Comparator<T> comparator)
    {
        if (comparator != null)
        {
            return comparator.compare(o1, o2);
        }
        else
        {
            return ((Comparable)o1).compareTo(o2);
        }
    }
    private static class QuickSorter<T> extends RecursiveAction
    {
        private List<T> list;
        private int lo;
        private int hi;
        private Comparator<T> comparator;

        public QuickSorter(List<T> list, int lo, int hi, Comparator<T> comparator)
        {
            this.list = list;
            this.lo = lo;
            this.hi = hi;
            this.comparator = comparator;
        }

        @Override
        protected void compute()
        {
            if (lo < hi)
            {
                int p = partition(list, lo, hi, comparator);
                List<ForkJoinTask> tasks = new ArrayList<>();
                if (p-lo > 50)
                {
                    tasks.add(new QuickSorter(list, lo, p, comparator));
                }
                else
                {
                    quickSort(list, lo, p, comparator);
                }
                if (hi-p+1 > 50)
                {
                    tasks.add(new QuickSorter(list, p+1, hi, comparator));
                }
                else
                {
                    quickSort(list, p+1, hi, comparator);
                }
                invokeAll(tasks);
            }
        }
        
    }
}
