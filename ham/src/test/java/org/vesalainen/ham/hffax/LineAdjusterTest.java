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
public class LineAdjusterTest
{
    
    public LineAdjusterTest()
    {
    }

    @Test
    public void test1()
    {
        long expll = 500010;
        LineAdjuster la = new LineAdjuster(500000);
        
        la.update(5, 999995);
        la.update(4, 999996);
        
        la.update(1, 999999+expll);
        assertEquals(999996, la.getStartOfLine());
        //assertEquals(500013, la.getLineLength());

        for (int ii=1;ii<10;ii++)
        {
            la.update(1, 999999+ii*expll);
            la.update(0, 1000000+ii*expll);
        }
        assertEquals(999998, la.getStartOfLine());
        assertEquals(expll, la.getLineLength());
    }
    
}
