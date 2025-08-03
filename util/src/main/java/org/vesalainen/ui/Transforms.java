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
import java.awt.geom.Rectangle2D;
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Transforms
{

    /**
     * Creates translation that translates userBounds in Cartesian coordinates 
     * to screenBound in screen coordinates.
     * <p>In Cartesian y grows up while in screen y grows down
     * @param userBounds
     * @param screenBounds
     * @param keepAspectRatio 
     * @return 
     */
    public static AffineTransform createScreenTransform(Rectangle2D userBounds, Rectangle2D screenBounds, boolean keepAspectRatio)
    {
        return createScreenTransform(userBounds, screenBounds, keepAspectRatio, new AffineTransform());
    }
    /**
     * Creates translation that translates userBounds in Cartesian coordinates 
     * to screenBound in screen coordinates.
     * <p>In Cartesian y grows up while in screen y grows down
     * @param userBounds
     * @param screenBounds
     * @param keepAspectRatio
     * @param transform 
     * @return Returns parameter transform
     */
    public static AffineTransform createScreenTransform(Rectangle2D userBounds, Rectangle2D screenBounds, boolean keepAspectRatio, AffineTransform transform)
    {
        createScreenTransform(
                userBounds, 
                screenBounds, 
                keepAspectRatio, 
                (double mxx, double mxy, double tx, double myx, double myy, double ty)->
                {
                    transform.setTransform(mxx, mxy, tx, myx, myy, ty);
                });
        return transform;
    }
    public static void createScreenTransform(Rectangle2D userBounds, Rectangle2D screenBounds, boolean keepAspectRatio, Consumer2D transform)
    {
        createScreenTransform(
                screenBounds.getWidth(),
                screenBounds.getHeight(),
                userBounds.getMinX(),
                userBounds.getMinY(),
                userBounds.getMaxX(),
                userBounds.getMaxY(),
                keepAspectRatio, 
                transform
        );
    }
    public static void createScreenTransform(
            double width,
            double height,
            double xMin,
            double yMin,
            double xMax,
            double yMax,
            boolean keepAspectRatio, 
            Consumer2D transform)
    {
        double aspect = width / height;
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
        transform.accept(scaleX, 0, 0, -scaleY, xOff, yOff);
    }
    /**
     * Creates DoubleTransform for AffineTransform.
     * @param transform
     * @return 
     */
    public static DoubleTransform affineTransform(AffineTransform transform)
    {
        return new AffineDoubleTransform(transform);
    }
    /**
     * Creates Inverse DoubleTransform for AffineTransform. 
     * @param transform
     * @return 
     * @throws java.awt.geom.NoninvertibleTransformException 
     */
    public static DoubleTransform affineInverseTransform(AffineTransform transform) throws NoninvertibleTransformException
    {
        return new AffineInverseTransform(transform);
    }
    public static class AffineDoubleTransform implements DoubleTransform
    {
        private final double m00;
        private final double m01;
        private final double m02;
        private final double m10;
        private final double m11;
        private final double m12;
        
        public AffineDoubleTransform(AffineTransform transform)
        {
            this.m00 = transform.getScaleX();
            this.m01 = transform.getShearX();
            this.m02 = transform.getTranslateX();
            this.m11 = transform.getScaleY();
            this.m10 = transform.getShearY();
            this.m12 = transform.getTranslateY();
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            term.accept(m00*x+m01*y+m02, m10*x+m11*y+m12);
        }

        @Override
        public DoubleTransform inverse()
        {
            try
            {
                return new AffineInverseTransform(new AffineTransform(m00, m10, m01, m11, m02, m12));
            }
            catch (NoninvertibleTransformException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public DoubleBinaryOperator fx()
        {
            return (x,y)->m00*x+m01*y+m02;
        }

        @Override
        public DoubleBinaryOperator fy()
        {
            return (x,y)->m10*x+m11*y+m12;
        }

        @Override
        public DoubleBinaryMatrix gradient()
        {
            return DoubleBinaryMatrix.getInstance(2, m00, m01, m10, m11);
        }

    }
    public static class AffineInverseTransform extends AffineDoubleTransform
    {
        public AffineInverseTransform(AffineTransform transform) throws NoninvertibleTransformException
        {
            super(transform.createInverse());
        }
        
    }
    @FunctionalInterface
    public interface Consumer2D
    {
        void accept(double mxx, double mxy, double myx, double myy, double tx, double ty);
        default void consume(double[] a)
        {
            accept(a[0], a[1], a[2], a[3], a[4], a[5]);
        }
    }
}
