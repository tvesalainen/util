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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Transforms
{

    /**
     * Creates translation that translates userBounds in cartesian coordinates 
     * to screenBound in screen coordinates.
     * <p>In cartesian y grows up while in screen y grows down
     * @param userBounds
     * @param screenBounds
     * @param keepAspectRatio 
     * @return 
     */
    public static AffineTransform createScreenTransform(Rectangle2D.Double userBounds, Rectangle2D.Double screenBounds, boolean keepAspectRatio)
    {
        return createScreenTransform(userBounds, screenBounds, keepAspectRatio, new AffineTransform());
    }
    /**
     * Creates translation that translates userBounds in cartesian coordinates 
     * to screenBound in screen coordinates.
     * <p>In cartesian y grows up while in screen y grows down
     * @param userBounds
     * @param screenBounds
     * @param keepAspectRatio
     * @param transform 
     * @return Returns parameter transform
     */
    public static AffineTransform createScreenTransform(Rectangle2D.Double userBounds, Rectangle2D.Double screenBounds, boolean keepAspectRatio, AffineTransform transform)
    {
        double width = screenBounds.width;
        double height = screenBounds.height;
        double aspect = width / height;
        double xMin = userBounds.getMinX();
        double yMin = userBounds.getMinY();
        double xMax = userBounds.getMaxX();
        double yMax = userBounds.getMaxY();
        double xyWidth = xMax - xMin;
        double xyHeight = yMax - yMin;
        double scaleX;
        double scaleY;
        double xOff;
        double yOff;
        if (keepAspectRatio)
        {
            double xyAspect = xyWidth / xyHeight;
            if (aspect > xyAspect)
            {
                scaleX = scaleY = height / xyHeight;
                xOff = -scaleY*xMin + (width - scaleY*xyWidth) / 2.0;
                yOff = scaleY*yMin + height;
            }
            else
            {
                scaleX = scaleY = width / xyWidth;
                xOff = -scaleY*xMin;
                yOff = scaleY*yMin + height / 2.0 + scaleY*xyHeight / 2.0;
            }
        }
        else
        {
            scaleX = width / xyWidth;
            scaleY = height / xyHeight;
            xOff = -scaleX*xMin;
            yOff = scaleY*yMin + height / 2.0 + scaleY*xyHeight / 2.0;
        }
        transform.setTransform(scaleX, 0, 0, -scaleY, xOff, yOff);
        return transform;
    }
    /**
     * Creates DoubleTransformer for AffineTransform
     * @param transform
     * @return 
     */
    public static DoubleTransformer affineTransformer(AffineTransform transform)
    {
        return new AffineTransformer(transform);
    }
    /**
     * Creates Inverse DoubleTransformer for AffineTransform
     * @param transform
     * @return 
     */
    public static DoubleTransformer affineInverseTransformer(AffineTransform transform)
    {
        return new AffineInverseTransformer(transform);
    }
    public static class AffineTransformer implements DoubleTransformer
    {
        private AffineTransform transform;
        private Point2D.Double src = new Point2D.Double();
        private Point2D.Double dst = new Point2D.Double();

        public AffineTransformer(AffineTransform transform)
        {
            this.transform = transform;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            src.setLocation(x, y);
            transform.transform(src, dst);
            term.accept(dst.x, dst.y);
        }
        
    }
    public static class AffineInverseTransformer implements DoubleTransformer
    {
        private AffineTransform transform;
        private Point2D.Double src = new Point2D.Double();
        private Point2D.Double dst = new Point2D.Double();

        public AffineInverseTransformer(AffineTransform transform)
        {
            this.transform = transform;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            src.setLocation(x, y);
            try
            {
                transform.inverseTransform(src, dst);
            }
            catch (NoninvertibleTransformException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            term.accept(dst.x, dst.y);
        }
        
    }
}
