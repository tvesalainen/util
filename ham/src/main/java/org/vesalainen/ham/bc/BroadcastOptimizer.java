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
package org.vesalainen.ham.bc;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetTime;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.ham.BroadcastStationsFile.Schedule;
import org.vesalainen.ham.BroadcastStationsFile.Station;
import org.vesalainen.ham.ssn.SunSpotNumber;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastOptimizer
{
    private SunSpotNumber ssn;
    private Path sunSpotNumberPath = Paths.get("prediML.txt");
    private URL BroadcastStationsPath = BroadcastStationsFile.class.getResource("/broadscast-stations.xml");
    private Path transmitterAntennaPath = Paths.get("default", "Isotrope");
    private Path receiverAntennaPath = Paths.get("default", "SWWhip.VOA");
    private BroadcastStationsFile broadcastStation;
    private NavigableMap<OffsetTime,Schedule> scheduleMap;

    public BroadcastOptimizer()
    {
    }

    private void init()
    {
        if (ssn == null)
        {
            try
            {
                ssn = new SunSpotNumber(sunSpotNumberPath);
                broadcastStation = new BroadcastStationsFile(BroadcastStationsPath);
                scheduleMap = new TreeMap<>();
                for (Station station : broadcastStation.getStations().values())
                {
                    if (station.isActive())
                    {
                        scheduleMap.putAll(station.getSchedules());
                    }
                }
            }
            catch (MalformedURLException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    public BestStation bestStation(Location myLocation, Instant instant)
    {
        
    }
    public BroadcastOptimizer setSunSpotNumberPath(Path sunSpotNumberPath)
    {
        this.sunSpotNumberPath = sunSpotNumberPath;
        return this;
    }
    public BroadcastOptimizer setTransmitterAntennaPath(Path transmitterAntennaPath)
    {
        this.transmitterAntennaPath = transmitterAntennaPath;
        return this;
    }

    public BroadcastOptimizer setReceiverAntennaPath(Path receiverAntennaPath)
    {
        this.receiverAntennaPath = receiverAntennaPath;
        return this;
    }
    

}
