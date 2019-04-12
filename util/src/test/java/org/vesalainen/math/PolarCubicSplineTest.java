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
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.Plotter;
import org.vesalainen.ui.PolarTransform;
import org.vesalainen.ui.scale.MergeScale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCubicSplineTest
{
    
    public PolarCubicSplineTest()
    {
    }

    //@Test
    public void testEval() throws IOException
    {
        Point2D.Double dy0 = new Point2D.Double();
        Point2D.Double ddy0 = new Point2D.Double();
        Point2D.Double dy360 = new Point2D.Double();
        Point2D.Double ddy360 = new Point2D.Double();
        PolarCubicSpline pcs = new PolarCubicSpline(0, 100, 90, 150, 180, 50, 270, 150);
        ParameterizedOperator c0 = pcs.getCurve(0);
        double t0 = c0.evalTForX(0);
        double y0 = c0.evalY(0);
        c0.derivative().eval(t0, dy0::setLocation);
        c0.secondDerivative().eval(t0, ddy0::setLocation);
        ParameterizedOperator c360 = pcs.getCurve(360);
        double t360 = c360.evalTForX(360);
        double y360 = c360.evalY(360);
        c360.derivative().eval(t360, dy360::setLocation);
        c360.secondDerivative().eval(t360, ddy360::setLocation);
        assertEquals(y0, y360, 1e-10);
        assertEquals(dy0.x, dy360.x, 1e-10);
        assertEquals(dy0.y, dy360.y, 1e-10);
        assertEquals(ddy0.x, ddy360.x, 1e-10);
        assertEquals(ddy0.y, ddy360.y, 1e-10);
    }
    
    @Test
    public void testPlot() throws IOException
    {
        Plotter plotter = new Plotter(1000, 1000, Color.WHITE, true, MergeScale.BASIC15, MergeScale.BASIC15, new PolarTransform());
        plotter.updatePoint(0, 2);
        plotter.updatePoint(90, 2);
        plotter.updatePoint(180, 2);
        plotter.updatePoint(270, 2);
        plotter.setColor(Color.RED);
        plotter.setFont("Arial", BOLD, 20);
        plotter.drawLine(0, 1, 360, 1);
        plotter.drawLine(0, 2, 100, 2);
        //plotter.draw(pcs);
        //plotter.drawCoordinates();
        plotter.plot("polar", "png");
    }
}

