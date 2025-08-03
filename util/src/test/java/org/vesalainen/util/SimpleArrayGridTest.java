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
package org.vesalainen.util;

import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleArrayGridTest
{
    
    public SimpleArrayGridTest()
    {
    }

    @Test
    public void testLine()
    {
        SimpleArrayGrid<Color> ag = new SimpleArrayGrid<>(5, 8, 0);
        ag.line(1, 1, 8, Color.yellow);
        assertEquals(Color.yellow, ag.getColor(1, 1));
        assertEquals(Color.yellow, ag.getColor(3, 2));
    }

}
