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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import static org.vesalainen.ham.hffax.Fax.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageRectifier extends JavaLogging
{
    private static final int DEPTH = 5;
    private WritableRaster raster;
    private int width;
    private int height;
    private int[] buffer;
    private int[] offset;

    public ImageRectifier(URL url) throws IOException
    {
        this(ImageIO.read(url));
    }

    public ImageRectifier(File file) throws IOException
    {
        this(ImageIO.read(file));
    }

    public ImageRectifier(BufferedImage image)
    {
        this(image.getRaster());
    }

    public ImageRectifier(WritableRaster raster)
    {
        super(ImageRectifier.class);
        this.raster = raster;
        width = raster.getWidth();
        height = raster.getHeight();
        buffer = new int[width*height];
        offset = new int[height];
    }
    public void rectify()
    {
        raster.getPixels(0, 0, width, height, buffer);
        // make first line as example
        int topBlackInPixels = topBlackInPixels(width);
        int topWhiteInPixels = topWhiteInPixels(width);
        for (int d=0;d<DEPTH;d++)
        {
            for (int ii=0;ii<topBlackInPixels;ii++)
            {
                buffer[width*d+ii] = 0;
            }
            for (int ii=0;ii<topWhiteInPixels;ii++)
            {
                buffer[width*d+ii+topBlackInPixels] = 1;
            }
        }
        // correct line by line
        for (int line=DEPTH;line<height;line++)
        {
            int maxPix = 0;
            int newOff = 0;
            for (int off=0;off<width;off++)
            {
                int samePixels = samePixels(line, off, 0);
                if (samePixels > maxPix)
                {
                    maxPix = samePixels;
                    newOff = off;
                }
            }
            offset[line] = (offset[line-1]+(newOff-offset[line-1]))%width;
        }
        int[] tmp = new int[width];
        for (int line=1;line<height;line++)
        {
            int sol = width*line;
            int off = offset[line];
            System.arraycopy(buffer, sol+off, tmp, 0, width - off);
            System.arraycopy(buffer, sol, buffer, sol+width-off, off);
            System.arraycopy(tmp, 0, buffer, sol, width - off);
        }
        raster.setPixels(0, 0, width, height, buffer);
    }
    private int samePixels(int line, int start, int lastMax)
    {
        int errors = 0;
        int maxErrors = DEPTH*width-lastMax;
        for (int ii=0;ii<width;ii++)
        {
            for (int d=1;d<=DEPTH;d++)
            {
                int idx1 = ii+offset[line-d];
                int idx2 = start+ii;
                int pix1 = pixel(line-d, idx1);
                int pix2 = pixel(line, idx2);
                if (pix1 != pix2)
                {
                    errors++;
                    if (errors >= maxErrors)
                    {
                        break;
                    }
                }
            }
        }
        return DEPTH*width-errors;
    }
    private int pixel(int line, int index)
    {
        int idx = width*line+(index+2*width)%width;
        return buffer[idx];
    }
}
