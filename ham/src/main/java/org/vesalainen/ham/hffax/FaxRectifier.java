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
import static org.vesalainen.ham.hffax.Fax.isAbout;
import org.vesalainen.math.BestFitLine;
import org.vesalainen.math.SimpleAverage;
import org.vesalainen.math.Statistics;
import org.vesalainen.math.XYSamples;
import org.vesalainen.util.Lists;
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
    private double averageSlope;

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
        noiseOptimizer();
        for (Part part : parts)
        {
            finest("solved: %s", part);
            part.rectify();
        }
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
            if (isAbout(lineEndLen, length))
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
        int start = 0;
        while (true)
        {
            Part part = new Part(start);
            parts.add(part);
            int begin = scanStart(part, line, scanner);
            if (begin == -1)
            {
                part.setLength(height-start);
                part.calc();
                return;
            }
            beginLengthOptimizer.setExpectedBegin(scanner.getBegin());
            int body = scanBody(part, begin+BAND, beginScanner, beginLengthOptimizer);
            if (body == -1)
            {
                part.setLength(height-start);
                part.calc();
                return;
            }
            int end = scanEnd(part, body, beginScanner, beginLengthOptimizer);
            if (end == -1)
            {
                part.setLength(height-start);
                part.calc();
                return;
            }
            part.setLength(end-start);
            part.calc();
            finest("raw: %s", part);
            line = end;
            start = end;
        }
    }
    private void noiseOptimizer()
    {
        checkIntegrity(parts);
        SimpleAverage ave = new SimpleAverage();
        for (Part part : parts)
        {
            double slope = part.bestFitLine.getSlope();
            if (Double.isFinite(slope))
            {
                ave.add(slope, part.length);
            }
        }
        averageSlope = ave.average();
        List<Part> list = new ArrayList<>();
        List<Part> noisy = null;
        Part prev = null;
        for (Part part : parts)
        {
            if (part.isNoisy(averageSlope))
            {
                finest("noicy: %s", part);
                if (noisy == null)
                {
                    noisy = new ArrayList<>();
                    if (prev != null)
                    {
                        noisy.add(prev);
                    }
                }
                prev = null;
                noisy.add(part);
            }
            else
            {
                finest("ok: %s", part);
                if (noisy == null)
                {
                    if (prev != null)
                    {
                        list.add(prev);
                    }
                    prev = part;
                }
                else
                {
                    noisy.add(part);
                    List<Part> solved = solveNoise(noisy);
                    list.addAll(solved.subList(0, solved.size()-1));
                    prev = solved.get(solved.size()-1);
                    noisy = null;
                }
            }
        }
        if (noisy != null)
        {
            list.addAll(solveNoise(noisy));
        }
        else
        {
            list.add(prev);
        }
        parts = list;
        checkIntegrity(parts);
    }
    private List<Part> solveNoise(List<Part> noise)
    {
        Part head = null;
        Part tail = null;
        int start = 0;
        int end = 0;
        if (noise.get(0).isNoisy(averageSlope))
        {
            head = tail = noise.get(noise.size()-1);
            start = 0;
            end = noise.size();
        }
        else
        {
            if (noise.get(noise.size()-1).isNoisy(averageSlope))
            {
                tail = head = noise.get(0);
                start = 1;
                end = noise.size()-1;
            }
            else
            {
                head = noise.get(0);
                tail = noise.get(noise.size()-1);
                start = 1;
                end = noise.size();
            }
        }
        double minCost = Double.POSITIVE_INFINITY;
        int minIndex = -1;
        for (int sp=start;sp<end;sp++)
        {
            double headCost = 0;
            double tailCost = 0;
            for (int ii=1;ii<sp;ii++)
            {
                Part part = noise.get(ii);
                headCost += Statistics.rootMeanSquareError(part.samples, head.bestFitLine);
            }
            for (int ii=sp;ii<noise.size()-1;ii++)
            {
                Part part = noise.get(ii);
                tailCost += Statistics.rootMeanSquareError(part.samples, tail.bestFitLine);
            }
            double cost = headCost+tailCost;
            if (cost < minCost)
            {
                minCost = cost;
                minIndex = sp;
            }
        }
        finest("solved: %s", noise);
        if (minCost < 2*(noise.size()-2)*Math.max(head.rootMeanSquareError, tail.rootMeanSquareError))
        {
            Part newHead = new Part(noise.subList(0, minIndex));
            finest("head: %s", newHead);
            Part newTail = new Part(noise.subList(minIndex, noise.size()));
            finest("tail: %s", newTail);
            return Lists.create(newHead, newTail);
        }
        else
        {
            Part hea = noise.get(0);
            Part tai = noise.get(noise.size()-1);
            int wholeLen = tai.offset+tail.length-hea.offset;
            Part he = noise.get(minIndex-1);
            int len1 = he.offset+he.length-hea.offset;
            hea.setLength(len1);
            Part ts = noise.get(minIndex);
            tai.setOffset(hea.offset+len1);
            tai.setLength(wholeLen-hea.length);
            return Lists.create(hea, tai);
        }
    }
    private void checkIntegrity(List<Part> list)
    {
        Part prev = null;
        for (Part part : list)
        {
            if (prev != null)
            {
                if (prev.offset+prev.length != part.offset)
                {
                    throw new IllegalArgumentException(prev+" clash with "+part);
                }
            }
            prev = part;
        }
    }
    private static final int ERR_PERCENT = 15;
    /**
     * 
     * @param part
     * @param line
     * @param scanner
     * @return Start line of block
     */
    private int scanStart(Part part, int line, BarScanner scanner)
    {
        while (line < height-BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            int negativeLength = scanner.getNegativeLength();
            if (
                    isAbout(lineEndLen, length, ERR_PERCENT) &&
                    negativeLength > 30
                    )
            {
                part.add(scanner, line, BAND);
                return line;
            }
            else
            {
                line++;
            }
        }
        return -1;
    }
    /**
     * 
     * @param part
     * @param line
     * @param scanner
     * @param beginLengthOptimizer
     * @return last first line of failing block
     */
    private int scanBody(Part part, int line, BarScanner scanner, BarScannerBeginLengthOptimizer beginLengthOptimizer)
    {
        while (line < height-BAND)
        {
            raster.getPixels(0, line, width, BAND, buffer);
            scanner.maxBar(buffer, 0, BAND);
            int length = scanner.getLength();
            int begin = scanner.getBegin();
            int negativeLength = scanner.getNegativeLength();
            if (
                    isAbout(lineEndLen, length, ERR_PERCENT) &&
                    isAbout(beginLengthOptimizer.getExpectedBegin(), begin, ERR_PERCENT) &&
                    negativeLength > 30
                    )
            {
                if (isAbout(lineEndLen, length, 3))
                {
                    beginLengthOptimizer.setExpectedBegin(begin);
                }
                part.add(scanner, line, BAND);
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
            int negativeLength = scanner.getNegativeLength();
            if (
                    isAbout(lineEndLen, length, ERR_PERCENT) &&
                    isAbout(beginLengthOptimizer.getExpectedBegin(), begin, ERR_PERCENT) &&
                    negativeLength > 30
                    )
            {
                part.add(scanner, line-ii, ii, BAND);
                return line-ii+BAND;
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
            if (isAbout(topBlackInPixels, length))
            {
                topBlockHeight = line+BAND-1;
                line += BAND;
            }
            else
            {
                if (topBlockHeight > 0)
                {
                    for (int ii=0;ii<BAND;ii++)
                    {
                        line--;
                        raster.getPixels(0, line, width, BAND, buffer);
                        scanner.maxBar(buffer, 0, BAND);
                        length = scanner.getLength();
                        if (isAbout(topBlackInPixels, length))
                        {
                            topBlockHeight = line+BAND-1;
                            return;
                        }
                    }
                    return;
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
        private XYSamples samples;
        private BestFitLine bestFitLine;
        private double rootMeanSquareError;

        public Part()
        {
            samples = new XYSamples();
        }

        public Part(List<Part> list)
        {
            this.offset = list.get(0).offset;
            samples = new XYSamples();
            for (Part part : list)
            {
                this.length += part.length;
                this.samples.add(part.samples);
            }
            calc();
        }
        public Part(int offset)
        {
            this.offset = offset;
            samples = new XYSamples();
        }
        public void calc()
        {
            bestFitLine = new BestFitLine(samples);
            rootMeanSquareError = Statistics.rootMeanSquareError(samples, bestFitLine);
        }
        public void add(BarScanner scanner, int line, int lineCount)
        {
            add(scanner, line, 0, lineCount);
        }
        public void add(BarScanner scanner, int line, int offset, int lineCount)
        {
            for (int ii=offset;ii<lineCount;ii++)
            {
                samples.add(line+ii, scanner.getBegin(ii)+scanner.getLength(ii));
            }
        }
        public void rectify()
        {
            for (int ii=0;ii<length;ii++)
            {
                int line = offset+ii;
                raster.getPixels(0, line, width, 1, buffer);
                int dif = (int) bestFitLine.getY(line)%width;
                if (dif > 0)
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
        public boolean isNoisy(double averageSlope)
        {
            return length > 3*samples.getCount() || 
                    samples.getCount() < 10 ||
                    !isAbout(averageSlope, bestFitLine.getSlope(), 40);
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
            return "Part{" + "o=" + offset + ", l=" + length + ", bfl=" + bestFitLine + ", rmse=" + rootMeanSquareError + '}';
        }

    }
}
