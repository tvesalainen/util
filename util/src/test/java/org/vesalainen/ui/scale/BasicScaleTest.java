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
import java.util.PrimitiveIterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicScaleTest
{

    public BasicScaleTest()
    {
        BasicScale ds = new BasicScale(5);
        Tester.generator(ds, 0, 50, 2);
    }

    @Test
    public void testAuto1()
    {
        ScaleLevel level;
        Tester t;
        BasicScale scale = new BasicScale(1);
        Iterator<ScaleLevel> iterator = scale.iterator(0.0, 50.0);
        t = new Tester();
        t.add(0.0, "0");
        t.add(10.0, "10");
        t.add(20.0, "20");
        t.add(30.0, "30");
        t.add(40.0, "40");
        t.add(50.0, "50");
        level = iterator.next();
        level.forEach(0.0, 50.0, Locale.US, t::check);
        t = new Tester();
        t.add(0.0, "0");
        t.add(1.0, "1");
        t.add(2.0, "2");
        t.add(3.0, "3");
        t.add(4.0, "4");
        t.add(5.0, "5");
        t.add(6.0, "6");
        t.add(7.0, "7");
        t.add(8.0, "8");
        t.add(9.0, "9");
        t.add(10.0, "10");
        t.add(11.0, "11");
        t.add(12.0, "12");
        t.add(13.0, "13");
        t.add(14.0, "14");
        t.add(15.0, "15");
        t.add(16.0, "16");
        t.add(17.0, "17");
        t.add(18.0, "18");
        t.add(19.0, "19");
        t.add(20.0, "20");
        t.add(21.0, "21");
        t.add(22.0, "22");
        t.add(23.0, "23");
        t.add(24.0, "24");
        t.add(25.0, "25");
        t.add(26.0, "26");
        t.add(27.0, "27");
        t.add(28.0, "28");
        t.add(29.0, "29");
        t.add(30.0, "30");
        t.add(31.0, "31");
        t.add(32.0, "32");
        t.add(33.0, "33");
        t.add(34.0, "34");
        t.add(35.0, "35");
        t.add(36.0, "36");
        t.add(37.0, "37");
        t.add(38.0, "38");
        t.add(39.0, "39");
        t.add(40.0, "40");
        t.add(41.0, "41");
        t.add(42.0, "42");
        t.add(43.0, "43");
        t.add(44.0, "44");
        t.add(45.0, "45");
        t.add(46.0, "46");
        t.add(47.0, "47");
        t.add(48.0, "48");
        t.add(49.0, "49");
        t.add(50.0, "50");
        level = iterator.next();
        level.forEach(0.0, 50.0, Locale.US, t::check);
    }

    @Test
    public void testAuto5()
    {
        ScaleLevel level;
        Tester t;
        BasicScale scale = new BasicScale(5);
        Iterator<ScaleLevel> iterator = scale.iterator(0.0, 50.0);
        t = new Tester();
        t.add(0.0, "0");
        t.add(50.0, "50");
        level = iterator.next();
        level.forEach(0.0, 50.0, Locale.US, t::check);
        t = new Tester();
        t.add(0.0, "0");
        t.add(5.0, "5");
        t.add(10.0, "10");
        t.add(15.0, "15");
        t.add(20.0, "20");
        t.add(25.0, "25");
        t.add(30.0, "30");
        t.add(35.0, "35");
        t.add(40.0, "40");
        t.add(45.0, "45");
        t.add(50.0, "50");
        level = iterator.next();
        level.forEach(0.0, 50.0, Locale.US, t::check);
    }
}
