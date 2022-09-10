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

import org.vesalainen.math.sliding.AbstractSliding;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LastLocations extends AbstractSliding
{
    private double[] latitude;
    private double[] longitude;
    private double limit;   // NM
    private double lastLat;
    private double lastLon;
    
    public LastLocations(int initialSize, double limit)
    {
        this.initialSize = initialSize;
        this.limit = limit;
        if (Integer.bitCount(initialSize) == 1)
        {
            this.size = initialSize;
        }
        else
        {
            this.size = 2*Integer.highestOneBit(initialSize);
        }
        latitude = new double[size];
        longitude = new double[size];
    }
    
    public void accept(double lat, double lon)
    {
        writeLock.lock();
        lastLat = lat;
        lastLon = lon;
        try
        {
            eliminate();
            int count = count();
            if (count >= size)
            {
                grow();
            }
            assign(endMod(), lat, lon);
            endIncr();
        }
        finally
        {
            writeLock.unlock();
        }
    }
            
    protected void assign(int index, double lat, double lon)
    {
        latitude[index] = lat;
        longitude[index] = lon;
    }

    @Override
    protected void remove(int index)
    {
    }

    @Override
    protected void grow()
    {
        int newSize = newSize();
        latitude = (double[]) newArray(latitude, size, new double[newSize]);
        longitude = (double[]) newArray(longitude, size, new double[newSize]);
        size = newSize;
    }

    @Override
    protected boolean isRemovable(int index)
    {
        return Navis.distance(lastLat, lastLon, latitude[index], longitude[index]) > limit;
    }
    
}
