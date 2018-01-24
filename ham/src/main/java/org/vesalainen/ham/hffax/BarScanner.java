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
package org.vesalainen.ham.hffax;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BarScanner
{
    private int width;
    private int verticalError;
    private int begin;
    private int length;
    private int line;
    private int lineHeight;
    private BarPredicate predicate;

    public BarScanner(int width, int verticalError)
    {
        this(width, verticalError, (mb, ml, nb, nl)->nl>ml);
    }
    public BarScanner(int width, int verticalError, BarPredicate predicate)
    {
        this.width = width;
        this.verticalError = verticalError;
        this.predicate = predicate;
    }

    public void maxBar(int[] buffer, int color, int height)
    {
        int beg = 0;
        int len = 0;
        begin = 0;
        length = 0;
        predicate.reset();
        while (beg+len<width*2)
        {
            int err = 0;
            for (int y=0;y<height;y++)
            {
                if (buffer[((beg+len)%width)+y*width] != color)
                {
                    err++;
                }
            }
            if (err > verticalError)
            {
                if (len > 0 && predicate.test(begin, length, beg, len))
                {
                    begin = beg;
                    length = len;
                }
                beg += len+1;
                len = 0;
            }
            else
            {
                len++;
            }
        }
    }
    public int getBegin()
    {
        return begin;
    }

    public int getLength()
    {
        return length;
    }

    public int getLine()
    {
        return line;
    }

    public int getLineHeight()
    {
        return lineHeight;
    }

}
