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
import static java.awt.Color.*;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.fft.FFT;
import org.vesalainen.ham.filter.BooleanConvolution;
import org.vesalainen.ham.hffax.ui.FaxViewer;
import org.vesalainen.ham.hffax.ui.ImageGrid;
import org.vesalainen.ham.riff.RIFFFile;
import org.vesalainen.ham.riff.WaveFile;
import org.vesalainen.util.ArrayGridFiller;
import org.vesalainen.util.BitGrid;

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
    private BufferedImage image;
    private Graphics2D graphics;
    private Rectangle bounds;
    private FaxViewer faxViewer;
    private WritableRaster raster;
    private int n;
    private FFTGrid grid;

    public FaxDecoder(int lpm, int ioc, Path in, Path out) throws IOException
    {
        this.lpm = lpm;
        this.ioc = ioc;
        this.out = out;
        wave = (WaveFile) RIFFFile.open(in);
        data = wave.getData();
        audioFormat = wave.getAudioFormat();
        sampleRate = (int) audioFormat.getSampleRate();
        init(0, 60*sampleRate/lpm);
    }
    private void init(int offset, int samplesPerLine)
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
        grid = new FFTGrid(pixelsPerLine, lineCount, offset);
        image = grid.getImage();
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
        //int offset = startBar();
        //data.position(offset*n*audioFormat.getFrameSize());
        data = data.slice();
        init(0, samplesPerLine);
        faxViewer.repaint();
        findBlackBar();
    }
    private void findBlackBar()
    {
        ArrayGridFiller<Boolean> filler = new ArrayGridFiller<>(grid, ArrayGridFiller::roundedSquare);
        int halfLine = pixelsPerLine/2;
        BitGrid bg = null;
        for (int ll=0;ll<100;ll++)
        {
            bg = filler.fill(0, ll, true);
            Rectangle bounds = bg.patternBounds();
            if (bounds.getWidth() > halfLine)
            {
                break;
            }
            bg = filler.fill(halfLine, ll, true);
            bounds = bg.patternBounds();
            if (bounds.getWidth() > halfLine)
            {
                break;
            }
        }
    }
    private class FFTGrid extends ImageGrid
    {
        private int offset;
        
        public FFTGrid(int width, int height, int offset)
        {
            super(width, height);
            this.offset = offset;
            int count = width*height;
            for (int ii=0;ii<count;ii++)
            {
                buffer.goTo(ii*n+offset);
                fft.forward(buffer, 0);
                double black = fft.getMagnitude(sampleRate, 1500);
                double white = fft.getMagnitude(sampleRate, 2300);
                setColor(ii, black > white);
            }
        }

    }
}
