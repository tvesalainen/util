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

import org.vesalainen.navi.dd1.DD1Calc;
import java.io.IOException;
import static java.lang.Math.*;
import java.nio.file.Paths;
import java.util.function.DoubleBinaryOperator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.Point;
import org.vesalainen.math.SimplePoint;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DD1CalcTest
{
    
    public DD1CalcTest()
    {
    }

    @Test
    public void test0() throws IOException
    {
    }
    @Test
    public void test01() throws IOException
    {
        DD1Calc dd1 = new DD1Calc(25, 16.5, 36, 65, 30, 13);
        dd1.optR1();
        //assertEquals(12.7, dd1.offset(), 1e-10);
        //assertEquals(dd1.link0(), dd1.link(), 1e-10);
        //assertEquals(36, dd1.alpha(65), 1e-8);
        //assertEquals(0, dd1.alpha(0), 1e-8);
        for (double beta=-65;beta<=65;beta+=5)
        {
            System.err.println(beta+" "+dd1.alpha(beta));
        }
        //assertEquals(-36, dd1.alpha(-65), 1e-10);
        //dd1.plot(Paths.get("dd01.png"));
    }
    @Test
    public void test02() throws IOException
    {
        DD1Calc dd1 = new DD1Calc(20, 13, 36, 65, 30, 12);
        dd1.optR1();
        //assertEquals(10.6, dd1.offset(), 1e-10);
        assertEquals(dd1.link0(), dd1.link(), 1e-10);
        dd1.plot(Paths.get("dd02.png"));
    }
}
