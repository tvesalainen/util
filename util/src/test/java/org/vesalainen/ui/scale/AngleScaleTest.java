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
package org.vesalainen.ui.scale;

import java.util.Iterator;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AngleScaleTest
{
    
    public AngleScaleTest()
    {
    }

    @Test
    public void test1()
    {
    }
    
   //@Test
    public void test0()
    {
        Scale cs = new AngleScale();
        Iterator<ScaleLevel> iterator = cs.iterator(0, 180);
        for (int ii=0;ii<10;ii++)
        {
            ScaleLevel next = iterator.next();
            double step = next.step();
            double v = -180;
            for (int jj=0;jj<10;v+=step,jj++)
            {
                System.err.println(next.label(Locale.US, v));
            }
            System.err.println();
        }
    }
}

