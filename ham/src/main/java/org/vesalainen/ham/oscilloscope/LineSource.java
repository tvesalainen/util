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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import static javax.sound.sampled.FloatControl.Type.MASTER_GAIN;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.SampleBufferImpl;
import org.vesalainen.ham.AGC;
import org.vesalainen.nmea.icommanager.IcomManager;

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
    private String agcPort;
    private AGC agc;
    private IcomManager manager;

    public LineSource(AudioFormat audioFormat, Mixer.Info mixerInfo, double refreshInterval, String agcPort)
    {
        this.audioFormat = audioFormat;
        this.mixerInfo = mixerInfo;
        this.refreshInterval = refreshInterval;
        this.agcPort = agcPort;
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
            createAGC();
            int size = (int) (audioFormat.getSampleRate()*refreshInterval);
            byte[] buffer = new byte[size*audioFormat.getFrameSize()];
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            SampleBufferImpl sampleBuffer = new SampleBufferImpl(audioFormat, bb, size);
            line.open(audioFormat, size);
            line.start();
            fireUpdate(sampleBuffer);
            while (!stopped)
            {
                int rc = line.read(buffer, 0, size);
                fireUpdate();
                updateAGC(sampleBuffer);
            }
        }
        catch (LineUnavailableException | IOException | InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            closeAGC();
        }
    }

    private void createAGC() throws IOException, InterruptedException
    {
        if (!agcPort.startsWith("--"))
        {
            manager = IcomManager.getInstance(0, agcPort);
            agc = new AGC(manager, 0.1, 0.02);
        }
    }
    private void updateAGC(SampleBufferImpl sampleBuffer) throws IOException, InterruptedException
    {
        if (agc != null)
        {
            agc.update(sampleBuffer.getArray());
            setText(agc.toString());
        }
    }
    private void closeAGC()
    {
        if (agc != null)
        {
            try {
                manager.close();
                manager = null;
                agc = null;
            }
            catch (IOException | InterruptedException ex) 
            {
            }
        }
    }
    @Override
    public String toString()
    {
        return mixerInfo+" "+audioFormat;
    }
    
}
