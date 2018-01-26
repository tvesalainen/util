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
    @Test
    public void test2()
    {
        int[] buf = new int[48];
        for (int ii=3;ii<8;ii++)
        {
            buf[ii] = 1;
            buf[ii+16] = 1;
            buf[ii+32] = 1;
        }
        buf[1] = 1;
        buf[2] = 1;
        buf[2+16] = 1;
        
        buf[8+16] = 1;
        buf[8+32] = 1;
        buf[9+32] = 1;
        BarScanner bs = new BarScanner(16, 0);
        
        bs.maxBar(buf, 1, 3);
        assertEquals(3, bs.getBegin());
        assertEquals(1, bs.getBegin(0));
        assertEquals(2, bs.getBegin(1));
        assertEquals(3, bs.getBegin(2));
        assertEquals(5, bs.getLength());
        assertEquals(7, bs.getLength(0));
        assertEquals(7, bs.getLength(1));
        assertEquals(7, bs.getLength(2));
        
        bs.maxBar(buf, 0, 3);
        assertEquals(10, bs.getBegin());
        assertEquals(7, bs.getLength());
    }
    
}
