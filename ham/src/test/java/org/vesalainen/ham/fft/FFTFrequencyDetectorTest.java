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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FFTFrequencyDetectorTest
{
    
    public FFTFrequencyDetectorTest()
    {
    }

    //@Test
    public void test1()
    {
        byte[] tone = MorseCode.createTone(100, 48000, 1500);
        FFTFrequencyDetector fd = new FFTFrequencyDetector(64, 48000);
        for (byte b : tone)
        {
            fd.update(b);
        }
        assertEquals(2300, fd.getFrequency(), 1e-8);
    }
    
}
