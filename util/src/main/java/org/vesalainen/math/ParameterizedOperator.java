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
public interface ParameterizedOperator
{
    void eval(double t, DoubleBiConsumer consumer);
    default ParameterizedOperator andThen(DoubleTransform transform)
    {
        return new Chain(this, transform);
    }
    default ParameterizedOperator derivative()
    {
        throw new UnsupportedOperationException("derivative not supported");
    }
    default ParameterizedOperator secondDerivative()
    {
        throw new UnsupportedOperationException("derivative not supported");
    }
    /**
     * Returns parameterized MathFunction  at range 0.0 - 1.0 
     * @param f
     * @param min
     * @param max
     * @return 
     */
    static ParameterizedOperator parameterize(MathFunction f, double min, double max)
    {
        return new Parameterized(f, min, max);
    }
    class Parameterized implements ParameterizedOperator
    {
        private MathFunction f;
        private double a;
        private double b;

        public Parameterized(MathFunction f, double min, double max)
        {
            this(max-min, min, f);
        }
        private Parameterized(double a, double b, MathFunction f)
        {
            this.f = f;
            this.a = a;
            this.b = b;
        }
        
        @Override
        public void eval(double t, DoubleBiConsumer consumer)
        {
            double x = a*t+b;
            consumer.accept(x, f.applyAsDouble(x));
        }

        @Override
        public ParameterizedOperator derivative()
        {
            return (t,c)->c.accept(a, f.derivative().applyAsDouble(a*t+b));
        }
        
    }
    static final ThreadLocal<Point2D.Double> PNT1 = ThreadLocal.withInitial(Point2D.Double::new);
    static final ThreadLocal<Point2D.Double> PNT2 = ThreadLocal.withInitial(Point2D.Double::new);
    class Chain implements ParameterizedOperator
    {
        private ParameterizedOperator p;
        private DoubleTransform f;
        private ParameterizedOperator operator;
        private ParameterizedOperator derivative;

        public Chain(ParameterizedOperator p, DoubleTransform f)
        {
            this.p = p;
            this.f = f;
        }
        
        @Override
        public void eval(double t, DoubleBiConsumer consumer)
        {
            if (operator == null)
            {
                operator = (tt,c)->p.eval(tt, (x,y)->f.transform(x, y, c));
            }
            operator.eval(t, consumer);
        }

        @Override
        public ParameterizedOperator derivative()
        {
            if (derivative == null)
            {
                DoubleTransform td = f.derivative();
                ParameterizedOperator d = p.derivative();
                derivative = (t,c)->
                {
                    Point2D.Double p1 = PNT1.get();
                    Point2D.Double p2 = PNT2.get();
                    p.eval(t, (x,y)->td.transform(x, y, p1::setLocation));
                    d.eval(t, p2::setLocation);
                    c.accept(p1.x*p2.x, p1.y*p2.y);
                };
            }
            return derivative;
        }
        
    }
}
