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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Timo Vesalainen
 * @param <T>
 */
public abstract class ListPrinter<T>
{

    protected final CodePrinter out;
    protected final CharSequence separator;
    protected T[] array;
    protected Collection<? extends T> list;

    public ListPrinter(CodePrinter out, CharSequence separator, Collection<? extends T> list)
    {
        this.out = out;
        this.separator = separator;
        this.list = list;
    }

    public ListPrinter(CodePrinter out, CharSequence separator, T[] array)
    {
        this.out = out;
        this.separator = separator;
        this.array = array;
    }

    public void print() throws IOException
    {
        boolean first = true;
        int index = 0;
        if (list != null)
        {
            for (T item : list)
            {
                if (!first)
                {
                    out.print(separator);
                }
                first = false;
                print(index, item);
                index++;
            }
        }
        else
        {
            for (T item : array)
            {
                if (!first)
                {
                    out.print(separator);
                }
                first = false;
                print(index, item);
                index++;
            }
        }
    }
    
    public void print(CharSequence str) throws IOException
    {
        out.print(str);
    }
    protected abstract void print(int index, T item) throws IOException;
}
