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
package org.vesalainen.math;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RelaxedCubicSplineTest
{
    
    public RelaxedCubicSplineTest()
    {
    }

    @Test
    public void testEval() throws IOException
    {
        double[] p = new double[]{0, 0, 2, 9, 3, 2, 4, 6, 5, 1};
        RelaxedCubicSpline csi = new RelaxedCubicSpline(p);
        Plotter plotter = new Plotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(csi);
        plotter.setColor(Color.BLUE);
        plotter.draw(csi, 0, 2, 5, 5);
        plotter.drawCoordinates();
        plotter.plot("relaxed", "png");
        for (int ii=0;ii<5;ii++)
        {
            assertEquals(p[2*ii+1], csi.applyAsDouble(p[2*ii]), 1e-10);
        }
        assertEquals(3, csi.applyAsDouble(0.5), 0.1);
        assertEquals(7.8, csi.applyAsDouble(1.4), 0.1);
        assertEquals(4.2, csi.applyAsDouble(4.6), 0.1);
    }
    @Test
    public void testPlot() throws IOException
    {
        RelaxedCubicSpline csi = new RelaxedCubicSpline(1, -1, -1, 2, 1, 4, 4, 3, 7, 5);
        Plotter plotter = new Plotter(1000, 1000, Color.CYAN);
        plotter.setColor(Color.BLACK);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(csi);
        plotter.drawCoordinates();
        plotter.plot("BSpline", "png");
    }
    @Test
    public void testInjection1()
    {
        RelaxedCubicSpline csi = new RelaxedCubicSpline(0, 0, 1, 2, 2, 3);
        assertTrue(csi.isIsInjection());
    }
    @Test
    public void testInjection3()
    {
        RelaxedCubicSpline csi = new RelaxedCubicSpline(2, 0, 1, 2, 2, 3);
        assertFalse(csi.isIsInjection());
    }
    
}
