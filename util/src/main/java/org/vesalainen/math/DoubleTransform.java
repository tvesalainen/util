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
import java.util.function.DoubleBinaryOperator;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
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
    default double evalX(double x, double y)
    {
        return fx().applyAsDouble(x,y);
    }
    default double evalY(double x, double y)
    {
        return fy().applyAsDouble(x,y);
    }
    /**
     * Returns x-coordinate function
     * @return 
     */
    default DoubleBinaryOperator fx()
    {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns y-coordinate function
     * @return 
     */
    default DoubleBinaryOperator fy()
    {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns Jacobian matrix
     * @return 
     */
    default DoubleBinaryMatrix gradient()
    {
        return MoreMath.gradient(this);
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
        return chain(next, this);
    }
    static final ThreadLocal<Point2D.Double> PNT1 = ThreadLocal.withInitial(Point2D.Double::new);
    static final ThreadLocal<Point2D.Double> PNT2 = ThreadLocal.withInitial(Point2D.Double::new);
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
        private DoubleBinaryMatrix gradient;

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
        public DoubleBinaryOperator fx()
        {
            return (x,y)->fx.applyAsDouble(x);
        }

        @Override
        public DoubleBinaryOperator fy()
        {
            return (x,y)->fy.applyAsDouble(y);
        }

        @Override
        public DoubleBinaryMatrix gradient()
        {
            if (gradient == null)
            {
                MathFunction dx = fx.derivative();
                MathFunction dy = fy.derivative();
                gradient = new DoubleBinaryMatrix(2, 2);
                gradient.set(0, 0, (x,y)->dx.applyAsDouble(x));
                gradient.set(1, 1, (x,y)->dy.applyAsDouble(y));
            }
            return gradient;
        }

    }
    public static class ChainTransform implements DoubleTransform
    {
        private final DoubleTransform f;
        private final DoubleTransform g;
        private DoubleBinaryMatrix gradient;


        public ChainTransform(DoubleTransform f, DoubleTransform g)
        {
            this.f = f;
            this.g = g;
        }
        
        @Override
        public void transform(double x, double y, DoubleBiConsumer term)
        {
            g.transform(x, y, (xx,yy)->f.transform(xx, yy, (xxx,yyy)->term.accept(xxx, yyy)));
        }

        @Override
        public DoubleTransform inverse()
        {
            return new ChainTransform(g.inverse(), f.inverse());
        }

        @Override
        public DoubleBinaryOperator fx()
        {
            return (x,y)->f.evalX(g.evalX(x, y), g.evalY(x, y));
        }

        @Override
        public DoubleBinaryOperator fy()
        {
            return (x,y)->f.evalY(g.evalX(x, y), g.evalY(x, y));
        }

        @Override
        public DoubleBinaryMatrix gradient()
        {
            if (gradient == null)
            {
                gradient = f.gradient().multiply(g.gradient());
            }
            return gradient;
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
        public DoubleBinaryOperator fx()
        {
            return (x,y)->cx*x;
        }

        @Override
        public DoubleBinaryOperator fy()
        {
            return (x,y)->cy*y;
        }

        @Override
        public DoubleBinaryMatrix gradient()
        {
            return DoubleBinaryMatrix.getInstance(2, cx, 0, 0, cy);
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
        public DoubleBinaryOperator fx()
        {
            return (x,y)->y;
        }

        @Override
        public DoubleBinaryOperator fy()
        {
            return (x,y)->x;
        }

        @Override
        public DoubleBinaryMatrix gradient()
        {
            return DoubleBinaryMatrix.getInstance(2, 0, 1, 1, 0);
        }

    }
}
