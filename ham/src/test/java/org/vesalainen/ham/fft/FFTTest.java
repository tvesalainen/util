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
package org.vesalainen.ham.fft;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.morse.MorseCode;
import org.vesalainen.nio.IntArray;

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
    public void test1500()
    {
        byte[] tone = MorseCode.createTone(100, 48000, 1500);
        FFT fft = new FFT(64);
        IntArray array = IntArray.getInstance(tone);
        fft.forward(array);
        assertTrue(fft.getMagnitude(48000, 1500) > 10);
    }
    @Test
    public void test2300()
    {
        byte[] tone = MorseCode.createTone(100, 48000, 2300);
        FFT fft = new FFT(64);
        IntArray array = IntArray.getInstance(tone);
        fft.forward(array);
        assertTrue(fft.getMagnitude(48000, 2300) > 10);
    }
    
}
