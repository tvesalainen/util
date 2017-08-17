/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.util.Recyclable;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleWayPoint implements WayPoint, Recyclable
{
    private long time;
    private double latitude;
    private double longitude;

    @Override
    public long getTime()
    {
        assert !isRecycled();
        return time;
    }

    public void setTime(long time)
    {
        assert !isRecycled();
        this.time = time;
    }

    @Override
    public double getLatitude()
    {
        assert !isRecycled();
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        assert !isRecycled();
        this.latitude = latitude;
    }

    @Override
    public double getLongitude()
    {
        assert !isRecycled();
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        assert !isRecycled();
        this.longitude = longitude;
    }

    @Override
    public void clear()
    {
        time = 0;
        latitude = 0;
        longitude = 0;
    }
    
}
