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
package org.vesalainen.navi.cpa;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleCourse implements Course
{
    private double latitude;
    private double longitude;
    private double speed;
    private double bearing;

    public SimpleCourse(double latitude, double longitude, double speed, double bearing)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.bearing = bearing;
    }

    @Override
    public double getLatitude()
    {
        return latitude;
    }

    @Override
    public double getLongitude()
    {
        return longitude;
    }

    @Override
    public double getSpeed()
    {
        return speed;
    }

    @Override
    public double getCourse()
    {
        return bearing;
    }
    
}
