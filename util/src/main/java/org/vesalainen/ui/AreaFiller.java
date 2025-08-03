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

import java.awt.Rectangle;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import org.vesalainen.util.function.IntBiPredicate;

/**
 * ScanlineFiller minimizes line load/store count
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AreaFiller extends AbstractLineFiller
{
    protected int width;
    protected int height;
    private int[] line;
    /**
     * Creates AreaFiller
     * @param width
     * @param height 
     */
    public AreaFiller(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.line = new int[width];
    }
    /**
     * Fills area . Pixels fullfilling target are replaced with
     * replacement color.
     * @param isInside
     * @param replacement 
     */
    public void fill(IntBiPredicate isInside, int replacement)
    {
        fill(0, 0, width, height, isInside, (x,y)->replacement);
    }
    /**
     * Fills area . Pixels fullfilling target are replaced with
     * replacement color.
     * <p>Note! Pattern is in relative coordinates
     * @param isInside
     * @param pattern 
     */
    public void fill(IntBiPredicate isInside, IntBinaryOperator pattern)
    {
        fill(0, 0, width, height, isInside, pattern);
    }
    /**
     * Fills clipped area. Area must be surrounded with 
     * replacement (or clip)
     * @param clip
     * @param isInside
     * @param replacement 
     */
    public void fill(Rectangle clip, IntBiPredicate isInside, int replacement)
    {
        fill(clip.x, clip.y, clip.x+clip.width, clip.y+clip.height, isInside, (x,y)->replacement);
    }
    /**
     * Fills clipped area. Area must be surrounded with 
     * replacement (or clip)
     * <p>Note! Pattern is in relative coordinates
     * @param clip
     * @param isInside 
     * @param pattern 
     */
    public void fill(Rectangle clip, IntBiPredicate isInside, IntBinaryOperator pattern)
    {
        fill(clip.x, clip.y, clip.x+clip.width, clip.y+clip.height, isInside, pattern);
    }
    /**
     * Fills clipped (minX, minY, maxX, maxY) area. Area must 
     * be surrounded with replacement (or clip)
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param isInside
     * @param replacement 
     */
    public void fill(int minX, int minY, int maxX, int maxY, IntBiPredicate isInside, int replacement)
    {
        fill(minX, minY, maxX, maxY, isInside, (x,y)->replacement);
    }
    /**
     * Fills clipped (minX, minY, maxX, maxY) area. Area must 
     * be surrounded with replacement (or clip)
     * <p>Note! Pattern is in relative coordinates
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param isInside 
     * @param pattern 
     */
    public void fill(int minX, int minY, int maxX, int maxY, IntBiPredicate isInside, IntBinaryOperator pattern)
    {
        for (int y=minY;y<=maxY;y++)
        {
            loadLine(y, line);
            for (int x=minX;x<=maxX;x++)
            {
                if (isInside.test(x, y))
                {
                    line[x] = pattern.applyAsInt(x, y);
                }
            }
            storeLine(y, line);
        }
    }

}
