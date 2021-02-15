/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.navi.Tank.LimitedCorner;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TankTest
{
    
    public TankTest()
    {
    }

    @Test
    public void test1()
    {
        LimitedCorner c1 = new Tank.LimitedCorner(99, 32, 23, 24, 12, 13);
        LimitedCorner c2 = new Tank.LimitedCorner(99, 38, 24, 16, 4, 13);
        Tank tank = new Tank(c1, c2);
        double volumeFull = tank.volumeFull();
        assertEquals(111565.875, volumeFull, 1e-10);
        for (double p=100;p>0;p-=20)
        {
            System.err.println(p+"%H = "+tank.volume((p/100)*24));
        }
    }
    
}
