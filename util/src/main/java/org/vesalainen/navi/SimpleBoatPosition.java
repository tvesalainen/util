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
package org.vesalainen.navi;

/**
 * SimpleBoatPosition stores a position in boat. Examples are GPS, Radar, etc. SimpleBoatPosition
   is used in making coordinate transforms. 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleBoatPosition implements BoatPosition
{
    private double toSb;
    private double toPort;
    private double toBow;
    private double toStern;
    /**
     * Creates BoatPosition
     * @param toSb Meters to starboard side.
     * @param toPort Meters to port side.
     * @param toBow Meters to bow.
     * @param toStern  Meters to stern.
     */
    public SimpleBoatPosition(double toSb, double toPort, double toBow, double toStern)
    {
        this.toSb = toSb;
        this.toPort = toPort;
        this.toBow = toBow;
        this.toStern = toStern;
    }

    @Override
    public double getDimensionToStarboard()
    {
        return toSb;
    }

    @Override
    public double getDimensionToPort()
    {
        return toPort;
    }

    @Override
    public double getDimensionToBow()
    {
        return toBow;
    }

    @Override
    public double getDimensionToStern()
    {
        return toStern;
    }
    
}
