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
package org.vesalainen.ham.oscilloscope;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.riff.RIFFFile;
import org.vesalainen.ham.riff.WaveFile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileSource extends AbstractSource implements Runnable
{
    private Path file;
    private Duration refreshInterval;
    private WaveFile wave;
    private AudioFormat audioFormat;
    private SampleBuffer sampleBuffer;
    private Duration duration;
    private Duration current;
    private boolean stopped;

    public FileSource(Path file, double refreshInterval)
    {
        try
        {
            this.file = file;
            this.refreshInterval = Duration.ofMillis((long) (refreshInterval*1000));
            this.wave = (WaveFile) RIFFFile.open(file);
            this.audioFormat = wave.getAudioFormat();
            this.sampleBuffer = wave.getSampleBuffer((int) (audioFormat.getSampleRate()*refreshInterval));
            this.duration = sampleBuffer.getDuration();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void start()
    {
        if (!stopped)
        {
            current = Duration.ZERO;
            updatePosition();
            fireUpdate(sampleBuffer);
            Thread thread = new Thread(this, file.toString());
            thread.start();
        }
    }

    @Override
    public void forward()
    {
        if (stopped && current.compareTo(duration) < 0)
        {
            current = current.plus(refreshInterval);
            sampleBuffer.goTo(current);
            fireUpdate();
            updatePosition();
        }
    }

    @Override
    public void back()
    {
        if (stopped && current.compareTo(Duration.ZERO) > 0)
        {
            current = current.minus(refreshInterval);
            sampleBuffer.goTo(current);
            fireUpdate();
            updatePosition();
        }
    }

    @Override
    public void pause()
    {
        stopped = true;
    }

    @Override
    public void play()
    {
        if (stopped)
        {
            stopped = false;
            start();
        }
    }

    @Override
    public void stop()
    {
        stopped = true;
    }

    private void updatePosition()
    {
        setText(current.toString());
    }
    @Override
    public void run()
    {
        try
        {
            while (!stopped && current.compareTo(duration) < 0)
            {
                sampleBuffer.goTo(current);
                fireUpdate();
                updatePosition();
                Thread.sleep(refreshInterval.toMillis());
                current = current.plus(refreshInterval);
            }
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString()
    {
        return file.toString();
    }
    
}
