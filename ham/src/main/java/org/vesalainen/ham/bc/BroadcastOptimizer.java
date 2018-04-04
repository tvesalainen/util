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
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.*;
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
import org.vesalainen.ham.itshfbc.HourPrediction;
import org.vesalainen.ham.itshfbc.Noise;
import org.vesalainen.ham.itshfbc.RSN;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.ssn.SunSpotNumber;
import org.vesalainen.util.BestNonOverlapping;
import org.vesalainen.util.OrderedList;
import org.vesalainen.util.Range;
import org.vesalainen.util.SimpleRange;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastOptimizer extends JavaLogging
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
    private double minSNR;

    public BroadcastOptimizer()
    {
        super(BroadcastOptimizer.class);
    }

    private void init() throws IOException
    {
        if (ssn == null)
        {
            try
            {
                ssn = new SunSpotNumber(sunSpotNumberPath);
                config("ssn from %s", sunSpotNumberPath);
                broadcastStation = new BroadcastStationsFile(BroadcastStationsPath);
                config("broadcast-stations from %s", BroadcastStationsPath);
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

    public BestStation bestStation(final Location myLocation, OffsetDateTime instant) throws IOException
    {
        init();
        fine("requested best station for %s %s", myLocation, instant);
        OffsetTime ot = OffsetTime.from(instant);
        SimpleRange<OffsetTime> key = new SimpleRange<>(ot, ot);
        Stream<SimpleRange<OffsetTime>> head = scheduleList.headStream(key, false, false);
        Stream<SimpleRange<OffsetTime>> tail = scheduleList.tailStream(key, true, false);
        Stream<BestStation> candidates = Stream.concat(tail, head)
                .peek((r)->finer("%s", r))
                .map((s)->(Schedule)s)
                .filter(new TimeFilter(instant))
                .filter(new LocationFilter(myLocation))
                .map((s)->getInstance(s, instant, myLocation))
                .filter((b)->snrFilter(b))
                ;
        BestStation best = BestNonOverlapping.best(candidates, this::compare);
        fine("best is %s", best);
        return best;
    }
    private boolean snrFilter(BestStation candidate)
    {
        if (candidate.snr90() > minSNR)
        {
            return true;
        }
        else
        {
            fine("skipping %s SNR90 < %f", candidate, minSNR);
            return false;
        }
    }
    private int compare(BestStation bs1, BestStation bs2)
    {
        if (bs1.getPriority() != bs2.getPriority())
        {
            return bs2.getPriority() - bs1.getPriority();
        }
        if (bs1.isSecondary() != bs2.isSecondary())
        {
            if (bs1.isSecondary())
            {
                fine("%s is secondary", bs2);
                return 1;
            }
            else
            {
                fine("%s is secondary", bs1);
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
                .map((t->getPrediction(schedule, cur, myLocation, t)))
                .collect(Collectors.toList());
        freqs.sort(null);
        if (isLoggable(FINER))
        {
            for (CircuitFrequency cf : freqs)
            {
                finer("%s", cf);
            }
        }
        CircuitFrequency best = freqs.get(0);
        BestStation candidate = new BestStation(schedule, best, myLocation);
        fine("candidate %s", candidate);
        return candidate;
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
        HourPrediction hourPrediction = circuit.getPrediction().getHourPrediction(utc.getHour());
        finer("hourPrediction %d %s", hourPrediction.getHour(), utc);
        return new CircuitFrequency(circuit, frequency, hourPrediction, transmitter.getEmissionClass());
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

    public BroadcastOptimizer setBroadcastStationsPath(URL BroadcastStationsPath)
    {
        this.BroadcastStationsPath = BroadcastStationsPath;
        return this;
    }

    public BroadcastOptimizer setMinSNR(double minSNR)
    {
        this.minSNR = minSNR;
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

        public int getPriority()
        {
            return schedule.getPriority();
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

        public double snr()
        {
            return circuit.snr();
        }

        public double snr90()
        {
            return circuit.snr90();
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
            return schedule+" "+getStation().getName()+" "/*+getContent()*/+" "+circuit;
        }

    }
}
