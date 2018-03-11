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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.ham.EmissionClass;
import org.vesalainen.ham.HfFax;
import org.vesalainen.ham.LocationFilter;
import org.vesalainen.ham.Schedule;
import org.vesalainen.ham.Station;
import org.vesalainen.ham.TimeUtils;
import org.vesalainen.ham.TimeFilter;
import org.vesalainen.ham.Transmitter;
import org.vesalainen.ham.itshfbc.Circuit;
import org.vesalainen.ham.itshfbc.CircuitFrequency;
import org.vesalainen.ham.itshfbc.Noise;
import org.vesalainen.ham.itshfbc.Prediction;
import org.vesalainen.ham.itshfbc.RSN;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.ssn.SunSpotNumber;
import org.vesalainen.util.BestNonOverlapping;
import org.vesalainen.util.OrderedList;
import org.vesalainen.util.Range;
import org.vesalainen.util.SimpleRange;
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
    private Noise noise = Noise.RESIDENTIAL;
    private BroadcastStationsFile broadcastStation;
    private OrderedList<SimpleRange<OffsetTime>> scheduleList;
    private Map<String,Circuit> circuitMap = new HashMap<>();

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
                scheduleList = new OrderedList<>();
                for (Station station : broadcastStation.getStations().values())
                {
                    if (station.isActive())
                    {
                        for (Schedule s : station.getSchedules())
                        {
                            scheduleList.add(s);
                        }
                    }
                }
            }
            catch (MalformedURLException | JAXBException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    public BestStation bestStation(final Location myLocation, Instant instant) throws IOException
    {
        init();
        final OffsetDateTime utc = instant.atOffset(ZoneOffset.UTC);
        OffsetTime ot = OffsetTime.from(utc);
        SimpleRange<OffsetTime> key = new SimpleRange<>(ot, ot);
        Stream<SimpleRange<OffsetTime>> head = scheduleList.headStream(key, false, false);
        Stream<SimpleRange<OffsetTime>> tail = scheduleList.tailStream(key, true, false);
        Stream<BestStation> candidates = Stream.concat(tail, head)
                .map((s)->(Schedule)s)
                .filter(new TimeFilter(utc))
                .filter(new LocationFilter(myLocation))
                .map((s)->getInstance(s, utc, myLocation))
                .peek((s)->System.err.println(s))
                ;
        return BestNonOverlapping.best(candidates, this::compare);
    }
    private int compare(BestStation bs1, BestStation bs2)
    {
        if (bs1.isSecondary() != bs2.isSecondary())
        {
            if (bs1.isSecondary())
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        return -bs1.getCircuit().compareTo(bs2.getCircuit());
    }
    public BestStation getInstance(Schedule schedule, OffsetDateTime utc, Location myLocation)
    {
        OffsetDateTime cur = TimeUtils.next(utc, (OffsetTime)schedule.getFrom());
        List<CircuitFrequency> freqs = schedule
                .getStation()
                .getTransmitters()
                .stream()
                .filter((t)->t.isInRange(cur))
                .map((t->getPrediction(schedule, utc, myLocation, t)))
                .collect(Collectors.toList());
        freqs.sort(null);
        CircuitFrequency best = freqs.get(0);
        return new BestStation(schedule, best, myLocation);
    }
    public CircuitFrequency getPrediction(Schedule schedule, OffsetDateTime utc, Location myLocation, Transmitter transmitter)
    {
        Station station = schedule.getStation();
        double frequency = transmitter.getFrequency()/1000.0; // MHz
        String name = station.getName();
        Circuit circuit = circuitMap.get(name+frequency);
        if (circuit == null || !circuit.getPrediction().isValid(myLocation, utc))
        {
            try {
                circuit = new Circuit(frequency)
                        .setDate(utc)
                        .setNoise(noise)
                        .setReceiverAntennaPath(receiverAntennaPath)
                        .setReceiverLocation(myLocation)
                        .setRsn(RSN.SSB)
                        .setSunSpotNumbers(ssn.getSunSpotNumber(utc))
                        .setTransmitterAntennaPath(transmitterAntennaPath)
                        .setTransmitterLabel(station.getName())
                        .setTransmitterLocation(station.getLocation())
                        .setTransmitterPower(transmitter.getPower())
                        ;
                circuit.predict();
                circuitMap.put(name+frequency, circuit);
            }
            catch (IOException ex) 
            {
                throw new RuntimeException(ex);
            }
        }
        return new CircuitFrequency(circuit, frequency, circuit.getPrediction().getHourPrediction(utc.getHour()), transmitter.getEmissionClass());
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

    public BroadcastOptimizer setNoise(Noise noise)
    {
        this.noise = noise;
        return this;
    }
    
    public class BestStation implements Range<OffsetTime>
    {
        private Schedule<? extends ScheduleType> schedule;
        private CircuitFrequency circuit;
        private Location myLocation;

        public BestStation(Schedule<? extends ScheduleType> schedule, CircuitFrequency circuit, Location myLocation)
        {
            this.schedule = schedule;
            this.circuit = circuit;
            this.myLocation = myLocation;
        }

        public boolean isSecondary()
        {
            if (schedule instanceof HfFax)
            {
                HfFax fax = (HfFax) schedule;
                return fax.inMap(myLocation);
            }
            else
            {
                return false;
            }
        }
        @Override
        public OffsetTime getFrom()
        {
            return schedule.getFrom();
        }

        @Override
        public OffsetTime getTo()
        {
            return schedule.getTo();
        }

        public Schedule getSchedule()
        {
            return schedule;
        }

        public CircuitFrequency getCircuit()
        {
            return circuit;
        }

        public double getFrequency()
        {
            return circuit.getFrequency();
        }

        public EmissionClass getEmissionClass()
        {
            return circuit.getEmissionClass();
        }

        public Station getStation()
        {
            return schedule.getStation();
        }

        public String getContent()
        {
            return schedule.getContent();
        }

        @Override
        public String toString()
        {
            return schedule+" "+getStation().getName()+" "+getContent()+" "+circuit;
        }

    }
}
