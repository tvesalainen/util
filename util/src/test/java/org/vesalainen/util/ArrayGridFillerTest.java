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
public class ArrayGridFillerTest
{
    
    public ArrayGridFillerTest()
    {
    }

    @Test
    public void testAllDirections1()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::allDirections);
        assertEquals(grid.getArray().length, agf.fill(0, 0, null).getSetCount());
    }
    @Test
    public void testAllDirections2()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::allDirections);
        Color[] array = grid.getArray();
        array[5] = Color.BLACK;
        array[8] = Color.GREEN;
        array[12] = Color.CYAN;
        array[32] = Color.BLUE;
        assertEquals(array.length-4, agf.fill(0, 0, null).getSetCount());
    }
    @Test
    public void testRoundedSquare1()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::roundedSquare);
        assertEquals(1, agf.fill(0, 0, null).getSetCount());
    }
    @Test
    public void testRoundedSquare2()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::roundedSquare);
        Color[] array = grid.getArray();
        assertEquals(array.length-4, agf.fill(3, 4, null).getSetCount());
    }
    @Test
    public void testRoundedSquare3()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::roundedSquare);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = grid.getArray();
        array[20] = Color.BLACK;
        array[22] = Color.BLACK;
        array[24] = Color.BLACK;
        assertEquals(16, agf.fill(2, 2, null).getSetCount());
    }
    @Test
    public void testRoundedSquareNotBoxed()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, false);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::roundedSquare);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = grid.getArray();
        grid.setColor(2, 0, Color.BLACK);
        grid.setColor(2, 2, Color.BLACK);
        grid.setColor(2, 4, Color.BLACK);
        grid.setColor(2, 6, Color.BLACK);
        assertEquals(28, agf.fill(3, 3, null).getSetCount());
    }
    @Test
    public void testSquare3()
    {
        SimpleArrayGrid<Color> grid = new SimpleArrayGrid<>(new Color[5*8], 5, true);
        ArrayGridFiller<Color> agf = new ArrayGridFiller(grid, ArrayGridFiller::square);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = grid.getArray();
        array[20] = Color.BLACK;
        array[22] = Color.BLACK;
        array[24] = Color.BLACK;
        assertEquals(20, agf.fill(2, 2, null).getSetCount());
    }

}
