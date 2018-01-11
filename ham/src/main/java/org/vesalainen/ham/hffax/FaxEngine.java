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
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxEngine implements FaxStateListener
{
    private int lpm;
    private int ioc;
    private int resolution = 1810;
    private BufferedImage image;
    private int line;
    private FaxTokenizer tokenizer;
    private FaxSynchronizer synchronizer;
    private FaxRenderer renderer;
    private SignalDetector signalDetector;

    public FaxEngine(TargetDataLine line)
    {
        this(new AudioInputStream(line));
    }

    public FaxEngine(AudioInputStream ais)
    {
        Objects.requireNonNull(ais, "AudioInputStream");
        tokenizer = new FaxTokenizer(ais);
        synchronizer = new FaxSynchronizer(this);
        signalDetector = new SignalDetector(this, tokenizer.getSize(), tokenizer.getSampleRate(), 1500, 2300);
    }
    public void parse() throws IOException
    {
        try
        {
            tokenizer.addListener(synchronizer);
            //tokenizer.addDataListener(signalDetector);
            tokenizer.run();
        }
        catch (EOFException ex)
        {
            stop();
        }
    }
    @Override
    public void start(long startOfLine, long lineLength)
    {
        System.err.println("START");
        if (renderer == null)
        {
            ZonedDateTime now = ZonedDateTime.now();
            String str = now.format(DateTimeFormatter.ISO_INSTANT).replace(":", "");
            renderer = new FaxRenderer("fax"+str, 1810, startOfLine, lineLength);
            tokenizer.addListener(renderer);
        }
        else
        {
            stop();
        }
    }

    @Override
    public synchronized void stop()
    {
        System.err.println("STOP");
        if (renderer != null)
        {
            try
            {
                tokenizer.removeListener(renderer);
                renderer.render();
                renderer = null;
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
}
