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
import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.vesalainen.ham.riff.RIFFFile;
import org.vesalainen.ham.riff.WaveFile;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxDecoder extends JavaLogging implements FaxStateListener
{
    private int lpm;
    private int ioc;
    private Path in;
    private Path out;
    private AudioInputStream ais;
    private int resolution = 2300;
    private BufferedImage image;
    private FaxTokenizer tokenizer;
    private FaxSynchronizer synchronizer;
    private FaxRenderer renderer;

    public FaxDecoder(int lpm, int ioc, Path in, Path out) throws MalformedURLException
    {
        super(FaxDecoder.class);
        try
        {
            this.lpm = lpm;
            this.ioc = ioc;
            this.in = in;
            this.out = out;
            WaveFile wave = (WaveFile) RIFFFile.open(in);
            this.ais = wave.getAudioInputStream();
            this.tokenizer = new FaxTokenizer(ais);
            this.synchronizer = new BWSynchronizer(this);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public void parse() throws IOException
    {
        try
        {
            tokenizer.addFrequencyListener(synchronizer);
            tokenizer.addListener(synchronizer);
            //tokenizer.addDataListener(signalDetector);
            tokenizer.run();
        }
        catch (EOFException ex)
        {
            stop("eof");
        }
    }
    @Override
    public void start(PageLocator locator)
    {
        config("start called");
        if (renderer == null)
        {
            ZonedDateTime now = ZonedDateTime.now();
            String str = now.format(DateTimeFormatter.ISO_INSTANT).replace(":", "");
            String filename = "fax"+str;
            config("start rendering of %s", filename);
            renderer = new FaxRenderer(this, out, resolution, locator);
            tokenizer.addListener(renderer);
        }
        else
        {
            config("stop called from start");
            stop("start");
        }
    }

    @Override
    public synchronized void stop(String reason)
    {
        config("stop from %s", reason);
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
