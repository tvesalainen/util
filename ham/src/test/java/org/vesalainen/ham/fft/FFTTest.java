/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.fft;

import javax.sound.sampled.LineUnavailableException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.vesalainen.ham.morse.MorseCode;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FFTTest
{
    
    public FFTTest()
    {
    }

    @Test
    public void test() throws LineUnavailableException
    {
        double pitch = 2000;
        int rate = 44000;
        byte[] tone = MorseCode.createTone(1000, rate, pitch);
        FFT fft = new FFT(tone.length);
        double frequency = fft.frequency(tone, rate);
        assertEquals(pitch, frequency, 0.1);
    }
    
}
