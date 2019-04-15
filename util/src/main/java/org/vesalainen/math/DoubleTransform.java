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

import java.awt.geom.Point2D;
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
    default DoubleTransform inverse()
    {
        throw new UnsupportedOperationException("inverse not implemented");
    }
    /**
     * Creates numerical derivative. It is recommended to override this if
 derivative is known.
     * @return 
     */
    default DoubleTransform derivative()
    {
        return MoreMath.derivative(this);
    }
    /**
     * Transforms src to dst and returns dst. Dst can be null in which case 
     * new Point2D.Double is created.
     * @param src
     * @param dst
     * @return 
     */
    default Point2D transform(Point2D src, Point2D dst)
    {
        if (dst == null)
        {
            dst = new Point2D.Double();
        }
        transform(src.getX(), src.getY(), dst::setLocation);
        return dst;
    }
    /**
     * Transforms points from src to dst
     * @param tmp Temporary. If null will be allocated.
     * @param dst
     * @param src 
     */
    default void transform(Point2D tmp, double[] src, double[] dst, int len)
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
    static final ThreadLocal<Point2D.Double> PNT1 = ThreadLocal.withInitial(Point2D.Double::new);
    static final ThreadLocal<Point2D.Double> PNT2 = ThreadLocal.withInitial(Point2D.Double::new);
    /**
     * Creates new DoubleTransform which first calls this transform
     * then next and multiplies result.
     * @param next
     * @return 
     */
    default DoubleTransform andThenMultiply(DoubleTransform next)
    {
        return (x,y,n)->
        {
            Point2D.Double p1 = PNT1.get();
            Point2D.Double p2 = PNT2.get();
            transform(x,y,p1::setLocation);
            next.transform(x,y,p2::setLocation);
            n.accept(p1.x*p2.x, p1.y*p2.y);
        };
    }
    /**
     * Returns identity transformer
     * @return 
     */
    public static DoubleTransform identity()
    {
        return new MultiplyTransform();
    }
    /**
     * Returns parameter swap transformer
     * @return 
     */
    public static DoubleTransform swap()
    {
        return new SwapTransform();
    }
    /**
     * Returns f1(f2(f3(...)))
     * @param fx
     * @return 
     */
    public static DoubleTransform chain(DoubleTransform... fx)
    {
        if (fx.length < 2)
        {
            throw new IllegalArgumentException("too few arguments");
        }
        DoubleTransform f = fx[0];
        for (int ii=1;ii<fx.length;ii++)
        {
            f = new ChainTransform(f, fx[ii]);
        }
        return f;
    }
    /**
     * return DoubleTransform having separate functions for x and y.
     * @param fx
     * @param fy
     * @return 
     */
    public static DoubleTransform composite(MathFunction fx, MathFunction fy)
    {
        return new CompositeTransform(fx, fy);
    }
    public static class CompositeTransform implements DoubleTransform
    {
        private final MathFunction fx;
        private final MathFunction fy;

        public CompositeTransform(MathFunction fx, MathFunction fy)
        {
            this.fx = fx;
            this.fy = fy;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            term.accept(fx.applyAsDouble(x), fy.applyAsDouble(y));
        }

        @Override
        public DoubleTransform inverse()
        {
            return (x,y,c)->c.accept(fx.inverse().applyAsDouble(x), fy.inverse().applyAsDouble(y));
        }

        @Override
        public DoubleTransform derivative()
        {
            return (x,y,c)->c.accept(fx.derivative().applyAsDouble(x), fy.derivative().applyAsDouble(y));
        }
        
    }
    public static class ChainTransform implements DoubleTransform
    {
        private final DoubleTransform f;
        private final DoubleTransform g;

        public ChainTransform(DoubleTransform f, DoubleTransform g)
        {
            this.f = f;
            this.g = g;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            g.andThen(f).transform(x, y, term);
        }

        @Override
        public DoubleTransform inverse()
        {
            return f.inverse().andThen(g.inverse());
        }

        @Override
        public DoubleTransform derivative()
        {
            return g.andThen(f.derivative()).andThenMultiply(g.derivative());
        }
        
    }
    public static class MultiplyTransform implements DoubleTransform
    {
        private final double cx;
        private final double cy;

        public MultiplyTransform()
        {
            this(1, 1);
        }

        public MultiplyTransform(double cx, double cy)
        {
            this.cx = cx;
            this.cy = cy;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            term.accept(cx*x, cy*y);
        }

        @Override
        public DoubleTransform inverse()
        {
            return (x,y,c)->c.accept(x/cx, y/cy);
        }

        @Override
        public DoubleTransform derivative()
        {
            return (x,y,c)->c.accept(cx, cy);
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
        public DoubleTransform inverse()
        {
            return this;
        }

        @Override
        public DoubleTransform derivative()
        {
            return (x,y,c)->c.accept(1, 1);
        }
        
    }
}
