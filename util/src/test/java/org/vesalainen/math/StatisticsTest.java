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
package org.vesalainen.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StatisticsTest
{
    
    public StatisticsTest()
    {
    }

    @Test
    public void test1()
    {
        XYSamples s1 = new XYSamples();
        s1.add(1, 2);
        s1.add(2, 4);
        s1.add(3, 6);
        s1.add(4, 8);
        s1.add(5, 10);
        
        BestFitLine bfl = new BestFitLine(s1);
        AbstractLine line = bfl.getLine();
        assertEquals(0, line.getA(), 1e-10);
        assertEquals(2, line.getSlope(), 1e-10);
        
        assertEquals(0, Statistics.rootMeanSquareError(s1, line), 1e-10);
        assertEquals(0, Statistics.meanAbsoluteError(s1, line), 1e-10);
        
        XYSamples s2 = new XYSamples();
        s2.add(1, 2+1);
        s2.add(2, 4+1);
        s2.add(3, 6+1);
        s2.add(4, 8+1);
        s2.add(5, 10+1);
        
        assertEquals(1, Statistics.rootMeanSquareError(s2, line), 1e-10);
        assertEquals(1, Statistics.meanAbsoluteError(s2, line), 1e-10);
        
    }    
}
