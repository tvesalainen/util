/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.itshfbc;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HourPrediction
{
    private int hour;
    private Object[] frequences;
    private Map<String, Object[]> attributes = new HashMap<>();

    public HourPrediction(Object[] frequences)
    {
        this.hour = ((Double)frequences[0]).intValue();
        this.frequences = frequences;
    }
    public void addAttribute(Object[] array)
    {
        attributes.put((String) array[array.length-1], array);
    }

    public int getHour()
    {
        return hour;
    }
    
}
