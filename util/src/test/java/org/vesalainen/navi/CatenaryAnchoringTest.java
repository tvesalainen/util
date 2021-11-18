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
package org.vesalainen.navi;

import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.Catenary;
import org.vesalainen.math.MathFunction;
import org.vesalainen.ui.Plotter;
import org.vesalainen.ui.scale.LogarithmScale;
import org.vesalainen.ui.scale.MergeScale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CatenaryAnchoringTest
{
    
    public CatenaryAnchoringTest()
    {
    }

    @Test
    public void testPlot() throws IOException
    {
        Plotter p = new Plotter(1000, 1000, Color.CYAN);
        p.setColor(Color.BLACK);
        p.setFont("Arial", BOLD, 20);
        Catenary catenary = new Catenary(60);
        p.draw(catenary, -100, 100, 100, 200);
        p.drawCoordinates();
        p.plot("Anchoring", "png");
        
    }
    @Test
    public void testFairleadTension()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double F = ca.fairleadTension(s, d);
        assertEquals(s, ca.chainLength(F, d), 1e-10);
    }
    @Test
    public void testHorizontalDistance()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double F = ca.fairleadTension(s, d);
        double x = ca.horizontalScope(F, d);
        double a = Catenary.aForXAndH(x, d);
        Catenary c = new Catenary(a);
        assertEquals(d+a, c.applyAsDouble(x), 1e-10);
    }    
    @Test
    public void testMinimalDepth()
    {
        double mm = 10;
        double s = 80;
        double T = 2000;
        ElasticChain ca = new ElasticChain(mm);
        double d = ca.maximalDepth(T, s);
        assertEquals(26.66666666666667, d, 1e-10);
        double l = ca.chainLength(T, d);
        assertEquals(s, l, 1e-10);
    }
    //@Test
    public void testHorizontalForce()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double T = ca.fairleadTension(s, d);
        double Th = ca.horizontalForce(T, d);
        double Tz = ca.w*s;
        assertEquals(T, Math.hypot(Th, Tz), 1e-10);
    }
}
