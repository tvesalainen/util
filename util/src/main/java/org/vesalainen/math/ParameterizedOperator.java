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
import java.util.function.DoubleUnaryOperator;
import static org.vesalainen.math.MoreMath.SQRT_EPSILON;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;
import org.vesalainen.math.matrix.DoubleUnaryMatrix;
import org.vesalainen.util.function.DoubleBiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface ParameterizedOperator
{
    /**
     * Calculates (x, y)
     * @param t
     * @param consumer 
     */
    void calc(double t, DoubleBiConsumer consumer);
    /**
     * Calculates x
     * @param t
     * @return 
     */
    default double calcX(double t)
    {
        Point2D.Double p = new Point2D.Double();
        calc(t, p::setLocation);
        return p.x;
    }
    /**
     * Calculates y
     * @param t
     * @return 
     */
    default double calcY(double t)
    {
        Point2D.Double p = new Point2D.Double();
        calc(t, p::setLocation);
        return p.y;
    }
    /**
     * Creates ParameterizedOperator that first calls this and with result calls
     * transform.
     * @param transform
     * @return 
     */
    default ParameterizedOperator andThen(DoubleTransform transform)
    {
        return new Chain(this, transform);
    }
    /**
     * Returns derivative vector
     * @return 
     */
    default DoubleUnaryMatrix derivative()
    {
        throw new UnsupportedOperationException("derivative not supported");
    }
    /**
     * Returns derivative vector of second derivative
     * @return 
     */
    default DoubleUnaryMatrix secondDerivative()
    {
        throw new UnsupportedOperationException("derivative not supported");
    }
    /**
     * Returns gradients magnitude.
     * @return 
     */
    default DoubleUnaryOperator hypot()
    {
        throw new UnsupportedOperationException("derivative not supported");
    }
    /**
     * Tries to find y for x with some precision.
     * <p>If y=f(x) is not injection the result will be unpredictable!
     * @param x
     * @return 
     */
    default double evalY(double x)
    {
        return evalY(x, x != 0.0 ? SQRT_EPSILON*x : SQRT_EPSILON);
    }
    /**
     * Tries to find y for x with deltaX precision.
     * <p>If y=f(x) is not injection the result will be unpredictable!
     * @param x
     * @param deltaX
     * @return 
     */
    default double evalY(double x, double deltaX)
    {
        double t = evalTForX(x, deltaX);
        return calcY(t);
    }
    /**
     * Tries to find x for y with some precision.
     * <p>If x=f(y) is not injection the result will be unpredictable!
     * @param y
     * @return 
     */
    default double evalX(double y)
    {
        return evalX(y, y != 0.0 ? SQRT_EPSILON*y : SQRT_EPSILON);
    }
    /**
     * Tries to find x for y with deltaY precision.
     * <p>If x=f(y) is not injection the result will be unpredictable!
     * @param y
     * @param deltaY
     * @return 
     */
    default double evalX(double y, double deltaY)
    {
        double t = evalTForY(y, deltaY);
        return calcX(t);
    }
    /**
     * Tries to find t for y with some precision.
     * <p>If x=f(y) is not injection the result will be unpredictable!
     * @param y
     * @return 
     */
    default double evalTForY(double y)
    {
        return evalTForY(y, y != 0.0 ? SQRT_EPSILON*y : SQRT_EPSILON);
    }
    /**
     * Tries to find t for y with deltaY precision.
     * <p>If x=f(y) is not injection the result will be unpredictable!
     * @param y
     * @param deltaY
     * @return 
     */
    default double evalTForY(double y, double deltaY)
    {
        throw new UnsupportedOperationException();
    }
    /**
     * Tries to find t for x with some precision.
     * <p>If y=f(x) is not injection the result will be unpredictable!
     * @param x
     * @return 
     */
    default double evalTForX(double x)
    {
        return evalTForX(x, x != 0.0 ? SQRT_EPSILON*x : SQRT_EPSILON);
    }
    /**
     * Tries to find t for x with deltaX precision.
     * <p>If y=f(x) is not injection the result will be unpredictable!
     * @param x
     * @param deltaX
     * @return 
     */
    default double evalTForX(double x, double deltaX)
    {
        throw new UnsupportedOperationException();
    }
    static final ThreadLocal<Point2D.Double> PNT1 = ThreadLocal.withInitial(Point2D.Double::new);
    static final ThreadLocal<Point2D.Double> PNT2 = ThreadLocal.withInitial(Point2D.Double::new);
    class Chain implements ParameterizedOperator
    {
        private ParameterizedOperator p;
        private DoubleTransform f;
        private ParameterizedOperator operator;
        private DoubleUnaryMatrix derivative;

        public Chain(ParameterizedOperator p, DoubleTransform f)
        {
            this.p = p;
            this.f = f;
        }
        
        @Override
        public void calc(double t, DoubleBiConsumer consumer)
        {
            if (operator == null)
            {
                operator = (tt,c)->p.calc(tt, (x,y)->f.transform(x, y, c));
            }
            operator.calc(t, consumer);
        }

        @Override
        public double calcX(double t)
        {
            return f.evalX(p.calcX(t), p.calcY(t));
        }

        @Override
        public double calcY(double t)
        {
            return f.evalY(p.calcX(t), p.calcY(t));
        }

        public DoubleUnaryOperator hypot()
        {
            DoubleBinaryMatrix J = f.gradient();
            DoubleUnaryMatrix d = p.derivative();
            return (t)->
            {
                double x = p.calcX(t);
                double y = p.calcY(t);
                double det = J.determinant().applyAsDouble(x, y);
                double dx = d.eval(0, 0, t);
                double dy = d.eval(1, 0, t);
                double Jfxdx = J.eval(0, 0, x, y);
                double Jfxdy = J.eval(0, 1, x, y);
                double Jfydx = J.eval(1, 0, x, y);
                double Jfydy = J.eval(1, 1, x, y);
                return Math.hypot(
                        dx*Jfxdx+dy*Jfxdy, 
                        dx*Jfydx+dy*Jfydy
                );
            };
                  
        }
        @Override
        public DoubleUnaryMatrix derivative()
        {
            if (derivative == null)
            {
                DoubleBinaryMatrix J = f.gradient();
                DoubleUnaryMatrix d = p.derivative();
                derivative = new DoubleUnaryMatrix(2,
                        (t)->
                        {
                            double x = p.calcX(t);
                            double y = p.calcY(t);
                            double dx = d.eval(0, 0, t);
                            double dy = d.eval(1, 0, t);
                            double Jfxdx = J.eval(0, 0, x, y);
                            double Jfxdy = J.eval(0, 1, x, y);
                            return dx*Jfxdx+dy*Jfxdy;
                        },
                        (t)->
                        {
                            double x = p.calcX(t);
                            double y = p.calcY(t);
                            double dx = d.eval(0, 0, t);
                            double dy = d.eval(1, 0, t);
                            double Jfydy = J.eval(1, 1, x, y);
                            double Jfydx = J.eval(1, 0, x, y);
                            return dx*Jfydx+dy*Jfydy;
                        }
                );
            }
            return derivative;
        }
        
    }
}
