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

import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.navi.Area;
import org.vesalainen.util.Lists;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MapArea
{
    
    private Station station;
    private MapType map;
    private Area area;

    public MapArea(Station station, MapType map)
    {
        this.station = station;
        this.map = map;
        List<Location> list = map.getCorners().stream().map(LocationParser::parse).collect(Collectors.toList());
        this.area = Area.getArea(Lists.toArray(list, Location.class));
    }

    public boolean isInside(Location location)
    {
        return area.isInside(location);
    }

    public Station getStation()
    {
        return station;
    }

    public String getName()
    {
        return map.getName();
    }

    public String getProjection()
    {
        return map.getProjection();
    }
    
}
