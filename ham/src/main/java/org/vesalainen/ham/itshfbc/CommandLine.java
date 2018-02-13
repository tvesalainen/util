/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.itshfbc;

import java.util.ArrayList;
import java.util.List;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CommandLine<T>
{
    private Command command;
    private List<T> columns;
    private int columnLength;
    private String columnFormat;

    public CommandLine(Command command, int columnLength, T... columns)
    {
        this.command = command;
        this.columnLength = columnLength;
        this.columns = Lists.create(columns);
    }

    public void add(T col)
    {
        columns.add(col);
    }
    public void set(int index, T col)
    {
        columns.add(index, col);
    }
    public T get(int index)
    {
        return columns.get(index);
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10.10s", command));
        int index = 0;
        for (T col : columns)
        {
            String colFmt = columnFormat(index++);
            String format = String.format(colFmt, col);
            sb.append(format);
        }
        return sb.toString().trim();
    }
    private String columnFormat(int index)
    {
        if (columns.get(index).getClass().equals(String.class))
        {
            return String.format("%%-%d.%ds", columnLength, columnLength);
        }
        else
        {
            return String.format("%%%d.%ds", columnLength, columnLength);
        }
    }
}
