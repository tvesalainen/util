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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateScale extends SerialScale
{
    public static final Scale LATITUDE = new LatitudeScale();
    public static final Scale LONGITUDE = new LongitudeScale();
    private static final double MINUTE = 1.0/60.0;
    private static final String DEGREE_FORMAT = "%.0f\u00B0";
    
    private char[] sign;
    
    protected CoordinateScale(char... sign)
    {
        this.sign = sign;
        
        addTail(new MinuteScale(1));
    }

    public static class LatitudeScale extends CoordinateScale
    {

        public LatitudeScale()
        {
            super('N', 'S');
            addScaleLevel(new DegreeScaleLevel(90));
            addScaleLevel(new DegreeScaleLevel(30));
            addScaleLevel(new DegreeScaleLevel(10));
            addScaleLevel(new DegreeScaleLevel(5));
            addScaleLevel(new DegreeScaleLevel(1));
        }
        
    }
    public static class LongitudeScale extends CoordinateScale
    {

        public LongitudeScale()
        {
            super('E', 'W');
            addScaleLevel(new DegreeScaleLevel(180));
            addScaleLevel(new DegreeScaleLevel(90));
            addScaleLevel(new DegreeScaleLevel(30));
            addScaleLevel(new DegreeScaleLevel(10));
            addScaleLevel(new DegreeScaleLevel(5));
            addScaleLevel(new DegreeScaleLevel(1));
        }
        
    }
    private class DegreeScaleLevel extends AbstractScaleLevel
    {
        
        public DegreeScaleLevel(int step)
        {
            super(step, null);
        }

        @Override
        public void format(Formatter formatter, double value)
        {
            if (value >= 0)
            {
                formatter.format("%c", sign[0]);
            }
            else
            {
                formatter.format("%c", sign[1]);
            }
            formatter.format(DEGREE_FORMAT, Math.floor(Math.abs(value)));
        }
        
    }
    private class MinuteScale extends BasicScale
    {

        public MinuteScale(double mul)
        {
            super(MINUTE*mul, "'");
            setMaxDelta(1);
        }

        @Override
        protected void format(Formatter formatter, double value, AbstractScaleLevel caller)
        {
            if (value >= 0)
            {
                formatter.format("%c", sign[0]);
            }
            else
            {
                formatter.format("%c", sign[1]);
            }
            double abs = Math.abs(value);
            double deg = Math.floor(abs);
            formatter.format(DEGREE_FORMAT, deg);
            double min = 60.0*(abs-deg);
            caller.format(formatter, min);
        }

    }
}
