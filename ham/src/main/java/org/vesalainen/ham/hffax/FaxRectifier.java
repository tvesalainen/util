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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private int scanLength;
    private int patternLen;
    private int[] error;
    private int[] dif;
    private int[] tmp;
    private int topBlockHeight;
    private int lineEndLen;
    private List<Part> parts = new ArrayList<>();

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
        scanParts();
        //findLineEndBlocks();
        //findBands();
    }
    private void findLineEndBlocksLength()
    {
        BarScannerBeginLengthOptimizer barScannerOptimizer = new BarScannerBeginLengthOptimizer(55, Fax.topBlackInPixels(width));
        BarScanner scanner = new BarScanner(width, 1, barScannerOptimizer);
        int maxLen = Fax.topWhiteInPixels(width);
        int minLen = Fax.inPixels(width, 30);
        int[] count = new int[maxLen];
        for (int line = topBlockHeight+1;line < height-BAND;line += BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
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
        BarScannerLengthOptimizer barScannerOptimizer = new BarScannerLengthOptimizer(lineEndLen);
        BarScanner scanner = new BarScanner(width, 0, barScannerOptimizer);
        Arrays.fill(error, Integer.MAX_VALUE);
        int line = topBlockHeight+1;
        boolean lastHit = false;
        while (line < height-BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            int end = begin+length;
            if (Fax.isAbout(lineEndLen, length))
            {
                int err = Math.abs(lineEndLen-length);
                error[line] = err;
                dif[line] = width < end ? end-width : end;
                line += BAND;
                lastHit = true;
            }
            else
            {
                if (!lastHit)
                {
                    line++;
                }
                else
                {
                    line--;
                }
                lastHit = false;
            }
        }
    }
    private void scanParts()
    {
        BarScannerLengthOptimizer lengthOptimizer = new BarScannerLengthOptimizer(lineEndLen);
        BarScanner scanner = new BarScanner(width, 0, lengthOptimizer);
        BarScannerBeginLengthOptimizer beginLengthOptimizer = new BarScannerBeginLengthOptimizer(lineEndLen, width-lineEndLen);
        BarScanner beginScanner = new BarScanner(width, 0, beginLengthOptimizer);
        int line = topBlockHeight+1;
        while (true)
        {
            Part part = new Part();
            int begin = scanStart(part, line, scanner);
            if (begin == -1)
            {
                return;
            }
            part.setOffset(begin);
            beginLengthOptimizer.setExpectedBegin(scanner.getBegin());
            int body = scanBody(part, begin+BAND, beginScanner, beginLengthOptimizer);
            if (body == -1)
            {
                return;
            }
            int end = scanEnd(part, body, beginScanner, beginLengthOptimizer);
            if (end == -1)
            {
                return;
            }
            part.setLength(end-begin);
            parts.add(part);
            line = end+1;
        }
    }
    private static final int ERR_PERCENT = 15;
    private int scanStart(Part part, int line, BarScanner scanner)
    {
        while (line < height-BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            if (Fax.isAbout(lineEndLen, length, ERR_PERCENT))
            {
                part.add(line+BAND/2, begin+length, BAND);
                return line;
            }
            else
            {
                line++;
            }
        }
        return -1;
    }
    private int scanBody(Part part, int line, BarScanner scanner, BarScannerBeginLengthOptimizer beginLengthOptimizer)
    {
        Arrays.fill(error, Integer.MAX_VALUE);
        while (line < height-BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            if (
                    Fax.isAbout(lineEndLen, length, ERR_PERCENT) &&
                    Fax.isAbout(beginLengthOptimizer.getExpectedBegin(), begin, ERR_PERCENT)
                    )
            {
                int err = Math.abs(lineEndLen-length);
                if (err == 0)
                {
                    beginLengthOptimizer.setExpectedBegin(begin);
                }
                part.add(line+BAND/2, begin+length, BAND);
                line += BAND;
            }
            else
            {
                return line;
            }
        }
        return -1;
    }
    private int scanEnd(Part part, int line, BarScanner scanner, BarScannerBeginLengthOptimizer beginLengthOptimizer)
    {
        for (int ii=1;ii<BAND;ii++)
        {
            raster.getPixels(0, line-ii, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            if (
                    Fax.isAbout(lineEndLen, length, ERR_PERCENT) &&
                    Fax.isAbout(beginLengthOptimizer.getExpectedBegin(), begin, ERR_PERCENT)
                    )
            {
                part.add(line+(BAND-ii)/2, begin+length, BAND-ii);
                return line;
            }
        }
        return line;
    }
    private void findLineEndBlocksLineByLine()
    {
        BarScannerBeginLengthOptimizer barScannerOptimizer = new BarScannerBeginLengthOptimizer(lineEndLen, width-lineEndLen);
        BarScanner scanner = new BarScanner(width, 0, barScannerOptimizer);
        Arrays.fill(error, Integer.MAX_VALUE);
        int maxLen = Fax.topWhiteInPixels(width);
        int minLen = Fax.inPixels(width, 30);
        for (int line = topBlockHeight+1;line < height-BAND;line++)
        {
            raster.getPixels(0, line, width, 1, buffer);
            scanner.maxBar(buffer, 0, 1);
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
    private class Part
    {
        private int offset;
        private int length;
        private final BestFitLine bestFitLine;

        public Part()
        {
            bestFitLine = new BestFitLine();
        }
        public void add(int line, int lineEnd, int count)
        {
            bestFitLine.add(line, lineEnd, count);
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

        public int getOffset()
        {
            return offset;
        }

        public void setOffset(int offset)
        {
            this.offset = offset;
        }

        public int getLength()
        {
            return length;
        }

        public void setLength(int length)
        {
            this.length = length;
        }
        @Override
        public String toString()
        {
            return "Part{" + "offset=" + offset + ", length=" + length + '}';
        }
        
    }
}
