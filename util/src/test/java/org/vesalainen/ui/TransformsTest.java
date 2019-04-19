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
package org.vesalainen.ui;

import org.vesalainen.math.DoubleTransform;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TransformsTest
{
    
    public TransformsTest()
    {
    }

    @Test
    public void testAffineTransform()
    {
        AffineTransform at = new AffineTransform(1, 2, 3, 4, 5, 6);
        DoubleTransform t = Transforms.affineTransform(at);
        DoubleBinaryMatrix J = t.gradient();
        assertEquals(J.eval(0, 0, 100, 6)+J.eval(0, 1, 100, 6), at.getScaleX()+at.getShearX(), 1e-10);
        assertEquals(J.eval(1, 0, 100, 6)+J.eval(1, 1, 100, 6), at.getScaleY()+at.getShearY(), 1e-10);
    }
    @Test
    public void testInverseAffineTransform() throws NoninvertibleTransformException
    {
        AffineTransform at = new AffineTransform(1, 2, 3, 4, 5, 6);
        DoubleTransform t = Transforms.affineInverseTransform(at);
        DoubleBinaryMatrix J = t.gradient();
        assertEquals(J.eval(0, 0, 100, 6)+J.eval(0, 1, 100, 6), -0.5, 1e-10);
        assertEquals(J.eval(1, 0, 100, 6)+J.eval(1, 1, 100, 6), 0.5, 1e-10);
    }
    @Test
    public void testScale()
    {
        double scale = 10;
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        DoubleTransform t = Transforms.affineTransform(at);
        DoubleBinaryMatrix J = t.gradient();
        double det = J.determinant().applyAsDouble(0, 0);
        assertEquals(scale*scale, det, 1e-10);
    }
}
