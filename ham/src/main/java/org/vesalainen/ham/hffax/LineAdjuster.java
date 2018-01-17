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

import java.util.Arrays;
import org.vesalainen.math.BestFitLine;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineAdjuster
{
    private static final int SIZE = 100;
    private static final long DELTA = 1000;
    private final long initialLineLength;
    private long lineLength;
    private long startOfLine;
    private long[] startOfLines;
    private long[] lineLens;
    private int[] errors;
    private int index;
    private BestFitLine bestFitLine;

    public LineAdjuster(long initialLineLength)
    {
        this.initialLineLength = initialLineLength;
        this.lineLength = initialLineLength;
        this.startOfLines = new long[SIZE];
        this.lineLens = new long[SIZE-1];
        this.errors = new int[SIZE];
        this.bestFitLine = new BestFitLine();
    }
    public void reset()
    {
        index = 0;
        lineLength = initialLineLength;
        Arrays.fill(startOfLines, 0);
        Arrays.fill(lineLens, 0);
        Arrays.fill(errors, 0);
        bestFitLine.reset();
    }
    public boolean update(int errs, long time)
    {
        if (index > 0)
        {
            long ll = time - startOfLines[index-1];
            if (ll < initialLineLength/2)
            {
                if (errs <= errors[index-1])
                {
                    errors[index-1] = errs;
                    startOfLines[index-1] = time;
                }
            }
            else
            {
                calc();
                errors[index] = errs;
                startOfLines[index++] = time;
            }
        }
        else
        {
            errors[index] = errs;
            startOfLines[index++] = time;
        }
        return index > 2;
    }
    private void calc()
    {
        startOfLine = startOfLines[0];
        if (index > 1)
        {
            long sol = startOfLine;
            long ll = lineLength;
            lineLens[index-2] = startOfLines[index-1]-startOfLines[index-2];
            bestFitLine.add(line(startOfLines[index-2]), startOfLines[index-2]);
            switch (index)
            {
                case 1:
                    lineLength = initialLineLength;
                    break;
                case 2:
                case 3:
                case 4:
                    lineLength = averageLineLength();
                    break;
                default:
                    startOfLine = (long) bestFitLine.getY(0);
                    lineLength = averageLineLength();
                    break;
            }
            if (ll != lineLength)
            {
                long d = lineLength-ll;
                System.err.println(ll+"->"+d+" == "+lineLength);
            }
            if (sol != startOfLine)
            {
                long d = startOfLine-sol;
                System.err.println(sol+"->"+d);
            }
        }
    }
    private long estimatedStartOfLine()
    {
        bestFitLine.reset();
        for  (int ii=1;ii<index;ii++)
        {
            if (nearLineLength(lineLens[ii-1]))
            {
                long line = line(startOfLines[ii-1]);
                bestFitLine.add(line, startOfLines[ii-1]);
                line = line(startOfLines[ii]);
                bestFitLine.add(line, startOfLines[ii]);
                ii++;
            }                        
        }
        return (long) bestFitLine.getY(0);
    }
    private long line(long time)
    {
        return (time-startOfLine+DELTA)/initialLineLength;
    }
    private long averageLineLength()
    {
        int count = 0;
        long sum = 0;
        for  (int ii=1;ii<index;ii++)
        {
            if (nearLineLength(lineLens[ii-1]))
            {
                sum += lineLens[ii-1];
                count++;
            }                        
        }
        if (count > 0)
        {
            return sum/count;
        }
        else
        {
            return initialLineLength;
        }
    }
    private boolean nearLineLength(long value)
    {
        long diff = initialLineLength-value;
        return diff > -DELTA && diff < DELTA;
    }
    public long getLineLength()
    {
        return lineLength;
    }

    public long getStartOfLine()
    {
        return startOfLine;
    }
    public int getFirstLine()
    {
        return (int) line(startOfLines[index-1]);
    }
}
