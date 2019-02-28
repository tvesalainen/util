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
package org.vesalainen.ui.scale;

import java.util.Formatter;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import org.vesalainen.text.Unicodes;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Log10Scale implements Scale
{

    @Override
    public Iterator<ScaleLevel> iterator(double min, double max)
    {
        int majorExponent = (int) Math.ceil(Math.log10(min));
        int minorExponent = 0;
        if (Math.pow(10, majorExponent) > max)
        {
            minorExponent = majorExponent -(int) Math.floor(Math.log10(max-min));
        }
        return new LevelIterator(majorExponent, minorExponent);
    }

    @Override
    public Iterator<ScaleLevel> iterator(double delta)
    {
        throw new UnsupportedOperationException("Use iterator(min, max)");
    }
    private class LevelIterator implements Iterator<ScaleLevel> 
    {
        private int majorExponent;
        private int minorExponent;

        public LevelIterator(int majorExponent, int minorExponent)
        {
            this.majorExponent = majorExponent;
            this.minorExponent = minorExponent;
        }
        
        @Override
        public boolean hasNext()
        {
            return true;
        }

        @Override
        public ScaleLevel next()
        {
            return new Log10ScaleLevel(majorExponent, minorExponent++);
        }
        
    }
    private class Log10ScaleLevel extends AbstractScaleLevel
    {
        private int majorExponent;
        private int minorExponent;

        public Log10ScaleLevel(int majorExponent, int minorExponent)
        {
            super(-minorExponent, null);
            this.majorExponent = majorExponent;
            this.minorExponent = minorExponent;
        }

        @Override
        protected void format(Formatter formatter, double value)
        {
            int exp = (int) Math.floor(Math.log10(value));
            int coeff = (int) Math.floor(value/Math.pow(10, exp));
            if (coeff != 1)
            {
                formatter.format("%dx", coeff);
            }
            formatter.format("10");
            StringBuilder out = (StringBuilder) formatter.out();
            Unicodes.toSuperScript(String.valueOf(exp), out);
        }
        
        @Override
        public PrimitiveIterator.OfDouble iterator(double min, double max)
        {
            return new MarkerIterator(majorExponent, minorExponent, min, max);
        }
        
    }
    private class MarkerIterator implements PrimitiveIterator.OfDouble
    {
        private int majorExponent;
        private int minorExponent;
        private final double min;
        private final double max;
        private double next;
        private double step;
        private double limit;

        public MarkerIterator(int majorExponent, int minorExponent, double min, double max)
        {
            this.majorExponent = majorExponent;
            this.minorExponent = minorExponent;
            this.min = min;
            this.max = max;
            limit = Math.pow(10, majorExponent);
            step = Math.pow(10, majorExponent-minorExponent);
            next = Math.ceil(min/step)*step;
        }

        @Override
        public double nextDouble()
        {
            double res = next;
            next += step;
            if (next >= limit)
            {
                limit *= 10;
                step *= 10;
                next = step;
            }
            return res;
        }

        @Override
        public boolean hasNext()
        {
            return next <= max;
        }
        
    }
            
}
