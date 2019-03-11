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
import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

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
        DoubleTransform d = t.derivate();
        Point2D.Double exp = new Point2D.Double(at.getScaleX()+at.getShearX(), at.getScaleY()+at.getShearY());
        Point2D.Double got = new Point2D.Double();
        d.transform(100, 6, got::setLocation);
        assertEquals(exp.x, got.x, 1e-10);
        assertEquals(exp.y, got.y, 1e-10);
    }
    @Test
    public void testInverseAffineTransform() throws NoninvertibleTransformException
    {
        AffineTransform at = new AffineTransform(1, 2, 3, 4, 5, 6);
        DoubleTransform t = Transforms.affineInverseTransform(at);
        DoubleTransform d = t.derivate();
        Point2D.Double exp = new Point2D.Double(-0.5, 0.5);
        Point2D.Double got = new Point2D.Double();
        d.transform(100, 6, got::setLocation);
        assertEquals(exp.x, got.x, 1e-10);
        assertEquals(exp.y, got.y, 1e-10);
    }
    
}
