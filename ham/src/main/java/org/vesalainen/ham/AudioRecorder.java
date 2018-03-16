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
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AudioRecorder extends JavaLogging implements AutoCloseable
{
    private TargetDataLine targetDataLine;
    
    public AudioRecorder(String mixerName) throws LineUnavailableException
    {
        this(mixerName, 44000, 16);
    }
    public AudioRecorder(String mixerName, float sampleRate, int sampleSizeInBits) throws LineUnavailableException
    {
        super(AudioRecorder.class);
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, 1, true, false);
        config("AudioRecorder(%s)", audioFormat);
        Mixer.Info mixerInfo = mixer(mixerName, audioFormat);
        config("using mixer %s", mixerInfo);
        if (mixerInfo != null)
        {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat, mixerInfo);
        }
        else
        {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        }
        targetDataLine.open(audioFormat);
    }
    public void record(Path file) throws IOException
    {
        record(file, Type.WAVE);
    }
    public void record(Path file, Type type) throws IOException
    {
        fine("start record(%s, %s)", file, type);
        targetDataLine.stop();
        targetDataLine.start();
        try (AudioInputStream in = new AudioInputStream(targetDataLine))
        {
            AudioSystem.write(in, type, file.toFile());
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
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                int maxLines = mixer.getMaxLines(info);
                if (maxLines != 0 && mixerInfo.toString().startsWith(name))
                {
                    return mixerInfo;
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
    
}
