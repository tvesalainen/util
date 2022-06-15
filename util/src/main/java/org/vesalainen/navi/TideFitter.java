/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import static java.lang.Math.*;
import java.util.function.LongSupplier;
import org.vesalainen.math.CosineFitter;
import org.vesalainen.math.MathFunction;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;
import org.vesalainen.math.sliding.DoubleAbstractTimeoutSliding;

/**
 * TideFitter fits time slope series to create function that returns tide for
 * time
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TideFitter
{
    private static final double EPSILON = 0.1;
    private final LongSupplier clock;
    private final Data data;
    private final CosineFitter cosineFitter;
    private final Points points;
    private final Result result;
    /**
     * Creates TideFitter with given clock
     * @param clock 
     */
    public TideFitter(LongSupplier clock)
    {
        this.clock = clock;
        this.data = new Data(clock, 1024, Tide.PERIOD);
        this.points = new Points(data);
        this.result = new Result(data);
        this.cosineFitter = new CosineFitter(points, result, 1, 0);
    }
    /**
     * Adds time and slope of tide. Slope is positive if water is coming up.
     * Only values in last tide period are used in calculations.
     * @param time
     * @param slope 
     */
    public void add(long time, double slope)
    {
        data.accept(slope, time);
    }
    public boolean isValid()
    {
        return cosineFitter.getPointCount() > 2 && cosineFitter.getFinalCost() < EPSILON;
    }
    /**
     * Returns function that returns tide for time in milliseconds using latest
     * parameters
     * @return 
     */
    public MathFunction getTideFunction()
    {
        return (t)->getTide((long) t);
    }
    /**
     * Returns tide in meters now. Using given clock.
     * @return 
     */
    public double getTide()
    {
        if (isValid())
        {
            return getTide(clock.getAsLong());
        }
        else
        {
            return Double.NaN;
        }
    }
    /**
     * Returns half tide in meters for time in millis.
     * @param time
     * @return 
     */
    public double getTide(long time)    
    {
        double[] p = cosineFitter.getParams();
        return p[0]*sin(Tide.TIME_TO_RAD.applyAsDouble((long) time)+p[1]);
    }
    public double getDerivative()
    {
        if (isValid())
        {
            return getDerivative(clock.getAsLong());
        }
        else
        {
            return Double.NaN;
        }
    }
    public double getDerivative(long time)    
    {
        double[] p = cosineFitter.getParams();
        return p[0]*cos(Tide.TIME_TO_RAD.applyAsDouble((long) time)+p[1]);
    }
    /**
     * Return tide phase in degrees now
     * @return 
     */
    public double getPhaseInDegrees()
    {
        return getPhaseInDegrees(clock.getAsLong());
    }
    /**
     * Return tide phase in degrees at time
     * @param time
     * @return 
     */
    public double getPhaseInDegrees(long time)
    {
        if (isValid())
        {
            double[] p = cosineFitter.getParams();
            if (p[0] > 0)
            {
                return toDegrees(Tide.TIME_TO_RAD.applyAsDouble((long) time)+p[1])%360;
            }
            else
            {
                return toDegrees(Tide.TIME_TO_RAD.applyAsDouble((long) time)+p[1]+PI)%360;
            }
        }
        else
        {
            return Double.NaN;
        }
    }
    /**
     * Returns function that returns tide for time in milliseconds using current
     * parameters.
     * @return 
     */
    public MathFunction getTideSnapshot()
    {
        MathFunction ad = cosineFitter.getAntiderivative();
        return (t)->ad.applyAsDouble(Tide.TIME_TO_RAD.applyAsDouble((long) t));
    }
    /**
     * Coeff*sin = tide in meters
     * @return 
     */
    public double getCoefficient()
    {
        if (isValid())
        {
            return cosineFitter.getParamA();
        }
        else
        {
            return Double.NaN;
        }
    }
    /**
     * Phase in radians.
     * @return 
     */
    public double getPhase()
    {
        if (isValid())
        {
            return cosineFitter.getParamB();
        }
        else
        {
            return Double.NaN;
        }
    }

    public double fit()
    {
        return cosineFitter.fit();
    }

    public double[] getParams()
    {
        return cosineFitter.getParams();
    }

    public int getPointCount()
    {
        return cosineFitter.getPointCount();
    }

    public double getFinalCost()
    {
        if (isValid())
        {
            return cosineFitter.getFinalCost();
        }
        else
        {
            return Double.NaN;
        }
    }
    
    private class Data extends DoubleAbstractTimeoutSliding
    {

        public Data(LongSupplier clock, int initialSize, long timeout)
        {
            super(clock, initialSize, timeout, (l)->l);
        }

        @Override
        protected void addSample(double td, double value)
        {
        }

        @Override
        protected void removeSample(double xi, double yi)
        {
        }

    }
    private class Points implements ReadableDoubleMatrix
    {
        private Data data;

        public Points(Data data)
        {
            this.data = data;
        }
        
        @Override
        public double get(int i, int j)
        {
            return Tide.TIME_TO_RAD.applyAsDouble(data.getTime(i));
        }

        @Override
        public int rows()
        {
            return data.count();
        }

        @Override
        public int columns()
        {
            return 1;
        }
        
    }
    private class Result implements ReadableDoubleMatrix
    {
        private Data data;

        public Result(Data data)
        {
            this.data = data;
        }
        
        @Override
        public double get(int i, int j)
        {
            return data.getValue(i);
        }

        @Override
        public int rows()
        {
            return data.count();
        }

        @Override
        public int columns()
        {
            return 1;
        }
        
    }
}
