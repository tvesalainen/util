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
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.vesalainen.math.BestFitLine;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxRectifier extends JavaLogging
{
    private static final int ERR_LIM = 10;
    private static final double ERR_SUM_LIM = 1;
    private static final int SPLIT_LIM = 10;
    private static final int BAND = 4;
    private WritableRaster raster;
    private int width;
    private int height;
    private int[] buffer;
    private int[] pattern;
    private int whiteLen;
    private int scanLength;
    private int patternLen;
    private int[] error;
    private int[] dif;
    private int[] tmp;
    private int topBlockHeight;
    private int lineEndLen;

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
        super(FaxRectifier.class);
        this.raster = raster;
        width = raster.getWidth();
        height = raster.getHeight();
        buffer = new int[width*BAND];
        tmp = new int[width];
        error = new int[height];
        dif = new int[height];
    }
    public void rectify()
    {
        findTopBlock();
        fine("top block height is %s", topBlockHeight);
        findLineEndBlocksLength();
        findLineEndBlocks();
        //findBands();
        Part part = new Part(0, height);
        split(part);
    }
    private void findLineEndBlocksLength()
    {
        BarScannerOptimizer barScannerOptimizer = new BarScannerOptimizer(55, Fax.topBlackInPixels(width));
        BarScanner scanner = new BarScanner(width, 1, barScannerOptimizer);
        int maxLen = Fax.topWhiteInPixels(width);
        int minLen = Fax.inPixels(width, 30);
        int[] count = new int[maxLen];
        for (int line = topBlockHeight+1;line < height-BAND;line += BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            barScannerOptimizer.reset();
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            int end = begin+length;
            if (length < maxLen && length > minLen)
            {
                count[length]++;
            }
        }
        int max = 0;
        for (int ii=minLen;ii<maxLen;ii++)
        {
            if (count[ii] > max)
            {
                max = count[ii];
                lineEndLen = ii;
            }
        }
    }
    private void findLineEndBlocks()
    {
        BarScannerOptimizer barScannerOptimizer = new BarScannerOptimizer(lineEndLen, width-lineEndLen);
        BarScanner scanner = new BarScanner(width, 0, barScannerOptimizer);
        Arrays.fill(error, Integer.MAX_VALUE);
        int maxLen = Fax.topWhiteInPixels(width);
        int minLen = Fax.inPixels(width, 30);
        for (int line = topBlockHeight+1;line < height-BAND;line++)
        {
            raster.getPixels(0, line, width, 1, buffer);
            scanner.maxBar(buffer, 0, 1);
            barScannerOptimizer.reset();
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            int end = begin+length;
            if (length < maxLen && length > minLen)
            {
                int err = Math.abs(lineEndLen-length);
                error[line] = err;
                dif[line] = width < end ? end-width : end;
                if (err == 0)
                {
                    barScannerOptimizer.setExpectedBegin(begin);
                }
            }
        }
    }
    private void findTopBlock()
    {
        BarScanner scanner = new BarScanner(width, 1);
        int topBlackInPixels = Fax.topBlackInPixels(width);
        int line = 0;
        while (line < 100)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            if (Fax.isAbout(topBlackInPixels, length))
            {
                topBlockHeight = line+BAND-1;
                line += BAND;
            }
            else
            {
                if (line > BAND)
                {
                    for (int ii=0;ii<BAND;ii++)
                    {
                        line--;
                        raster.getPixels(0, line, width, BAND, buffer);
                        scanner.maxBar(buffer, 0, BAND);
                        length = scanner.getLength();
                        if (Fax.isAbout(topBlackInPixels, length))
                        {
                            topBlockHeight = line+BAND-1;
                            return;
                        }
                    }
                }
                else
                {
                    line++;
                }
            }
        }
    }
    private void findBands()
    {
        int minErr;
        int end = -1;
        Arrays.fill(error, 0, topBlockHeight+1, Integer.MAX_VALUE);
        for (int  line=topBlockHeight+1;line<height;line++)
        {
            raster.getPixels(0, line, width, 1, buffer);
            minErr = Integer.MAX_VALUE;
            for (int col=0;col<scanLength;col++)
            {
                int err = 0;
                for (int pat=0;pat<patternLen&err<minErr;pat++)
                {
                    if (buffer[(col+pat)%width] != pattern[pat])
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
            dif[line] = width < end ? end-width : end;
        }
    }
    private void split(Part part)
    {
        if (part.getLength() > SPLIT_LIM)
        {
            Part[] parts = part.split();
            if ((parts[0].errorSum + parts[1].errorSum) < part.errorSum)
            {
                steal(parts);
                split(parts[0]);
                split(parts[1]);
            }
            else
            {
                processParts(part);
            }
        }
        else
        {
            processParts(part);
        }
    }
    private void processParts(Part part)
    {
        if (part.isOk())
        {
            part.rectify();
        }
        else
        {
            if (part.getLength() > SPLIT_LIM)
            {
                Part[] parts = part.split();
                steal(parts);
                processParts(parts[0]);
                processParts(parts[1]);
            }
        }
    }
    private void steal(Part[] parts)
    {
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
    }
    private void steal(Part to, Part from)
    {
        boolean after = from.isAfter(to);
        while (to.isOk() && !from.isOk())
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
        if (!from.isOk())
        {
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
            return (double)errorSum/(double)length < ERR_SUM_LIM;
        }
        public void rectify()
        {
            for (int ii=0;ii<length;ii++)
            {
                int line = offset+ii;
                raster.getPixels(0, line, width, 1, buffer);
                int dif = (int) bestFitLine.getY(line);
                if (dif > 0 && dif < width)
                {
                    try
                    {
                        System.arraycopy(buffer, dif, tmp, 0, width - dif);
                        System.arraycopy(buffer, 0, buffer, width - dif, dif);
                        System.arraycopy(tmp, 0, buffer, 0, width - dif);
                        raster.setPixels(0, line, width, 1, buffer);
                    }
                    catch (ArrayIndexOutOfBoundsException ex)
                    {
                        ex.printStackTrace();
                    }
                }
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

        @Override
        public String toString()
        {
            return "Part{" + "offset=" + offset + ", length=" + length + ", errorSum=" + errorSum + '}';
        }
        
    }
}
