/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.DoubleConsumer;

/**
 * Utility for creating scales. For example if samples are between 0 - 30 the 
 * 0-level scale is 0, 10, 20, 30. 1-level 0, 1, 2, 3, .... Additionally it is
 * possible to get 5-scales by putting five=true. 
 * @author Timo Vesalainen
 */
public class Scaler
{
    private double min;
    private double max;
    private boolean updated;
    private int exp;

    public Scaler(double min, double max)
    {
        this.min = min;
        this.max = max;
        updated = true;
    }

    void calc()
    {
        if (updated)
        {
            if (max < min)
            {
                throw new IllegalArgumentException("max="+max+" < min="+min);
            }
            exp = 0;
            double delta = max-min;
            while (delta >= 10.0 || delta < 1.0)
            {
                if (delta >= 10.0)
                {
                    delta /= 10;
                    exp++;
                }
                else
                {
                    delta *= 10;
                    exp--;
                }
            }
            updated = false;
        }
    }
    /**
     * Returns iterator for markers between min and max. 0-level returns less
     * than 10.
     * @param level >= 0
     * @return 
     */
    public PrimitiveIterator.OfDouble iterator(int level)
    {
        return iterator(level, false);
    }
    /**
     * Returns iterator for markers between min and max. 0-level returns less
     * than 10.
     * @param level >= 0
     * @param five If true the step between markers is halved.
     * @return 
     */
    public PrimitiveIterator.OfDouble iterator(int level, boolean five)
    {
        calc();
        double step = Math.pow(10, exp-level);
        double np = Math.pow(10, -(exp-level));
        double begin = Math.ceil(min*np)*step;
        double end = Math.floor(max*np)*step;
        if (five)
        {
            step /= 2.0;
            if (begin-step > min)
            {
                begin -= step;
            }
            return new Iter(begin, end, step);
        }
        else
        {
            return new Iter(begin, end, step);
        }
    }
    
    public double getMin()
    {
        return min;
    }

    public void setMin(double min)
    {
        this.min = min;
        updated = true;
    }

    public double getMax()
    {
        return max;
    }

    public void setMax(double max)
    {
        this.max = max;
        updated = true;
    }

    private class Iter implements PrimitiveIterator.OfDouble
    {
        private final double begin;
        private final double end;
        private final double step;
        private double next;

        public Iter(double begin, double end, double step)
        {
            this.begin = begin;
            this.end = end;
            this.step = step;
            this.next = begin;
        }
        
        @Override
        public double nextDouble()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            double res = next;
            next += step;
            return res;
        }

        @Override
        public void forEachRemaining(DoubleConsumer action)
        {
            while (next <= end)
            {
                action.accept(next);
                next += step;
            }
        }

        @Override
        public boolean hasNext()
        {
            return next <= end;
        }

        @Override
        public Double next()
        {
            return nextDouble();
        }
        
    }
}
