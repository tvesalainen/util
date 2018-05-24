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
package org.vesalainen.util;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import java.awt.image.IndexColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.vesalainen.math.Rect;

/**
 * SimpleArrayGrid backed by BitArray
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BitGrid extends SimpleArrayGrid<Boolean>
{
    private BitArray bits;
    
    public BitGrid(int width, int heigth)
    {
        this(width, heigth, 0, false);
    }

    public BitGrid(int width, int heigth, int offset, boolean boxed)
    {
        super(width, heigth, offset, boxed);
        bits = new BitArray(length);
    }

    public BitGrid(BitArray bits, int width, int height, int offset, int length, boolean boxed)
    {
        super(null, width, height, offset, length, boxed);
        this.bits = bits;
    }

    @Override
    public BitGrid view(int offset, int width)
    {
        return new BitGrid(bits, width, height, this.offset+offset, length, boxed);
    }

    protected int patternStart()
    {
        return bits.first()+offset;
    }
    protected int patternEnd()
    {
        return bits.last()+offset;
    }
    public Rectangle patternBounds()
    {
        int patternStart = patternStart();
        int patternEnd = patternEnd();
        int sy = line(patternStart);
        int ey = line(patternEnd);
        int sx = column(patternStart);
        int ex = column(patternEnd);
        return new Rectangle(sx, sy, ex-sx+1, ey-sy+1);
    }
    /**
     * Returns true if pattern overflows line
     * @return 
     */
    public boolean patternOverflow()
    {
        BitArray lin = new BitArray(width);
        bits.forEach((i)->lin.set(column(i), true));
        return lin.isSet(0) && lin.isSet(width-1);
    }
    /**
     * Returns 1.0 if all columns have set bits or less
     * @return 
     */
    public float patternLineCoverage()
    {
        BitArray lin = new BitArray(width);
        bits.forEach((i)->lin.set(column(i), true));
        return (float)lin.count()/(float)width;
    }
    /**
     * Returns 1.0 if pattern is square or less if not.
     * @return 
     */
    public float patternSquareness()
    {
        int patternStart = patternStart();
        int patternEnd = patternEnd();
        int sy = line(patternStart);
        int ey = line(patternEnd);
        int sx = column(patternStart);
        int ex = column(patternEnd);
        float actCnt = 0;
        int w = ex-sx+1;
        int h = ey-sy+1;
        for (int ii=0;ii<h;ii++)
        {
            for (int jj=0;jj<w;jj++)
            {
                if (getColor(sx+jj, sy+ii))
                {
                    actCnt++;
                }
            }
        }
        float allCnt = getSetCount();
        return actCnt/allCnt;
    }
    public int getSetCount()
    {
        return bits.count();
    }
    @Override
    protected Boolean getColor(int position)
    {
        return bits.isSet(position);
    }

    @Override
    protected void setColor(int position, Boolean color)
    {
        bits.set(position, color);
    }
    
}
