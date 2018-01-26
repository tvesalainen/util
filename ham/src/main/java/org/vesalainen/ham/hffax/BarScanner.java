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
    private int negativeLength;
    private BarPredicate predicate;
    private int[] lineBegin;
    private int[] lineLength;

    public BarScanner(int width, int verticalError)
    {
        this(width, verticalError, (mb, ml, nb, nl, ne)->nl>ml);
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
        int neg = 0;
        int negLen = 0;
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
                if (len > 0 && predicate.test(begin, length, beg, len, negLen))
                {
                    begin = beg;
                    length = len;
                    negativeLength = negLen;
                    neg = 0;
                }
                beg += len+1;
                len = 0;
                if ((height-err) <= verticalError)
                {
                    neg++;
                }
            }
            else
            {
                if (len == 0)
                {
                    negLen = neg;
                    neg = 0;
                }
                len++;
            }
        }
        // calc per line
        if (lineBegin == null || lineBegin.length < height)
        {
            lineBegin = new int[height];
            lineLength = new int[height];
        }
        for (int y=0;y<height;y++)
        {
            for (int x=0;x<begin;x++)
            {
                if (buffer[((begin-x)%width)+y*width] != color)
                {
                    break;
                }
                else
                {
                    lineBegin[y] = begin-x;
                }
            }
            int xx = begin+length-1;
            int ll = width-length;
            for (int x=0;x<ll;x++)
            {
                if (buffer[((xx+x)%width)+y*width] != color)
                {
                    break;
                }
                else
                {
                    lineLength[y] = begin-lineBegin[y]+length+x;
                }
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

    public int getBegin(int line)
    {
        return lineBegin[line];
    }

    public int getLength(int line)
    {
        return lineLength[line];
    }

    public int getNegativeLength()
    {
        return negativeLength;
    }

}
