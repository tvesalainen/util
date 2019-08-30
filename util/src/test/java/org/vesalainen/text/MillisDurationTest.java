/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MillisDurationTest
{
    M m = new M(1*24*60*60*1000+2*60*60*1000+3*60*1000+4*1000+567);
    public MillisDurationTest()
    {
    }

    @Test
    public void testAll()
    {
        assertEquals("1 d 2 h 3 m 4.567 s", String.format("%s", m));
    }
    
    @Test
    public void testLeft()
    {
        assertEquals("1 d   ", String.format("%-6s", m));
    }
    
    @Test
    public void testWidth1()
    {
        assertEquals("   1 d", String.format("%6s", m));
    }
    
    @Test
    public void testWidth2()
    {
        assertEquals(" 1 d 2 h 3 m 4 s", String.format("%16s", m));
    }
    
    @Test
    public void test0()
    {
        assertEquals("1 d", String.format("%.0s", m));
    }
    
    @Test
    public void test1()
    {
        assertEquals("1 d 2 h", String.format("%.1s", m));
    }
    
    @Test
    public void test2()
    {
        assertEquals("1 d 2 h 3 m", String.format("%.2s", m));
    }
    
    @Test
    public void test3()
    {
        assertEquals("1 d 2 h 3 m 4 s", String.format("%.3s", m));
    }
    
    @Test
    public void test4()
    {
        assertEquals("1 d 2 h 3 m 4.567 s", String.format("%.4s", m));
    }
    
    @Test
    public void testAlt()
    {
        assertEquals("1 days 2 hours 3 minutes 4.567 seconds", String.format("%#s", m));
    }
    
    @Test
    public void testUpper()
    {
        assertEquals("1 DAYS 2 HOURS 3 MINUTES 4.567 SECONDS", String.format("%#S", m));
    }
    
    class M implements MillisDuration
    {
        long millis;

        public M(long millis)
        {
            this.millis = millis;
        }

        @Override
        public long millis()
        {
            return millis;
        }
        
    }
}
