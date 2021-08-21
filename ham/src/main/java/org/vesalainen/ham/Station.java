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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.vesalainen.ham.jaxb.HfFaxType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Station implements TimeRange
{
    
    private StationType station;
    private List<Transmitter> transmitters;
    private List<Schedule> schedules;
    private Map<String, MapArea> maps;
    private TimeRange dateRanges;
    private Location location;

    public Station(StationType station)
    {
        this.station = station;
        this.transmitters = station.getTransmitter().stream().map((org.vesalainen.ham.jaxb.TransmitterType t) -> new Transmitter(this, t)).collect(Collectors.toList());
        this.schedules = station.getHfFax().stream().map((HfFaxType s) -> new HfFax(this, s)).collect(Collectors.toList());
        this.maps = station.getMap().stream().map((org.vesalainen.ham.jaxb.MapType m) -> new MapArea(this, m)).collect(Collectors.toMap((MapArea m) -> m.getName(), (org.vesalainen.ham.MapArea m) -> m));
        this.dateRanges = TimeRanges.orDateRanges(station.getDate());
        this.location = LocationParser.parse(station.getLocation());
    }

    /**
     * Returns true if location is inside named map.
     * @param name
     * @param location
     * @return
     */
    public boolean inMap(String name, Location location)
    {
        MapArea map = maps.get(name);
        if (map == null)
        {
            throw new IllegalArgumentException(name);
        }
        return map.isInside(location);
    }

    /**
     * Location is inside one of the maps.
     * @param location
     * @return
     */
    public boolean inAnyMap(Location location)
    {
        return maps.values().stream().anyMatch((org.vesalainen.ham.MapArea m) -> m.isInside(location));
    }

    public List<Transmitter> getTransmitters()
    {
        return transmitters;
    }

    public List<Schedule> getSchedules()
    {
        return schedules;
    }

    public Map<String, MapArea> getMaps()
    {
        return maps;
    }

    @Override
    public boolean isInRange(OffsetDateTime dateTime)
    {
        return dateRanges.isInRange(dateTime);
    }

    public String getName()
    {
        return station.getName();
    }

    public String getInfo()
    {
        return station.getInfo();
    }

    public boolean isActive()
    {
        return station.isActive();
    }

    public Location getLocation()
    {
        return location;
    }
    
}
