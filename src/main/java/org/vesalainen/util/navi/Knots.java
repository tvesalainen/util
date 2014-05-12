/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util.navi;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author tkv
 */
public class Knots extends Velocity
{
    public Knots(double knots)
    {
        super(Miles.NM_IN_METERS*knots/TimeUnit.HOURS.toSeconds(1));
    }
    
    @Override
    public String toString()
    {
        return String.format("%.1fKn", getKnots());
    }
    
}

