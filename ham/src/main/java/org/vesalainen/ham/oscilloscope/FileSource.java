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
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.riff.RIFFFile;
import org.vesalainen.ham.riff.WaveFile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileSource extends AbstractSource implements Runnable
{
    private Path file;
    private double refreshInterval;
    private boolean stopped;

    public FileSource(Path file, double refreshInterval)
    {
        this.file = file;
        this.refreshInterval = refreshInterval;
    }
    
    @Override
    public void start()
    {
        Thread thread = new Thread(this, file.toString());
        thread.start();
    }

    @Override
    public void run()
    {
        try
        {
            long millis = (long) (refreshInterval*1000);
            Duration intDur = Duration.ofMillis(millis);
            WaveFile wave = (WaveFile) RIFFFile.open(file);
            AudioFormat audioFormat = wave.getAudioFormat();
            int interval = (int) (audioFormat.getSampleRate()*refreshInterval);
            SampleBuffer sampleBuffer = wave.getSampleBuffer(interval);
            Duration duration = sampleBuffer.getDuration();
            fireUpdate(sampleBuffer);
            while (!stopped && intDur.compareTo(duration) < 0)
            {
                sampleBuffer.goTo(intDur);
                fireUpdate();
                Thread.sleep(millis);
                intDur = intDur.plus(intDur);
            }
        }
        catch (IOException | InterruptedException ex)
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
