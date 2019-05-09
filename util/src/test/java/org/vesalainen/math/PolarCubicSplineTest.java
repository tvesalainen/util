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
        Point2D.Double p1 = new Point2D.Double(0, 13.1);
        Point2D.Double p2 = new Point2D.Double(120, 2.2);
        Point2D.Double p3 = new Point2D.Double(124, 0.9);
        Point2D.Double p4 = new Point2D.Double(126, 4.0);
        Point2D.Double p5 = new Point2D.Double(128, 0.9);
        Point2D.Double p6 = new Point2D.Double(130, 1.8);
        Point2D.Double p7 = new Point2D.Double(134, 11.9);
        Point2D.Double p8 = new Point2D.Double(137, 3.2);
        Point2D.Double p9 = new Point2D.Double(140, 0.7);
        Point2D.Double p10 = new Point2D.Double(145, 1.0);
        Point2D.Double p11 = new Point2D.Double(151, 3.3);
        Point2D.Double p12 = new Point2D.Double(154, 1.8);
        Point2D.Double p13 = new Point2D.Double(156, 1.1);
        Point2D.Double p14 = new Point2D.Double(159, 1.1);
        Point2D.Double p15 = new Point2D.Double(160, 2.3);
        Point2D.Double p16 = new Point2D.Double(163, 1.1);
        Point2D.Double p17 = new Point2D.Double(165, 5.3);
        Point2D.Double p18 = new Point2D.Double(166, 3.8);
        Point2D.Double p19 = new Point2D.Double(169, 2.9);
        Point2D.Double p20 = new Point2D.Double(171, 3.5);
        Point2D.Double p21 = new Point2D.Double(172, 1.4);
        Point2D.Double p22 = new Point2D.Double(175, 0.8);
        Point2D.Double p23 = new Point2D.Double(177, 3.9);
        Point2D.Double p24 = new Point2D.Double(182, 5.1);
        Point2D.Double p25 = new Point2D.Double(183, 5.4);
        Point2D.Double p26 = new Point2D.Double(190, 8.5);
        Point2D.Double p27 = new Point2D.Double(194, -5.3);
        Point2D.Double p28 = new Point2D.Double(195, -4.8);
        Point2D.Double p29 = new Point2D.Double(198, -4.7);
        Point2D.Double p30 = new Point2D.Double(303, -5.5);
        Point2D.Double p31 = new Point2D.Double(319, 3.8);
        
        PolarCubicSpline pcs = new PolarCubicSpline(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22, p23, p24, p25, p26, p27, p28, p29, p30, p31);
        if (!pcs.isInjection())
        {
            pcs.forceInjection((i)->System.err.println(i));
        }
        pcs.setDrawWithControlPoints(true);
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

