/*
 * Copyright (C) 2025 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import java.time.LocalTime;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.PolarPlotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocalTimeCubicSplineTest
{
    
    public LocalTimeCubicSplineTest()
    {
    }

    @Test
    public void test0() throws IOException
    {
        LocalTimeCubicSpline ltcs = LocalTimeCubicSpline.builder()
                .add(LocalTime.of(3, 0), 490)
                .add(LocalTime.of(6, 0), 400)
                .add(LocalTime.of(9, 0), 300)
                .add(LocalTime.of(12, 0), 153)
                .add(LocalTime.of(15, 0), 300)
                .add(LocalTime.of(18, 0), 400)
                .add(LocalTime.of(21, 0), 500)
                .add(LocalTime.of(0, 0), 490)
                .build();
        assertEquals(400, ltcs.applyAsDouble(LocalTime.of(6, 0)), 1e-10);
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setTraceColor(Color.BLACK);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(ltcs);
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates();
        plotter.plot("ltcs.png");
    }
    
}
