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

import java.util.function.LongSupplier;
import org.vesalainen.math.CosineFitter;
import org.vesalainen.math.matrix.ReadableDoubleMatrix;
import org.vesalainen.math.sliding.DoubleAbstractTimeoutSliding;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TideFitter extends CosineFitter
{
    private Data data;

    public TideFitter(LongSupplier clock)
    {
        super(new Data(clock, 1024, Tide.PERIOD));
        this.data = (Data) points;
    }
    
    public void add(long time, double slope)
    {
        data.accept(slope, time);
    }
    
    private static class Data extends DoubleAbstractTimeoutSliding implements ReadableDoubleMatrix
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

        @Override
        public double get(int i, int j)
        {
            switch (j)
            {
                case 0:
                    return Tide.TIME_TO_RAD.applyAsDouble(getTime(i));
                case 1:
                    return getValue(i);
                default:
                    throw new IllegalArgumentException("wrong column");
            }
        }

        @Override
        public int rows()
        {
            return count();
        }

        @Override
        public int columns()
        {
            return 2;
        }
        
    }
}
