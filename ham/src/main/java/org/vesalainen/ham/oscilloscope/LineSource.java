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

import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.SampleBuffer;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineSource extends AbstractSource implements Runnable
{
    private AudioFormat audioFormat;
    private Mixer.Info mixerInfo;
    private double refreshInterval;
    private volatile boolean stopped;

    public LineSource(AudioFormat audioFormat, Mixer.Info mixerInfo, double refreshInterval)
    {
        this.audioFormat = audioFormat;
        this.mixerInfo = mixerInfo;
        this.refreshInterval = refreshInterval;
    }

    @Override
    public void start()
    {
        Thread thread = new Thread(this, mixerInfo.getDescription());
        thread.start();
    }

    @Override
    public void stop()
    {
        stopped = true;
        super.stop();
    }

    @Override
    public void run()
    {
        try (TargetDataLine line = AudioSystem.getTargetDataLine(audioFormat, mixerInfo))
        {
            int size = (int) (audioFormat.getSampleRate()*refreshInterval);
            byte[] buffer = new byte[size*audioFormat.getFrameSize()];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            SampleBuffer sampleBuffer = new SampleBufferImpl(audioFormat, bb, size);
            line.open(audioFormat, size);
            line.start();
            fireUpdate(sampleBuffer);
            while (!stopped)
            {
                int rc = line.read(buffer, 0, size);
                fireUpdate();
            }
        }
        catch (LineUnavailableException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString()
    {
        return mixerInfo+" "+audioFormat;
    }
    
}
