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
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.hffax.FaxTokenizer.Tone;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxTokenizerTest
{
    
    public FaxTokenizerTest()
    {
    }

    @Test
    public void test() throws UnsupportedAudioFileException, IOException
    {
        URL url = HFFaxTest.class.getResource("/wefax1.wav");
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        FaxTokenizer ft = new FaxTokenizer(ais);
        long prev = 0;
        while (true)
        {
            Tone tone = ft.nextTone();
            long micros = ft.getMicros();
            System.err.printf("%09d: %s\n", micros-prev, tone);
            prev = micros;
        }
    }
    
}
