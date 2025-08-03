/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.sliding;

import java.awt.Color;
import static java.awt.Color.*;
import java.io.IOException;
import static java.lang.Math.*;
import java.util.Random;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.CosineFitter;
import org.vesalainen.math.MathFunction;
import org.vesalainen.math.FunctionAfxBFitter;
import org.vesalainen.navi.Tide;
import org.vesalainen.navi.TideFitter;
import org.vesalainen.ui.AbstractPlotter;
import org.vesalainen.ui.AbstractPlotter.Polyline;
import org.vesalainen.ui.Plotter;
import org.vesalainen.util.LongReference;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingSlopeTest
{
    
    public DoubleTimeoutSlidingSlopeTest()
    {
    }

    @Test
    public void test0()
    {
        LongReference time = new LongReference(0);
        DoubleTimeoutSlidingSlope c = new DoubleTimeoutSlidingSlope(()->time.getValue(), 64, 10, (l)->l/2.0);
        c.accept(0, 0);
        c.accept(1, 1);
        time.setValue(15);
        c.accept(2, 16);
        c.accept(3, 17);
        c.accept(4, 18);
        assertEquals(2, c.slope(), 1e-10);
        c.clear();
        assertEquals(0, c.count());
    }
}
