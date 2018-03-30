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
package org.vesalainen.ham;

import java.io.IOException;
import java.nio.file.Path;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.fft.FilterAudioInputStream;
import org.vesalainen.ham.riff.WaveFile;
import org.vesalainen.nio.IntArray;
import org.vesalainen.util.Listener;
import org.vesalainen.util.ListenerSupport;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AudioRecorder extends JavaLogging implements AutoCloseable
{
    private TargetDataLine targetDataLine;
    protected ListenerSupport<IntArray> listeners = new ListenerSupport<>();
    private final AudioFormat audioFormat;
    private final Mixer.Info mixerInfo;
    
    public AudioRecorder(String mixerName) throws LineUnavailableException
    {
        this(mixerName, 41000, 16);
    }
    public AudioRecorder(String mixerName, float sampleRate, int sampleSizeInBits) throws LineUnavailableException
    {
        super(AudioRecorder.class);
        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, 1, true, false);
        config("AudioRecorder(%s)", audioFormat);
        mixerInfo = mixer(mixerName, audioFormat);
        config("using mixer %s", mixerInfo);
    }
    public void record(Path file) throws IOException, LineUnavailableException
    {
        fine("start record(%s)", file);
        if (mixerInfo != null)
        {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat, mixerInfo);
        }
        else
        {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        }
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        try (AudioInputStream in = new AudioInputStream(targetDataLine);
                FilterAudioInputStream fais = new FilterAudioInputStream(in, 4096))
        {
            fais.addListeners(listeners.getListeners());
            WaveFile wave = new WaveFile();
            wave.store(fais, file);
        }
        fine("stopped recording");
    }
    private Mixer.Info mixer(String name, AudioFormat audioFormat)
    {
        if (name != null)
        {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
            {
                try (Mixer mixer = AudioSystem.getMixer(mixerInfo))
                {
                    int maxLines = mixer.getMaxLines(info);
                    if (maxLines != 0 && mixerInfo.toString().startsWith(name))
                    {
                        return mixerInfo;
                    }
                }
            }
        }
        return null;
    }
    @Override
    public void close()
    {
        targetDataLine.close();
    }

    public void addListener(Listener<IntArray> listener)
    {
        listeners.addListener(listener);
    }

    public void removeListener(Listener<IntArray> listener)
    {
        listeners.removeListener(listener);
    }
    
}
