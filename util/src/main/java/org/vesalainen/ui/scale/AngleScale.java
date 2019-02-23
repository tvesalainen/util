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
public class AngleScale extends SerialScale
{
    private static final String DEGREE_FORMAT = "%.0f\u00B0";

    public AngleScale()
    {
        addScaleLevel(new DegreeScaleLevel(360));
        addScaleLevel(new DegreeScaleLevel(180));
        addScaleLevel(new DegreeScaleLevel(90));
        addScaleLevel(new DegreeScaleLevel(30));
        addScaleLevel(new DegreeScaleLevel(10));
        addScaleLevel(new DegreeScaleLevel(5));
        addScaleLevel(new DegreeScaleLevel(1));
        
        addTail(new BasicScale(1, "\u00B0").setMaxDelta(0.1));
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
            formatter.format(DEGREE_FORMAT, Math.floor(value));
        }
        
    }
}
