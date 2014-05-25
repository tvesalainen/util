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


/**
 *
 * @author tkv
 */
public class AverageLocation
{
    private Average x = new Average();
    private Average y = new Average();
    private int count;

    public AverageLocation()
    {
    }

    public AverageLocation(AverageLocation a)
    {
        x = new Average(a.x);
        y = new Average(a.y);
    }

    public AverageLocation(AverageLocation a1, AverageLocation a2)
    {
        x = new Average(a1.x, a2.x);
        y = new Average(a1.y, a2.y);
        count = a1.count + a2.count;
    }

    public void add(Location... location)
    {
        for (Location l : location)
        {
            add(l);
        }
    }
    public void add(Location location)
    {
        x.add(location.getX());
        y.add(location.getY());
        count++;
    }
    public Location getAverage()
    {
        return new Location(y.getAverage(), x.getAverage());
    }

    public double getDeviation()
    {
        return x.getDeviation()+y.getDeviation();
    }

    public void reset()
    {
        x.clear();
        y.clear();
        count = 0;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return getAverage().toString();
    }

}
