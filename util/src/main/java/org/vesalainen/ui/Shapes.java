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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Shapes
{
    public static Shape scaleInPlace(Shape shape, double scaleX, double scaleY)
    {
        Rectangle2D b = shape.getBounds2D();
        double cx = b.getCenterX();
        double cy = b.getCenterY();
        AffineTransform at = AffineTransform.getTranslateInstance(cx-scaleX*cx, cy-scaleY*cy);
        at.scale(scaleX, scaleY);
        return at.createTransformedShape(shape);
    }
}
