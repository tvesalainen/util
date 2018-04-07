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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import javax.sound.sampled.AudioFormat;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.fft.FFT;
import org.vesalainen.ham.filter.BooleanConvolution;
import org.vesalainen.ham.hffax.ui.FaxViewer;
import org.vesalainen.ham.riff.RIFFFile;
import org.vesalainen.ham.riff.WaveFile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxDecoder
{
    private static final double RATIO = 2182.0/2300.0;
    private final int lpm;
    private final int ioc;
    private final Path out;
    private final int sampleRate;
    private SampleBuffer buffer;
    private FFT fft;
    private int samplesPerLine;
    private int pixelsPerLine;
    private final WaveFile wave;
    private ByteBuffer data;
    private final AudioFormat audioFormat;
    private int lineCount;
    private float[] grid;
    private BufferedImage image;
    private Graphics2D graphics;
    private Rectangle bounds;
    private FaxViewer faxViewer;
    private WritableRaster raster;
    private BitSet visited;
    private int[] stack;
    private int stackPtr;
    private int gridSize;
    private int n;

    public FaxDecoder(int lpm, int ioc, Path in, Path out) throws IOException
    {
        this.lpm = lpm;
        this.ioc = ioc;
        this.out = out;
        wave = (WaveFile) RIFFFile.open(in);
        data = wave.getData();
        audioFormat = wave.getAudioFormat();
        sampleRate = (int) audioFormat.getSampleRate();
        init(60*sampleRate/lpm);
    }
    private void init(int samplesPerLine)
    {
        this.samplesPerLine = samplesPerLine;
        n = 1;
        while (sampleRate/n > 800)
        {
            n <<=1;
        }
        this.pixelsPerLine = samplesPerLine/n;
        this.fft = new FFT(n);
        int view = samplesPerLine/pixelsPerLine;
        this.buffer = new SampleBufferImpl(audioFormat, data, view);
        this.lineCount = buffer.getSampleCount()/samplesPerLine;
        gridSize = pixelsPerLine*lineCount;
        if (grid == null || grid.length < gridSize)
        {
            this.grid = new float[gridSize];
        }
        Arrays.fill(grid, Float.NaN);
        visited = new BitSet();
        stack = new int[gridSize];
        image = new BufferedImage(pixelsPerLine, lineCount, TYPE_BYTE_BINARY);
        graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.setColor(Color.BLACK);
        bounds = graphics.getDeviceConfiguration().getBounds();
        graphics.clearRect(0, 0, bounds.width, bounds.height);
        raster = image.getRaster();
        if (faxViewer == null)
        {
            faxViewer = new FaxViewer(image);
        }
        else
        {
            faxViewer.setImage(image);
        }
    }
    void parse()
    {
        int offset = startBar();
        data.position(offset*n*audioFormat.getFrameSize());
        data = data.slice();
        init(samplesPerLine);
        //int count = fill(pixelsPerLine/2, 5, Color.BLACK);
        render();
        faxViewer.repaint();
    }
    private int startBar()
    {
        boolean[] coef = new boolean[pixelsPerLine];
        int lim = (int) Math.round((1.0-RATIO)*pixelsPerLine);
        for (int ii=lim;ii<pixelsPerLine;ii++)
        {
            coef[ii] = true;
        }
        BooleanConvolution conv = new BooleanConvolution(coef);
        for (int ii=0;ii<pixelsPerLine;ii++)
        {
            int y = line(ii);
            int x = column(ii);
            boolean b = isBlack(ii);
            conv.conv(b);
        }
        int minErrors = pixelsPerLine;
        int pos = 0;
        for (int ii=pixelsPerLine;ii<100*pixelsPerLine;ii++)
        {
            int y = line(ii);
            int x = column(ii);
            boolean b = isBlack(ii);
            int errors = conv.conv(b);
            if (errors < minErrors)
            {
                minErrors = errors;
                pos = ii;
            }
        }
        return pos;
    }
    private int fill(int initX, int initY, Color color)
    {
        push(initX, initY);
        while (true)
        {
            int count = 0;
            int position = pop();
            if (position == -1)
            {
                return count;
            }
            visited.set(position);
            Color c = color(position);
            if (c.equals(color))
            {
                int x = column(position);
                int y = line(position);
                System.err.println(x+", "+y);
                raster.setSample(x, y, 0, 0);
                faxViewer.repaint();
                count++;
                push(x, y+1);
                push(x-1, y+1);
                push(x-1, y);
                push(x-1, y-1);
                push(x, y-1);
                push(x+1, y-1);
                push(x+1, y);
                push(x+1, y+1);
            }
        }
    }
    private void render()
    {
        for (int ii=0;ii<100000;ii++)
        {
            int y = line(ii);
            int x = column(ii);
            if (isBlack(ii))
            {
                raster.setSample(x, y, 0, 0);
            }
        }
    }
    private int line(int position)
    {
        return position/pixelsPerLine;
    }
    private int column(int position)
    {
        return position%pixelsPerLine;
    }
    private int position(int x, int y)
    {
        return y*pixelsPerLine+x;
    }
    private boolean isBlack(int position)
    {
        float ratio = ratio(position);
        if (ratio < 0.5)
        {
            return false;
        }
        else
        {
            int x = column(position);
            int y = line(position);
            raster.setSample(x, y, 0, 0);
            return true;
        }
    }
    private Color color(int x, int y)
    {
        return color(position(x, y));
    }
    private Color color(int position)
    {
        float ratio = ratio(position);
        if (ratio < 0.5)
        {
            return Color.WHITE;
        }
        else
        {
            return Color.BLACK;
        }
    }
    private float ratio(int x, int y)
    {
        return ratio(position(x, y));
    }
    private float ratio(int position)
    {
        if (Float.isNaN(grid[position]))
        {
            buffer.goTo(position*n);
            fft.forward(buffer, 0);
            double black = fft.getMagnitude(sampleRate, 1500);
            double white = fft.getMagnitude(sampleRate, 2300);
            grid[position] = (float) (black/white);
        }
        return grid[position];
    }
    private void push(int x, int y)
    {
        int position = position(x, y);
        if (position < 0 || position >= gridSize || visited.get(position))
        {
            return;
        }
         for (int ii=0;ii<stackPtr;ii++)
        {
            if (stack[ii] == position)
            {
                return;
            }
        }
        stack[stackPtr++] = position;
    }
    private int pop()
    {
        if (stackPtr > 0)
        {
            return stack[--stackPtr];
        }
        return -1;
    }
}
