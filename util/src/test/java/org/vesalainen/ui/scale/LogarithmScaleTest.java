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
public class LogarithmScaleTest
{

    public LogarithmScaleTest()
    {
        //LogScale ds = new LogarithmScale(2);
        //Tester.generator(ds, 1, 20000, 2);
    }

    @Test
    public void testAuto2()
    {
        ScaleLevel level;
        Tester t;
        LogarithmScale scale = new LogarithmScale(2);
        Iterator<ScaleLevel> iterator = scale.iterator(1.0, 20000.0);
        t = new Tester();
        t.add(1.0, "2\u2070");
        t.add(1024.0, "2\u00B9\u2070");
        level = iterator.next();
        level.forEach(1.0, 20000.0, Locale.US, t::check);
        t = new Tester();
        t.add(1.0, "2\u2070");
        t.add(2.0, "2\u00B9");
        t.add(4.0, "2\u00B2");
        t.add(8.0, "2\u00B3");
        t.add(16.0, "2\u2074");
        t.add(32.0, "2\u2075");
        t.add(64.0, "2\u2076");
        t.add(128.0, "2\u2077");
        t.add(256.0, "2\u2078");
        t.add(512.0, "2\u2079");
        t.add(1024.0, "2\u00B9\u2070");
        t.add(2048.0, "2\u00B9\u00B9");
        t.add(4096.0, "2\u00B9\u00B2");
        t.add(8192.0, "2\u00B9\u00B3");
        t.add(16384.0, "2\u00B9\u2074");
        level = iterator.next();
        level.forEach(1.0, 20000.0, Locale.US, t::check);
    }

    @Test
    public void testAuto10()
    {
        ScaleLevel level;
        Tester t;
        LogarithmScale scale = new LogarithmScale(10);
        Iterator<ScaleLevel> iterator = scale.iterator(1.0, 20000.0);
        t = new Tester();
        t.add(1.0, "10\u2070");
        t.add(10.0, "10\u00B9");
        t.add(100.0, "10\u00B2");
        t.add(1000.0, "10\u00B3");
        t.add(10000.0, "10\u2074");
        level = iterator.next();
        level.forEach(1.0, 20000.0, Locale.US, t::check);
        t = new Tester();
        t.add(1.0, "10\u2070");
        t.add(1.2589254117941673, "1.3x10\u2070");
        t.add(1.5848931924611136, "1.6x10\u2070");
        t.add(1.9952623149688797, "2.0x10\u2070");
        t.add(2.51188643150958, "2.5x10\u2070");
        t.add(3.1622776601683795, "3.2x10\u2070");
        t.add(3.9810717055349722, "4.0x10\u2070");
        t.add(5.011872336272722, "5.0x10\u2070");
        t.add(6.309573444801932, "6.3x10\u2070");
        t.add(7.943282347242813, "7.9x10\u2070");
        t.add(9.999999999999998, "10\u00B9");
        t.add(12.589254117941667, "1.3x10\u00B9");
        t.add(15.848931924611133, "1.6x10\u00B9");
        t.add(19.952623149688797, "2.0x10\u00B9");
        t.add(25.11886431509581, "2.5x10\u00B9");
        t.add(31.62277660168381, "3.2x10\u00B9");
        t.add(39.810717055349755, "4.0x10\u00B9");
        t.add(50.11872336272727, "5.0x10\u00B9");
        t.add(63.09573444801939, "6.3x10\u00B9");
        t.add(79.43282347242825, "7.9x10\u00B9");
        t.add(100.0000000000001, "10\u00B2");
        t.add(125.89254117941688, "1.3x10\u00B2");
        t.add(158.48931924611156, "1.6x10\u00B2");
        t.add(199.5262314968883, "2.0x10\u00B2");
        t.add(251.18864315095848, "2.5x10\u00B2");
        t.add(316.2277660168386, "3.2x10\u00B2");
        t.add(398.1071705534981, "4.0x10\u00B2");
        t.add(501.18723362727354, "5.0x10\u00B2");
        t.add(630.9573444801949, "6.3x10\u00B2");
        t.add(794.3282347242838, "7.9x10\u00B2");
        t.add(1000.0000000000031, "10\u00B3");
        t.add(1258.9254117941714, "1.3x10\u00B3");
        t.add(1584.893192461119, "1.6x10\u00B3");
        t.add(1995.262314968887, "2.0x10\u00B3");
        t.add(2511.88643150959, "2.5x10\u00B3");
        t.add(3162.2776601683922, "3.2x10\u00B3");
        t.add(3981.0717055349896, "4.0x10\u00B3");
        t.add(5011.872336272745, "5.0x10\u00B3");
        t.add(6309.573444801962, "6.3x10\u00B3");
        t.add(7943.282347242854, "7.9x10\u00B3");
        t.add(10000.00000000004, "10\u2074");
        t.add(12589.254117941713, "1.3x10\u2074");
        t.add(15848.931924611174, "1.6x10\u2074");
        t.add(19952.62314968883, "2.0x10\u2074");
        level = iterator.next();
        level.forEach(1.0, 20000.0, Locale.US, t::check);
    }
}
