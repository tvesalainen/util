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

import java.awt.geom.AffineTransform;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.vesalainen.math.BezierCurve.LINE;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.math.matrix.DoubleMatrix;
import org.vesalainen.math.matrix.DoubleUnaryMatrix;
import org.vesalainen.ui.PolarTransform;
import org.vesalainen.ui.Transforms;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ParameterizedOperatorTest
{
    
    public ParameterizedOperatorTest()
    {
    }

    @Test
    public void testChain()
    {
        double x = 3.6;
        double y = 1;
        AffineTransform at = AffineTransform.getScaleInstance(100, 100);
        DoubleTransform tr = Transforms.affineTransform(at);
        DoubleMatrix s1 = tr.gradient().snapshot(x, y);
        PolarTransform pt = new PolarTransform();
        DoubleMatrix s2 = pt.gradient().snapshot(x, y);
        ParameterizedOperator line = LINE.operator(0, 1, 360, 1);
        DoubleTransform chain = pt.andThen(tr);
        DoubleBinaryMatrix chgrad = MoreMath.gradient(chain);
        DoubleMatrix s3 = chgrad.snapshot(x, y);
        DoubleMatrix s4 = chain.gradient().snapshot(x, y);
        ParameterizedOperator curve = line.andThen(chain);
        DoubleUnaryMatrix cgn = MoreMath.gradient(curve);
        DoubleMatrix s5 = cgn.snapshot(0.01);
        DoubleUnaryMatrix der = curve.derivative();
        DoubleMatrix s6 = der.snapshot(0.01);
        for (double t=0.01;t<1;t+=0.1)
        {
            assertTrue(cgn.equals(der, t, 1e-4));
        }
    }
    
}
