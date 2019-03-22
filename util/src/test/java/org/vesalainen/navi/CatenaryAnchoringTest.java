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
    public void test0()
    {
        double mm = 10;
        ElasticChain ec = new ElasticChain(mm);
        double d = 5;
        double s = 25;
        double T = ec.fairleadTension(s, d);
        double Th0 = ec.horizontalForce(d, s);
        double Th = ec.AE*Math.sqrt(Math.pow(T/ec.AE+1, 2)-2*ec.w*d/ec.AE)-ec.AE;
        double lmin = Math.sqrt(T*T-Th*Th)/ec.w;
        double x0 = ec.horizontalScope(Th, d, s);
        double a = Th0/ec.w;
        Catenary c = new Catenary(a);
        assertEquals(d+a, c.applyAsDouble(x0), 1);
    }
    @Test
    public void testPlot() throws IOException
    {
        Plotter p = new Plotter(1000, 1000, Color.CYAN);
        p.setFont("Arial", BOLD, 20);
        Catenary catenary = new Catenary(60);
        p.draw(catenary, 0, 60, 40, 140);
        p.drawCoordinates();
        p.plot("Anchoring", "png");
        
    }
    @Test
    public void testDepth()
    {
        double mm = 10;
        ElasticChain ca = new ElasticChain(mm);
        double d0 = 5;
        double s0 = 25;
        double Th = ca.horizontalForce(d0, s0);
        for (int ii=1;ii<10;ii++)
        {
            double s1 = ca.chainLengthForHorizontalForce(Th, d0*ii);
            System.err.println(d0*ii+" "+s1);
        }
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
    public void testHorizontalForce()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double T = ca.fairleadTension(s, d);
        double Th = ca.horizontalForce(d, s);
        double Tz = ca.w*s;
        assertEquals(T, Math.hypot(Th, Tz), 1e-10);
    }
}
