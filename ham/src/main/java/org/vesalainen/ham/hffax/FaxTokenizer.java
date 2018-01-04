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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import org.vesalainen.ham.AudioReader;
import static org.vesalainen.ham.hffax.FaxTokenizer.Tone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxTokenizer
{
    public enum Tone {START, STOP, BLACK, WHITE, UNKNOWN}; 
    private AudioReader reader;
    private Tone last = UNKNOWN;
    
    public FaxTokenizer(TargetDataLine line)
    {
        reader = AudioReader.getInstance(line, 4096);
    }
    public FaxTokenizer(AudioInputStream ais)
    {
        reader = AudioReader.getInstance(ais, 4096);
    }
    
    public Tone nextTone() throws IOException
    {
        while (true)
        {
            Tone tone;
            float frequency = reader.getHalfWave();
            if (frequency > 1100)
            {
                if (frequency < 1900)
                {
                    tone = BLACK;
                }
                else
                {
                    tone = WHITE;
                }
            }
            else
            {
                tone = START;
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
    
}
