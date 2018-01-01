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
package org.vesalainen.ham;

import org.vesalainen.ham.morse.MorseCode;
import javax.sound.sampled.LineUnavailableException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MorseCodeTest
{
    
    public MorseCodeTest()
    {
    }

    //@Test
    public void test1() throws LineUnavailableException
    {
        try (MorseCode mc = MorseCode.getInstance(10))
        {
            long start = System.currentTimeMillis();
            mc.key("PARIS");
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed > 5500 && elapsed < 6500);
        }
    }
    
}
