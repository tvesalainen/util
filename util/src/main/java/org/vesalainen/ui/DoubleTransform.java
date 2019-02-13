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

import java.awt.geom.Point2D;
import org.vesalainen.util.concurrent.ThreadTemporal;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface DoubleTransform
{
    /**
     * Transforms x and y to term
     * @param x
     * @param y
     * @param term 
     */
    void transform(double x, double y, DoubleBiConsumer term);
    /**
     * Creates numerical derivate. It is recommended to override this if
     * derivate is known.
     * @return 
     */
    default DoubleTransform derivate()
    {
        return (x,y,c)->
        {
            double d = x*0.01;
            Point2D.Double p1 = ThreadTemporal.tmp1.get();
            Point2D.Double p2 = ThreadTemporal.tmp2.get();
            transform(x,y,p1::setLocation);
            transform(x+d,y+d,p2::setLocation);
            c.accept((p2.x-p1.x)/d, (p2.y-p1.y)/d);
        };
    }
    /**
     * Transforms points from src to dst
     * @param tmp Temporary. If null will be allocated.
     * @param dst
     * @param src 
     */
    default void transform(Point2D.Double tmp, double[] src, double[] dst, int len)
    {
        if (dst.length < src.length || src.length%2 != 0 || len > src.length/2)
        {
            throw new IllegalArgumentException();
        }
        if (tmp == null)
        {
            tmp = new Point2D.Double();
        }
        for (int ii=0;ii<len;ii++)
        {
            tmp.setLocation(src[2*ii], src[2*ii+1]);
            transform(tmp.getX(), tmp.getY(), tmp::setLocation);
            dst[2*ii] = tmp.getX();
            dst[2*ii+1] = tmp.getY();
        }
    }
    /**
     * Creates new DoubleTransform which first calls this transform and
     * then next.
     * @param next
     * @return 
     */
    default DoubleTransform andThen(DoubleTransform next)
    {
        return (x,y,n)->transform(x,y, (xx,yy)->next.transform(xx, yy, n));
    }
    /**
     * Creates new DoubleTransform which first calls this transform
     * then next multiplies result.
     * @param next
     * @return 
     */
    default DoubleTransform andThenMultiply(DoubleTransform next)
    {
        return (x,y,n)->
        {
            Point2D.Double p1 = ThreadTemporal.tmp1.get();
            Point2D.Double p2 = ThreadTemporal.tmp2.get();
            transform(x,y,p1::setLocation);
            next.transform(x,y,p2::setLocation);
            n.accept(p1.x*p2.x, p1.y*p2.y);
        };
    }
    /**
     * Returns identity transformer
     * @return 
     */
    static DoubleTransform identity()
    {
        return new IdentityTransform();
    }
    /**
     * Returns parameter swap transformer
     * @return 
     */
    static DoubleTransform swap()
    {
        return new SwapTransform();
    }
    public static class IdentityTransform implements DoubleTransform
    {

        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            term.accept(x, y);
        }

        @Override
        public DoubleTransform derivate()
        {
            return (x,y,c)->c.accept(1, 1);
        }
        
    }
    public static class SwapTransform implements DoubleTransform
    {

        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            term.accept(y, x);
        }

        @Override
        public DoubleTransform derivate()
        {
            return (x,y,c)->c.accept(1, 1);
        }
        
    }
}
