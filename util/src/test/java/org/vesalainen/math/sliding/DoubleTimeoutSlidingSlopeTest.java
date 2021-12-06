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
import org.vesalainen.math.MathFunction;
import org.vesalainen.math.SineFitter;
import org.vesalainen.navi.Tide;
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
    @Test
    public void testTide() throws IOException
    {
        double PI2 = 2*PI;
        Random random = new Random(1234567L);
        DoubleUnaryOperator noise = (x)->x+0.01*random.nextGaussian();
        double step = 0.1;
        DoubleUnaryOperator stepper = (x)->floor(x/step)*step+step/2;
        DoubleToLongFunction timer = (x)->(long) (Tide.PERIOD*x/PI2);
        LongToDoubleFunction xer = (t)->PI2*t/Tide.PERIOD;
        LongReference time = new LongReference(0);
        DoubleTimeoutSlidingSlope sloper = new DoubleTimeoutSlidingSlope(()->time.getValue(), 1000, Tide.PERIOD/18, xer);
        SineFitter sineFitter = new SineFitter();
        Plotter p = new Plotter(1024, 1024, WHITE, false);
        Polyline blue = p.polyline(BLUE);
        Polyline green = p.polyline(GREEN);
        Polyline yellow = p.polyline(YELLOW);
        for (long t=0;t<Tide.PERIOD;t+=1000)
        {
            time.setValue(t);
            double x = xer.applyAsDouble(t);
            double y1 = sin(x);
            blue.lineTo(t, y1);
            double y0 = cos(x);
            yellow.lineTo(t, y0);
            double y2 = stepper.applyAsDouble(noise.applyAsDouble(y1));
            green.lineTo(t, y2);
            sloper.accept(y2, t);
            double slope = sloper.slope();
            if (sloper.fullness() > 99)
            {
                p.setColor(RED);
                p.drawCross(sloper.meanTime(), slope);
                sineFitter.addPoints(xer.applyAsDouble(sloper.meanTime()), slope);
                sineFitter.fit();
                MathFunction cosz = sineFitter.getCos();
                MathFunction cost = (tt)->-cosz.applyAsDouble(xer.applyAsDouble((long) tt));
                int n = sineFitter.getPointCount();
                p.setColor(Color.getHSBColor((float) (n*0.1), 1, 1));
                p.draw(cost, 0, -1, Tide.PERIOD, 1);
                sloper.clear();
            }
        }
        p.drawPolyline(blue);
        p.drawPolyline(green);
        p.drawPolyline(yellow);
        p.setColor(BLACK);
        p.drawCoordinates();
        p.plot("c:\\temp\\tide.png");
    }    
}
