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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.ham.ClassFilter;
import org.vesalainen.ham.EmissionClass;
import org.vesalainen.ham.HfFax;
import org.vesalainen.ham.LocationFilter;
import org.vesalainen.ham.Schedule;
import org.vesalainen.ham.Schedule;
import org.vesalainen.ham.Station;
import org.vesalainen.ham.TimeFilter;
import org.vesalainen.ham.ssn.SunSpotNumber;
import org.vesalainen.util.RingIterator;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastOptimizer
{
    private SunSpotNumber ssn;
    private Path sunSpotNumberPath = Paths.get("prediML.txt");
    private URL BroadcastStationsPath = BroadcastStationsFile.class.getResource("/broadcast-stations.xml");
    private Path transmitterAntennaPath = Paths.get("default", "Isotrope");
    private Path receiverAntennaPath = Paths.get("default", "SWWhip.VOA");
    private BroadcastStationsFile broadcastStation;
    private NavigableMap<OffsetTime,Schedule> scheduleMap;

    public BroadcastOptimizer()
    {
    }

    private void init() throws IOException
    {
        if (ssn == null)
        {
            try
            {
                ssn = new SunSpotNumber(sunSpotNumberPath);
                broadcastStation = new BroadcastStationsFile(BroadcastStationsPath);
                broadcastStation.load();
                scheduleMap = new TreeMap<>();
                for (Station station : broadcastStation.getStations().values())
                {
                    if (station.isActive())
                    {
                        scheduleMap.putAll(station.getSchedules());
                    }
                }
            }
            catch (MalformedURLException | JAXBException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    public BestStation bestStation(Location myLocation, Instant instant) throws IOException
    {
        init();
        OffsetDateTime utc = instant.atOffset(ZoneOffset.UTC);
        OffsetTime key = OffsetTime.from(utc);
        RingIterator.stream(key, scheduleMap, false)
                .map((Entry<OffsetTime, Schedule> e)->e.getValue())
                .filter(new ClassFilter(HfFax.class))
                .map((s)->(HfFax)s)
                .filter(new TimeFilter(utc))
                .filter(new LocationFilter(myLocation))
                ;
        return null;
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
    
    public class BestStation
    {
        private Schedule schedule;
        private double frequency;
        private EmissionClass emissionClass;

        public BestStation(Schedule schedule, double frequency, EmissionClass emissionClass)
        {
            this.schedule = schedule;
            this.frequency = frequency;
            this.emissionClass = emissionClass;
        }

        public Schedule getSchedule()
        {
            return schedule;
        }

        public double getFrequency()
        {
            return frequency;
        }

        public EmissionClass getEmissionClass()
        {
            return emissionClass;
        }

    }
}
