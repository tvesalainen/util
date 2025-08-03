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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import org.vesalainen.text.Unicodes;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Tester
{
    private List<Double> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private int index;
    
    public void add(double value, String label)
    {
        values.add(value);
        labels.add(label);
    }
    public void check(double value, String label)
    {
        assertEquals(values.get(index), value, 1e-10);
        assertEquals(labels.get(index), label);
        index++;
    }
    public static void generator(Scale scale, double min, double max)
    {
        generator(scale, min, max, 3);
    }
    public static void generator(Scale scale, double min, double max, int levels)
    {
        String simpleName = scale.getClass().getSimpleName();
        System.err.println("@Test");
        System.err.println("public void testAuto()");
        System.err.println("{");
        System.err.println("ScaleLevel level;");
        System.err.println("Tester t;");
        System.err.println(simpleName+" scale = new "+simpleName+"();");
        System.err.println("Iterator<ScaleLevel> iterator = scale.iterator("+min+", "+max+");");
        Iterator<ScaleLevel> iterator = scale.iterator(min, max);
        for (int ii=0;ii<levels;ii++)
        {
            System.err.println("t = new Tester();");
            ScaleLevel next = iterator.next();
            next.forEach(min, max, Locale.US, (v,l)->System.err.println("t.add("+v+", \""+Unicodes.escape(l)+"\");"));
            System.err.println("level = iterator.next();");
            System.err.println("level.forEach("+min+", "+max+", Locale.US, t::check);");
        }
        System.err.println("}");
    }
}
