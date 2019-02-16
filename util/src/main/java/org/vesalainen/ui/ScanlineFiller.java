/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.function.IntPredicate;
import org.vesalainen.util.ArrayHelp;

/**
 * ScanlineFiller minimizes line load/store count
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class ScanlineFiller extends AbstractLineFiller
{
    protected int width;
    protected int height;
    private int[][] lines = new int[3][];
    private int[] lineNums = new int[]{-2, -2, -2};
    private int index;
    private int x;
    private int y;
    private PointQueue northQueue;
    private PointQueue southQueue;
    /**
     * Creates ScanlineFiller
     * @param width
     * @param height 
     */
    public ScanlineFiller(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.northQueue = new PointQueue(2*width, this::set);
        this.southQueue = new PointQueue(2*width, this::set);
        for (int ii=0;ii<3;ii++)
        {
            lines[ii] = new int[width];
        }
    }
    /**
     * Fills area starting at xx,yy. Area must be surrounded with replacement
     * color.
     * @param xx
     * @param yy
     * @param replacement 
     */
    public void floodFill(int xx, int yy, int replacement)
    {
        floodFill(xx, yy, (c)->c!=replacement, replacement);
    }
    /**
     * Fills clipped area starting at xx,yy. Area must be surrounded with 
     * replacement (or clip)
     * @param xx
     * @param yy
     * @param clip
     * @param replacement 
     */
    public void floodFill(int xx, int yy, Rectangle clip, int replacement)
    {
        floodFill(xx, yy, clip, (c)->c!=replacement, replacement);
    }
    /**
     * Fills area starting at xx,yy. Pixels fullfilling target are replaced with
     * replacement color.
     * @param xx
     * @param yy
     * @param target
     * @param replacement 
     */
    public void floodFill(int xx, int yy, IntPredicate target, int replacement)
    {
        floodFill(xx, yy, 0, 0, width, height, target, replacement);
    }
    /**
     * Fills clipped area starting at xx,yy. Area must be surrounded with 
     * replacement (or clip)
     * @param xx
     * @param yy
     * @param clip
     * @param target
     * @param replacement 
     */
    public void floodFill(int xx, int yy, Rectangle clip, IntPredicate target, int replacement)
    {
        floodFill(xx, yy, clip.x, clip.y, clip.x+clip.width, clip.y+clip.height, target, replacement);
    }
    /**
     * Fills clipped (minX, minY, maxX, maxY) area starting at xx,yy. Area must 
     * be surrounded with replacement (or clip)
     * @param xx
     * @param yy
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param target
     * @param replacement 
     */
    public void floodFill(int xx, int yy, int minX, int minY, int maxX, int maxY, IntPredicate target, int replacement)
    {
        if (xx<minX || yy<minY || xx>minX+width || yy>minY+height)
        {
            return;
        }
        this.x = xx;
        this.y = yy;
        ensure(index, y);
        ensure(north(), y-1);
        ensure(south(), y+1);
        int color = lines[index][x];
        if (color == replacement || !target.test(color))
        {
            return;
        }
        southQueue.add(x, y);
        while (take())
        {
            int[] n = lines[north()];
            int[] l = lines[index];
            int[] s = lines[south()];
            for (int ii=x;ii<maxX;ii++)
            {
                if (target.test(l[ii]))
                {
                    l[ii] = replacement;
                    if (y-1 >= minY && target.test(n[ii]))
                    {
                        northQueue.add(ii, y-1);
                    }
                    if (y+1 < maxY && target.test(s[ii]))
                    {
                        southQueue.add(ii, y+1);
                    }
                }
                else
                {
                    break;
                }
            }
            for (int ii=x-1;ii>=minX;ii--)
            {
                if (target.test(l[ii]))
                {
                    l[ii] = replacement;
                    if (y-1 >= minY && target.test(n[ii]))
                    {
                        northQueue.add(ii, y-1);
                    }
                    if (y+1 < maxY && target.test(s[ii]))
                    {
                        southQueue.add(ii, y+1);
                    }
                }
                else
                {
                    break;
                }
            }
        }
        for (int ii=0;ii<3;ii++)
        {
            if (lineNums[ii] >= 0)
            {
                storeLine(lineNums[ii], lines[ii]);
            }
            lineNums[ii] = -2;
        }
    }
    private boolean take()
    {
        if (northQueue.isEmpty())
        {
            if (southQueue.isEmpty())
            {
                return false;
            }
            southQueue.take();
        }
        else
        {
            northQueue.take();
        }
        int idx;
        if (lineNums[index] != y) 
        {
            idx = ArrayHelp.indexOf(lineNums, y);
            if (idx != -1)
            {
                index = idx;
                ensure(north(), y-1);
                ensure(south(), y+1);
            }
            else
            {
                idx = ArrayHelp.indexOf(lineNums, y-1);
                if (idx != -1)
                {
                    index = (idx+1)%3;
                    ensure(index, y);
                    ensure(south(), y+1);
                }
                else
                {
                    idx = ArrayHelp.indexOf(lineNums, y+1);
                    if (idx != -1)
                    {
                        index = (idx+2)%3;
                        ensure(index, y);
                        ensure(north(), y-1);
                    }
                }
            }
        }
        return true;
    }
    private void ensure(int idx, int line)
    {
        if (lineNums[idx] != line && line >= 0 && line < height)
        {
            if (lineNums[idx] >= 0)
            {
                storeLine(lineNums[idx], lines[idx]);
            }
            loadLine(line, lines[idx]);
            lineNums[idx] = line;
        }
    }
    private void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    private int north()
    {
        return (index+2)%3;
    }
    private int south()
    {
        return (index+1)%3;
    }

    static class PointQueue
    {
        private int[] queue;
        private int start;
        private int end;
        private int queueLength;
        private int size;
        private PlotOperator op;

        public PointQueue(int initialSize, PlotOperator op)
        {
            this.queue = new int[2*initialSize];
            this.queueLength = 2*initialSize;
            this.op = op;
        }

        public void add(Point p)
        {
            add(p.x, p.y);
        }
        public void add(int x, int y)
        {
            if (size >= queueLength)
            {
                if (start == 0)
                {
                    queue = Arrays.copyOf(queue, 2*queueLength);
                    end = queueLength;
                }
                else
                {
                    int[] arr = new int[2*queueLength];
                    System.arraycopy(queue, end, arr, 0, queueLength-end);
                    System.arraycopy(queue, 0, arr, queueLength-end, end);
                    queue = arr;
                    start = 0;
                    end = queueLength;
                }
                queueLength *= 2;
            }
            queue[end++] = x;
            queue[end++] = y;
            end %= queueLength;
            size += 2;
        }
        public void take()
        {
            if (size <= 0)
            {
                throw new IllegalArgumentException("underflow");
            }
            int x = queue[start++];
            int y = queue[start++];
            start %= queueLength;
            op.plot(x, y);
            size -= 2;
        }
        public boolean isEmpty()
        {
            return size <= 0;
        }
    }
}
