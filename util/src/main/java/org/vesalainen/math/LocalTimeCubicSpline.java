/*
 * Copyright (C) 2025 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.LocalTime;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocalTimeCubicSpline extends PolarCubicSpline
{
    private final static double MAX = LocalTime.MAX.toNanoOfDay();
    private LocalTimeCubicSpline(double... points)
    {
        super(points);
    }

    public double applyAsDouble(LocalTime time)
    {
        return super.applyAsDouble(toDegree(time));
    }
    
    private static double toDegree(LocalTime time)
    {
        return 360*time.toNanoOfDay()/MAX;
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    public static class Builder
    {
        private NavigableMap<LocalTime,Double> map = new TreeMap<>();
        
        public Builder add(LocalTime time, double value)
        {
            map.put(time, value);
            return this;
        }
        public LocalTimeCubicSpline build()
        {
            double[] arr = new double[map.size()*2];
            final AtomicInteger ii = new AtomicInteger();
            map.forEach((t, d)->
            {
                arr[ii.getAndIncrement()] = toDegree(t);
                arr[ii.getAndIncrement()] = d;
            });
            return new LocalTimeCubicSpline(arr);
        }
    }
}
