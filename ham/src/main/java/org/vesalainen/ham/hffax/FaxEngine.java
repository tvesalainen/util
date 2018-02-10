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
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxEngine extends JavaLogging implements FaxStateListener
{
    private int lpm;
    private int ioc;
    private int resolution = 2300;
    private BufferedImage image;
    private File faxDir;
    private FaxTokenizer tokenizer;
    private FaxSynchronizer synchronizer;
    private FaxRenderer renderer;
    private ExecutorService executor;

    public FaxEngine(File faxDir, TargetDataLine line)
    {
        this(faxDir, new AudioInputStream(line));
    }

    public FaxEngine(File faxDir, AudioInputStream ais)
    {
        super(FaxEngine.class);
        this.faxDir = faxDir;
        Objects.requireNonNull(ais, "AudioInputStream");
        tokenizer = new FaxTokenizer(ais);
        synchronizer = new BWSynchronizer(this);
        executor = Executors.newCachedThreadPool();
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
            renderer = new FaxRenderer(this, faxDir, filename, resolution, locator);
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
            tokenizer.removeListener(renderer);
            executor.submit(renderer::render);
            renderer = null;
        }
    }
}
