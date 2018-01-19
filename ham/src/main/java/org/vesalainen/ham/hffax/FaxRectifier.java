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
import org.vesalainen.math.BestFitLine;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxRectifier
{
    private static final int ERR_LIM = 10;
    private static final int ERR_SUM_LIM = 10;
    private WritableRaster raster;
    private int width;
    private int height;
    private int[] buffer;
    private int[] pattern;
    private int whiteLen;
    private int len;
    private int bufferLen;
    private int patternLen;
    private int[] error;
    private int[] dif;
    private int[] tmp;

    public FaxRectifier(URL url) throws IOException
    {
        this(ImageIO.read(url));
    }

    public FaxRectifier(File file) throws IOException
    {
        this(ImageIO.read(file));
    }

    public FaxRectifier(BufferedImage image)
    {
        this(image.getRaster());
    }

    public FaxRectifier(WritableRaster raster)
    {
        this.raster = raster;
        width = raster.getWidth();
        height = raster.getHeight();
        buffer = new int[width];
        tmp = new int[width];
        pattern = new int[width*(76+40)/2300];
        whiteLen = width*40/2300;
        len = buffer.length+pattern.length;
        bufferLen = buffer.length;
        patternLen = pattern.length;
        error = new int[height];
        dif = new int[height];
        for (int ii=0;ii<whiteLen;ii++)
        {
            pattern[ii] = 1;
        }
    }
    public void rectify()
    {
        int minErr;
        int end = -1;
        for (int  line=0;line<height;line++)
        {
            raster.getPixels(0, line, width, 1, buffer);
            minErr = Integer.MAX_VALUE;
            for (int col=0;col<len;col++)
            {
                int err = 0;
                for (int pat=0;pat<patternLen&err<minErr;pat++)
                {
                    if (buffer[(col+pat)%bufferLen] != pattern[pat])
                    {
                        err++;
                    }
                }
                if (err < minErr)
                {
                    minErr = err;
                    end = (col+patternLen);
                }
            }
            error[line] = minErr;
            dif[line] = bufferLen < end ? end-bufferLen : end;
        }
        Part part = new Part(0, height);
        process(part);
    }
    private void process(Part part)
    {
        if (part.isOk())
        {
            part.rectify();
        }
        else
        {
            if (part.getLength() > 20)
            {
                Part[] parts = part.split();
                if (parts[0].isOk() != parts[1].isOk())
                {
                    if (parts[0].isOk())
                    {
                        steal(parts[0], parts[1]);
                    }
                    else
                    {
                        steal(parts[1], parts[0]);
                    }
                }
                process(parts[0]);
                process(parts[1]);
            }
        }
    }
    private void steal(Part to, Part from)
    {
        boolean after = from.isAfter(to);
        while (to.isOk())
        {
            if (after)
            {
                to.newTail(1);
                from.newHead(1);
            }
            else
            {
                from.newTail(-1);
                to.newHead(-1);
            }
        }
        if (after)
        {
            to.newTail(-1);
            from.newHead(-1);
        }
        else
        {
            from.newTail(1);
            to.newHead(1);
        }
    }
    private class Part
    {
        private int offset;
        private int length;
        private final BestFitLine bestFitLine;
        private int errorSum;

        public Part(int offset, int length)
        {
            this.offset = offset;
            this.length = length;
            bestFitLine = new BestFitLine();
            calc();
        }
        private void calc()
        {
            bestFitLine.reset();
            errorSum = 0;
            for (int ii=0;ii<length;ii++)
            {
                int index = ii+offset;
                addBestFit(index, 1);
            }
            for (int ii=0;ii<length;ii++)
            {
                int index = ii+offset;
                addErrSum(index, 1);
            }
        }
        public boolean isAfter(Part other)
        {
            return offset > other.offset;
        }
        public boolean isOk()
        {
            return errorSum/length < ERR_SUM_LIM;
        }
        public void rectify()
        {
            for (int line=0;line<length;line++)
            {
                raster.getPixels(0, line, width, 1, buffer);
                int dif = (int) bestFitLine.getY(line);
            }
        }
        public Part[] split()
        {
            Part[] res = new Part[2];
            int half = length/2;
            res[0] = new Part(offset, half);
            res[1] = new Part(offset+half, length-half);
            return res;
        }
        public void newHead(int dif)
        {
            offset += dif;
            length -= dif;
            calc();
        }
        public void newTail(int dif)
        {
            length += dif;
            calc();
        }
        private void addBestFit(int index, int sign)
        {
            if (error[index] < ERR_LIM)
            {
                int weight = sign*(ERR_LIM - error[index]);
                bestFitLine.add(index, dif[index], weight);
                System.err.println(index+" "+dif[index]+" "+weight);
            }
        }
        private void addErrSum(int index, int sign)
        {
            if (error[index] < ERR_LIM)
            {
                int weight = sign*(ERR_LIM - error[index]);
                double y = bestFitLine.getY(index);
                errorSum += Math.abs(y-dif[index])/weight;
            }
        }
        public int getErrorSum()
        {
            return errorSum;
        }

        public int getLength()
        {
            return length;
        }
        
    }
}
