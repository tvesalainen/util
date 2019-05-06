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
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Test;
import org.vesalainen.io.IO;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.PolarPlotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSplineTest
{
    
    public PolarCubicSplineTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        Point2D.Double p1 = new Point2D.Double(270, 6);
        Point2D.Double p2 = new Point2D.Double(280, 5);
        Point2D.Double p3 = new Point2D.Double(290, 6);
        Point2D.Double p4 = new Point2D.Double(270, 15);
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        PolarCubicSpline pcs = new PolarCubicSpline(p1, p2, p3);
        if (!pcs.isInjection())
        {
            pcs.forceInjection();
        }
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(pcs);
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates(LEFT, TOP);
        plotter.plot("dev2.png");
    }
    
    @Test
    public void test2() throws IOException
    {
        Point2D.Double p1 = new Point2D.Double(121, -10.2);
        Point2D.Double p2 = new Point2D.Double(133, 12.3);
        Point2D.Double p3 = new Point2D.Double(156, 25.5);
        Point2D.Double p4 = new Point2D.Double(172, -19.2);
        Point2D.Double p5 = new Point2D.Double(179, -9.7);
        Point2D.Double p6 = new Point2D.Double(180, -15.6);
        Point2D.Double p7 = new Point2D.Double(182, -14.2);
        Point2D.Double p8 = new Point2D.Double(190, 2.1);
        Point2D.Double p9 = new Point2D.Double(200, -1.9);
        Point2D.Double p10 = new Point2D.Double(206, 1.4);
        PolarCubicSpline pcs = new PolarCubicSpline(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
        if (!pcs.isInjection())
        {
            pcs.forceInjection();
        }
        double v = pcs.eval(200, 0.01);
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        plotter.draw(pcs);
        plotter.setColor(Color.LIGHT_GRAY);
        plotter.drawCoordinates(LEFT, TOP);
        plotter.plot("dev3.png");
    }
    //@Test
    public void testPlot() throws IOException, ClassNotFoundException
    {
        PointList list = IO.deserialize(Paths.get("c:\\temp\\deviation_build.ser"));//new PointList();
        //list.add(17, 105);
        //list.add(117, 103);
        //list.add(317, 100.5);
        //list.add(357, 100);
        list.sort();
        PolarCubicSpline pcs = new PolarCubicSpline(list.array());
        PolarPlotter plotter = new PolarPlotter(1000, 1000, Color.WHITE);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        //plotter.drawLine(0, 1, 360, 1);
        plotter.draw(pcs);
        //plotter.drawCoordinates(LEFT, TOP);
        plotter.plot("dev.png");
    }
}

