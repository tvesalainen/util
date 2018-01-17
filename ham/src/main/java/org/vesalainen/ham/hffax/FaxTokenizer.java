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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.AudioReader;
import org.vesalainen.ham.DataListener;
import static org.vesalainen.ham.hffax.FaxTone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxTokenizer
{

    private AudioReader reader;
    private FaxTone last = UNKNOWN;
    private List<FaxListener> listeners = new CopyOnWriteArrayList<>();
    private List<FrequencyListener> frequencyListeners = new CopyOnWriteArrayList<>();
    
    public FaxTokenizer(TargetDataLine line)
    {
        reader = AudioReader.getInstance(line, 1500);
    }
    public FaxTokenizer(AudioInputStream ais)
    {
        reader = AudioReader.getInstance(ais, 1500);
    }
    
    public void addListener(FaxListener listener)
    {
        listeners.add(listener);
    }
    public void removeListener(FaxListener listener)
    {
        listeners.remove(listener);
    }

    public void addFrequencyListener(FrequencyListener listener)
    {
        frequencyListeners.add(listener);
    }
    public void removeToneListener(FrequencyListener listener)
    {
        frequencyListeners.remove(listener);
    }

    public void addDataListener(DataListener dataListener)
    {
        reader.addDataListener(dataListener);
    }

    public void removeDataListener(DataListener dataListener)
    {
        reader.removeDataListener(dataListener);
    }

    public int getSize()
    {
        return reader.getSize();
    }
    
    public void run() throws IOException
    {
        long prev = 0;
        FaxTone prevTone = UNKNOWN;
        while (true)
        {
            FaxTone tone = nextTone();
            long micros = getMicros();
            long span = micros - prev;
            float amplitude = getAmplitude();
            for (FaxListener l : listeners)
            {
                l.tone(prevTone, prev, micros, span, amplitude, 0);
            }
            prev = micros;
            prevTone = tone;
        }
    }

    private FaxTone nextTone() throws IOException
    {
        while (true)
        {
            FaxTone tone;
            float frequency = reader.getHalfWave();
            for (FrequencyListener l : frequencyListeners)
            {
                l.frequency(frequency, reader.getMicros());
            }
            if (frequency > 1100)
            {
                if (frequency < 1900)
                {
                    tone = BLACK;
                }
                else
                {
                    if (frequency < 2900)
                    {
                        tone = WHITE;
                    }
                    else
                    {
                        tone = HIGH;
                    }
                }
            }
            else
            {
                tone = LOW;
            }
            if (tone != last)
            {
                last = tone;
                return tone;
            }
        }
    }

    public long getMicros()
    {
        return reader.getMicros();
    }

    public float getAmplitude()
    {
        return reader.getAmplitude();
    }

    public float getSampleRate()
    {
        return reader.getSampleRate();
    }

    public int getFrameSize()
    {
        return reader.getFrameSize();
    }
    
}
