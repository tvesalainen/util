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
import static org.vesalainen.ui.scale.Decibel.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DecibelScaleTest
{

    public DecibelScaleTest()
    {
    }

    @Test
    public void test0()
    {
        DecibelScale ds = new DecibelScale(10);
        Tester.generator(ds, 1, 100);
    }

    @Test
    public void testAuto()
    {
        ScaleLevel level;
        Tester t;
        DecibelScale scale = new DecibelScale(10);
        Iterator<ScaleLevel> iterator = scale.iterator(1.0, 100.0);
        t = new Tester();
        t.add(1.0, "-10dB");
        t.add(10.0, "0dB");
        t.add(100.0, "10dB");
        level = iterator.next();
        level.forEach(1.0, 100.0, Locale.US, t::check);
        t = new Tester();
        t.add(1.2589254117941673, "-9dB");
        t.add(2.51188643150958, "-6dB");
        t.add(5.011872336272722, "-3dB");
        t.add(10.0, "0dB");
        t.add(19.952623149688794, "3dB");
        t.add(39.81071705534972, "6dB");
        t.add(79.43282347242815, "9dB");
        level = iterator.next();
        level.forEach(1.0, 100.0, Locale.US, t::check);
        t = new Tester();
        t.add(1.0, "-10dB");
        t.add(1.2589254117941673, "-9dB");
        t.add(1.5848931924611134, "-8dB");
        t.add(1.9952623149688797, "-7dB");
        t.add(2.51188643150958, "-6dB");
        t.add(3.162277660168379, "-5dB");
        t.add(3.981071705534972, "-4dB");
        t.add(5.011872336272722, "-3dB");
        t.add(6.3095734448019325, "-2dB");
        t.add(7.943282347242815, "-1dB");
        t.add(10.0, "0dB");
        t.add(12.589254117941673, "1dB");
        t.add(15.848931924611135, "2dB");
        t.add(19.952623149688794, "3dB");
        t.add(25.1188643150958, "4dB");
        t.add(31.622776601683793, "5dB");
        t.add(39.81071705534972, "6dB");
        t.add(50.118723362727216, "7dB");
        t.add(63.09573444801933, "8dB");
        t.add(79.43282347242815, "9dB");
        t.add(100.0, "10dB");
        level = iterator.next();
        level.forEach(1.0, 100.0, Locale.US, t::check);
    }

}
