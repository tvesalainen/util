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
package org.vesalainen.ham;

import org.vesalainen.ham.jaxb.HfFaxType;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HfFax extends Schedule<HfFaxType>
{
    
    public HfFax(Station station, HfFaxType schedule)
    {
        super(station, schedule);
    }

    @Override
    public TypeOfTransmittedInformation getTypeOfTransmittedInformation()
    {
        return TypeOfTransmittedInformation.C;
    }
    /**
     * Returns true if location is inside map.
     * @param location
     * @return
     */
    public boolean inMap(Location location)
    {
        for (String map : schedule.getMap())
        {
            if (station.inMap(map, location))
            {
                return true;
            }
        }
        return false;
    }

    public int getRpm()
    {
        return schedule.getRpm();
    }

    public int getIoc()
    {
        return schedule.getIoc();
    }
    
}
