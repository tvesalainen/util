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

import java.nio.ByteBuffer;
import javax.sound.sampled.LineUnavailableException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
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
    public void test1() throws LineUnavailableException
    {
        double pitch1 = 60;
        int rate = 5120;
        byte[] tone1 = MorseCode.createTone(300, rate, pitch1);
        FFT fft = new FFT(256);
        IntArray ia = IntArray.getInstance(tone1, 0, 256);
        assertEquals(pitch1, fft.frequency(rate, ia), 1e-5);
    }
    @Test
    public void test2() throws LineUnavailableException
    {
        double pitch1 = 60;
        int rate = 5120;
        byte[] tone1 = MorseCode.createTone(300, rate, pitch1);
        byte[] tone2 = MorseCode.createTone(300, rate, pitch1*2);
        for (int ii=0;ii<256;ii++)
        {
            tone1[ii] = (byte) ((tone1[ii]+tone2[ii])/2);
        }        
        FFT fft = new FFT(256);
        IntArray ia = IntArray.getInstance(tone1, 0, 256);
        assertEquals(pitch1, fft.frequency(rate, ia), 1e-5);
        for (int ii=0;ii<256;ii++)
        {
            System.err.printf("%03d: %d\n", ii, tone1[ii]);
        }
    }
    
}
