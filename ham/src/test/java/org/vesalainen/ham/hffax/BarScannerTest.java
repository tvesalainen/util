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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BarScannerTest
{
    
    public BarScannerTest()
    {
    }

    @Test
    public void test1()
    {
        int[] buf = new int[32];
        for (int ii=3;ii<8;ii++)
        {
            buf[ii] = 1;
            buf[ii+16] = 1;
        }
        BarScanner bs = new BarScanner(16, 1);
        
        bs.maxBar(buf, 1, 2);
        assertEquals(3, bs.getBegin());
        assertEquals(5, bs.getLength());
        
        bs.maxBar(buf, 0, 2);
        assertEquals(8, bs.getBegin());
        assertEquals(11, bs.getLength());
    }
    
}
