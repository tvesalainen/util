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

import java.awt.image.BufferedImage;
import java.util.function.IntPredicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageScanlineFiller extends ScanlineFiller
{
    private BufferedImage image;
    private int offset;
    private int length;

    public ImageScanlineFiller(BufferedImage image)
    {
        super(image.getWidth(), image.getHeight());
        this.image = image;
    }

    @Override
    public void floodFill(int xx, int yy, int minX, int minY, int maxX, int maxY, IntPredicate target, int replacement)
    {
        this.offset = minX;
        this.length = maxX-minX;
        super.floodFill(xx, yy, minX, minY, maxX, maxY, target, replacement);
    }

    @Override
    protected void loadLine(int y, int[] line)
    {
        image.getRGB(offset, y, length, 1, line, offset, 0);
        //System.err.println("load  "+y+" "+Arrays.toString(line));
    }

    @Override
    protected void storeLine(int y, int[] line)
    {
        image.setRGB(offset, y, length, 1, line, offset, 0);
        //System.err.println("store  "+y+" "+Arrays.toString(line));
    }
}
