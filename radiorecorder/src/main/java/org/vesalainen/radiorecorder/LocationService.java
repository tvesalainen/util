package org.vesalainen.radiorecorder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.vesalainen.code.PropertySetter;
import org.vesalainen.parsers.nmea.NMEAService;
import org.vesalainen.util.navi.Location;

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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationService extends NMEAService implements PropertySetter
{
    private double latitude;
    private double longitude;
    public LocationService(String address, int port, ExecutorService executor) throws IOException
    {
        super(address, port, executor);
        addNMEAObserver(this);
    }

    public Location getLocation()
    {
        return new Location(latitude, longitude);
    }
    @Override
    public String[] getPrefixes()
    {
        return new String[]{"latitude", "longitude"};
    }

    @Override
    public void set(String property, boolean arg)
    {
    }

    @Override
    public void set(String property, byte arg)
    {
    }

    @Override
    public void set(String property, char arg)
    {
    }

    @Override
    public void set(String property, short arg)
    {
    }

    @Override
    public void set(String property, int arg)
    {
    }

    @Override
    public void set(String property, long arg)
    {
    }

    @Override
    public void set(String property, float arg)
    {
        switch (property)
        {
            case "latitude":
                latitude = arg;
                break;
            case "longitude":
                latitude = arg;
                break;
        }
    }

    @Override
    public void set(String property, double arg)
    {
    }

    @Override
    public void set(String property, Object arg)
    {
    }
    
}
