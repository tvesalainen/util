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
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.Catenary;
import org.vesalainen.math.MathFunction;
import static org.vesalainen.math.MoreMath.arsinh;
import org.vesalainen.ui.Plotter;

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
    public void testHorizontalScopeForChain() throws IOException
    {
        double mm = 10;
        double d = 10;
        double s = 80;
        ElasticChain ca = new ElasticChain(mm);
        double max = Chain.maximalScope(d, s);
        double min = Chain.minimalScope(d, s);
        MathFunction f = (T)->
                {
                    return ca.horizontalScopeForChain(T, d, s);
                };
        Plotter p = new Plotter(1000, 1000, WHITE, false);
        p.draw(f, 0, min, 4000, max);
        p.drawCoordinates();
        p.plot("c:\\temp\\scope.png");
    }
    @Test
    public void testHorizontalScopeForChain2() throws IOException
    {
        double mm = 10;
        double d = 10.399999618530273;
        double s = 38.41958928418916;
        double T = 3770.4991119416145;
        ElasticChain ca = new ElasticChain(mm);
        double horizontalScopeForChain = ca.horizontalScopeForChain(T, d, s);
    }
    @Test
    public void testFairleadTension()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double F = ca.fairleadMinimumTensionForChain(s, d);
        assertEquals(s, ca.chainLength(F, d), 1e-10);
    }
    @Test
    public void testHorizontalDistance()
    {
        double mm = 10;
        double d = 10;
        double s = 40;
        ElasticChain ca = new ElasticChain(mm);
        double F = ca.fairleadMinimumTensionForChain(s, d);
        double x = ca.horizontalScope(F, d);
        double a = Catenary.aForXAndH(x, d);
        Catenary c = new Catenary(a);
        assertEquals(d+a, c.applyAsDouble(x), 1e-10);
    }    
    @Test
    public void testForceForScope()
    {
        double mm = 10;
        double d = 10;
        double s = 50;
        double T = 2000;
        ElasticChain ca = new ElasticChain(mm);
        double w = ca.w;
        double scope = ca.horizontalScope(T, d);
        double chainLength = ca.chainLength(T, d);
        double a = T/w;
        double xScope = a*arsinh((chainLength)/a);   // under sea bed scope
        assertEquals(xScope, scope, 1e-10);
        double exp = scope*0.9;
        double horizontalScopeForChain = ca.horizontalScopeForChain(1e4, d, s);
        assertTrue(horizontalScopeForChain < Chain.maximalScope(d, s));
        double forceForScope = ca.forceForScope(exp, d, s);
        assertEquals(exp, ca.horizontalScopeForChain(forceForScope, d, s), 1e-10);
    }
    @Test
    public void testMaximalScope()
    {
        double d = 10;
        double s = 50;
        double max = Chain.maximalScope(d, s);
        assertEquals(s, Math.hypot(max, d), 1e-10);
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
        double T = ca.fairleadMinimumTensionForChain(s, d);
        double Th = ca.horizontalForce(T, d);
        double Tz = ca.w*s;
        assertEquals(T, Math.hypot(Th, Tz), 1e-10);
    }
}
