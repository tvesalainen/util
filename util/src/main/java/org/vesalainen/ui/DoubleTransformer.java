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
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface DoubleTransformer
{
    /**
     * Transforms x and y to term
     * @param x
     * @param y
     * @param term 
     */
    void transform(double x, double y, DoubleBiConsumer term);
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
     * Creates new DoubleTransformer which first calls this transform and
     * then next.
     * @param next
     * @return 
     */
    default DoubleTransformer andThen(DoubleTransformer next)
    {
        return (x,y,n)->transform(x,y, (xx,yy)->next.transform(xx, yy, n));
    }
    /**
     * Returns identity transformer
     * @return 
     */
    static DoubleTransformer identity()
    {
        return (x,y,n)->n.accept(x, y);
    }
    /**
     * Returns parameter swap transformer
     * @return 
     */
    static DoubleTransformer swap()
    {
        return (x,y,n)->n.accept(y, x);
    }
}
