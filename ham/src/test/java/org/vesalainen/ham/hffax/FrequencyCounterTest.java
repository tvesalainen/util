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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.morse.MorseCode;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FrequencyCounterTest
{
    
    public FrequencyCounterTest()
    {
    }

    //@Test
    public void test1()
    {
        byte[] tone = MorseCode.createTone(100, 44000, 400);
        FrequencyCounter fc = new FrequencyCounter(44000);
        assertEquals(400F, fc.count(tone), 0.001F);
        assertEquals(100000, fc.getMicros());
    }
    
    //@Test
    public void test2()
    {
        byte[] tone = MorseCode.createTone(100, 44000, 400);
        for (int ii=0;ii<tone.length;ii++)
        {
            tone[ii] += 5;
        }
        FrequencyCounter fc = new FrequencyCounter(44000);
        assertEquals(400F, fc.count(tone), 0.001F);
    }
    
}
