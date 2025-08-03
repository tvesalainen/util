/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.TimeUnit;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingMax;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingMin;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeoutLocationBounds
{
    private final double margin;
    private final DoubleTimeoutSlidingMin minLon;
    private final DoubleTimeoutSlidingMin minLat;
    private final DoubleTimeoutSlidingMax maxLon;
    private final DoubleTimeoutSlidingMax maxLat;

    public TimeoutLocationBounds(long timeout, TimeUnit unit)
    {
        this(0, timeout, unit);
    }
    
    public TimeoutLocationBounds(double margin, long timeout, TimeUnit unit)
    {
        this.margin = margin;
        this.minLon = new DoubleTimeoutSlidingMin(4, unit.toMillis(timeout));
        this.minLat = new DoubleTimeoutSlidingMin(4, unit.toMillis(timeout));
        this.maxLon = new DoubleTimeoutSlidingMax(4, unit.toMillis(timeout));
        this.maxLat = new DoubleTimeoutSlidingMax(4, unit.toMillis(timeout));
    }
    
    public void addLongitude(double longitude)
    {
        minLon.accept(longitude);
        maxLon.accept(longitude);
    }
    
    public void addLatitude(double longitude)
    {
        minLat.accept(longitude);
        maxLat.accept(longitude);
    }
    
    public double getMinLongitude()
    {
        return minLon.getBound()-margin;
    }
    
    public double getMinLatitude()
    {
        return minLat.getBound()-margin;
    }
    
    public double getMaxLongitude()
    {
        return maxLon.getBound()+margin;
    }
    
    public double getMaxLatitude()
    {
        return maxLat.getBound()+margin;
    }
    
    public double getLatitudeRange()
    {
        return (maxLat.getBound()-minLat.getBound())+2*margin;
    }
    
    public double getLongitudeRange()
    {
        return (maxLon.getBound()-minLon.getBound())+2*margin;
    }
    
    public double getCenterLatitude()
    {
        return (maxLat.getBound()+minLat.getBound())/2;
    }
    
    public double getCenterLongitude()
    {
        return (maxLon.getBound()+minLon.getBound())/2;
    }
    
}
