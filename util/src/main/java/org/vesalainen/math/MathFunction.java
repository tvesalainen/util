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

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface MathFunction extends DoubleUnaryOperator
{
    public static final MathFunction IDENTITY = new Identity();
    /**
     * Returns inverse function
     * @return 
     */
    default MathFunction inverse()
    {
        throw new UnsupportedOperationException("inverse not supported");
    }
    /**
     * Returns antiderivative function
     * @return 
     */
    default MathFunction antiderivative()
    {
        throw new UnsupportedOperationException("antiderivative not supported");
    }
    /**
     * Returns derivative
     * @return 
     */
    default MathFunction derivative()
    {
        return (double x)->
        {
            return MoreMath.derivative(this, x);
        };
    }
    /**
     * Returns identity function f(x)=x
     * @return 
     */
    static MathFunction identity()
    {
        return IDENTITY;
    }
    /**
     * Returns PreMultiplier function f(x) = g(x*multiplier)
     * @param func
     * @param multiplier
     * @return 
     */
    static MathFunction preMultiplier(MathFunction func, double multiplier)
    {
        return new PreMultiplier(func, multiplier);
    }
    /**
     * Returns PostMultiplier function f(x) = g(x)*multiplier
     * @param func
     * @param multiplier
     * @return 
     */
    static MathFunction postMultiplier(MathFunction func, double multiplier)
    {
        return new PostMultiplier(func, multiplier);
    }
    /**
     * Returns antiderivative between x1 and x2
     * @param x1
     * @param x2
     * @return 
     */
    default double integral(double x1, double x2)
    {
        try
        {
            MathFunction integral = antiderivative();
            return integral.applyAsDouble(x2)-integral.applyAsDouble(x1);
        }
        catch (UnsupportedOperationException ex)
        {
            return integral(x1, x2, 60000);
        }
    }
    /**
     * Returns numerical antiderivative between x1 and x2
     * @param x1
     * @param x2
     * @param points
     * @return 
     */
    default double integral(double x1, double x2, int points)
    {
        return MoreMath.integral(this, x1, x2, points);
    }
    /**
     * Returns arcLength function or throws UnsupportedOperationException if
     * as usual there is no arcLength function.
     * @return 
     */
    default MathFunction arcLength()
    {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns arc length between x1 and x2
     * @param x1
     * @param x2
     * @return 
     */
    default double arcLength(double x1, double x2)
    {
        try
        {
            MathFunction arcLength = arcLength();
            return arcLength.applyAsDouble(x2) - arcLength.applyAsDouble(x1);
        }
        catch (UnsupportedOperationException ex)
        {
            return MathFunction.this.arcLength(x1, x2, 60000);
        }
    }
    /**
     * Returns numerical arc length between x1 and x2
     * @param x1
     * @param x2
     * @param points
     * @return 
     */
    default double arcLength(double x1, double x2, int points)
    {
        return MoreMath.arcLength(this, x1, x2, points);
    }
    public static class PreMultiplier implements MathFunction
    {
        private MathFunction f;
        private double mul;

        public PreMultiplier(MathFunction f, double mul)
        {
            this.f = f;
            this.mul = mul;
        }

        @Override
        public MathFunction inverse()
        {
            MathFunction inverse = f.inverse();
            return (x)->inverse.applyAsDouble(x)/mul;
        }

        @Override
        public MathFunction antiderivative()
        {
            MathFunction integral = f.antiderivative();
            return (x)->integral.applyAsDouble(x*mul)/mul;
        }

        @Override
        public MathFunction derivative()
        {
            MathFunction derivate = f.derivative();
            return (x)->derivate.applyAsDouble(x*mul)*mul;
        }

        @Override
        public double applyAsDouble(double operand)
        {
            return f.applyAsDouble(operand*mul);
        }
        
    }
    public static class PostMultiplier implements MathFunction
    {
        private MathFunction f;
        private double mul;

        public PostMultiplier(MathFunction f, double mul)
        {
            this.f = f;
            this.mul = mul;
        }

        @Override
        public MathFunction inverse()
        {
            MathFunction inverse = f.inverse();
            return (x)->inverse.applyAsDouble(x/mul);
        }

        @Override
        public MathFunction antiderivative()
        {
            MathFunction integral = f.antiderivative();
            return (x)->integral.applyAsDouble(x)*mul;
        }

        @Override
        public MathFunction derivative()
        {
            MathFunction derivate = f.derivative();
            return (x)->derivate.applyAsDouble(x)*mul;
        }

        @Override
        public double applyAsDouble(double operand)
        {
            return f.applyAsDouble(operand)*mul;
        }
        
    }
    public static class Chain implements MathFunction
    {
        private MathFunction f;
        private MathFunction g;

        public Chain(MathFunction f, MathFunction g)
        {
            this.f = f;
            this.g = g;
        }
        
        @Override
        public MathFunction inverse()
        {
            MathFunction fi = f.inverse();
            MathFunction gi = g.inverse();
            return (x)->gi.applyAsDouble(fi.applyAsDouble(x));
        }

        @Override
        public MathFunction derivative()
        {
            MathFunction df = f.derivative();
            MathFunction dg = g.derivative();
            return (x)->df.applyAsDouble(g.applyAsDouble(x))*dg.applyAsDouble(x);
        }

        @Override
        public double applyAsDouble(double operand)
        {
            return f.applyAsDouble(g.applyAsDouble(operand));
        }
        
    }
    public static class Identity implements MathFunction
    {

        @Override
        public MathFunction inverse()
        {
            return (x)->x;
        }

        @Override
        public MathFunction antiderivative()
        {
            return (x)->x*x*0.5;
        }

        @Override
        public MathFunction derivative()
        {
            return (x)->1;
        }

        @Override
        public double arcLength(double x1, double x2)
        {
            return Math.hypot(x2-x1, x2-x1);
        }

        @Override
        public double applyAsDouble(double operand)
        {
            return operand;
        }
        
    }
}
