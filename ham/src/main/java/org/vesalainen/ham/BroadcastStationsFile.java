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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.time.Duration;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import static org.vesalainen.ham.itshfbc.LocationFormatter.format;
import org.vesalainen.ham.jaxb.BroadcastStations;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.navi.Area;
import org.vesalainen.util.Lists;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastStationsFile
{
    public static final long DEF_DURATION_MINUTES = 15;
    public static final Comparator<ScheduleType> SCHEDULE_COMP = new ScheduleComp();
    private static JAXBContext jaxbCtx;
    private static ObjectFactory objectFactory;
    private static DatatypeFactory dataTypeFactory;
    private BroadcastStations stations;
    private URL url;
    
    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.ham.jaxb");
            objectFactory = new ObjectFactory();
            dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException | JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public BroadcastStationsFile()
    {
        this(BroadcastStationsFile.class.getResource("/broadscast-stations.xml"));
    }

    public BroadcastStationsFile(File file)
    {
        this(toUrl(file));
    }
    public BroadcastStationsFile(URL url)
    {
        this.url = url;
        stations = objectFactory.createBroadcastStations();
    }
    private static URL toUrl(File file)
    {
        try
        {
            return file.toURI().toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public StationType addStation(String name, Location location)
    {
        StationType stationType = objectFactory.createStationType();
        stationType.setName(name);
        stationType.setLocation(format(location));
        stationType.setActive(true);
        stations.getStation().add(stationType);
        return stationType;
    }
    public List<StationType> getStationTypes()
    {
        return stations.getStation();
    }
    public Map<String, Station> getStations()
    {
        return stations.getStation().stream().map(Station::new).collect(Collectors.toMap((Station s)->s.getName(), (s)->s));
    }
    public void load() throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        stations = (BroadcastStations) unmarshaller.unmarshal(url);
    }
    /**
     * Stores contents if url is a file otherwise throws IllegalArgumentException
     * @throws IOException 
     */
    public void store() throws IOException
    {
        try {
            File file = new File(url.toURI());
            try (FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw))
            {
                store(bw);
            }
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public synchronized void store(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(stations, writer);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }
    public static class ScheduleComp implements Comparator<ScheduleType>
    {

        @Override
        public int compare(ScheduleType o1, ScheduleType o2)
        {
            return o1.getTime().compare(o2.getTime());
        }

    }
    public static class Station
    {
        private StationType station;
        private Map<String,Transmitter> transmitters;
        private Map<OffsetTime,Schedule> schedules;
        private Map<String,MapArea> maps;

        public Station(StationType station)
        {
            this.station = station;
            this.transmitters = station.getTransmitter().stream().map((t)->new Transmitter(this, t)).collect(Collectors.toMap((Transmitter t)->t.getCallSign(), (t)->t));
            this.schedules = station.getSchedule().stream().map((s)->new Schedule(this, s)).collect(Collectors.toMap((Schedule s)->s.getStart(), (s)->s));
            this.maps = station.getMap().stream().map((m)->new MapArea(this, m)).collect(Collectors.toMap((MapArea m)->m.getName(), (m)->m));
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
        
    }
    public static class Transmitter
    {
        private Station station;
        private TransmitterType transmitter;
        private EmissionClass emissionClass;

        public Transmitter(Station station, TransmitterType transmitter)
        {
            this.station = station;
            this.transmitter = transmitter;
            this.emissionClass = new EmissionClass(transmitter.getEmission());
        }

        public EmissionClass getEmissionClass()
        {
            return emissionClass;
        }

        public Station getStation()
        {
            return station;
        }

        public String getCallSign()
        {
            return transmitter.getCallSign();
        }

        public double getFrequency()
        {
            return transmitter.getFrequency();
        }

        public String getEmission()
        {
            return transmitter.getEmission();
        }

        public Double getPower()
        {
            return transmitter.getPower();
        }
        
    }
    public static class Schedule
    {
        private Station station;
        private ScheduleType schedule;
        private OffsetTime start;
        private OffsetTime end;

        public Schedule(Station station, ScheduleType schedule)
        {
            this.station = station;
            this.schedule = schedule;
            XMLGregorianCalendar t = schedule.getTime();
            this.start = OffsetTime.of(t.getHour(), t.getMinute(), t.getSecond(), 0, ZoneOffset.UTC);
            if (schedule.getDuration() != null)
            {
                Duration duration = Duration.parse(schedule.getDuration().toString());
                this.end = start.plusNanos(duration.toNanos());
            }
            else
            {
                this.end = start.plusMinutes(DEF_DURATION_MINUTES);
            }
        }

        public OffsetTime getStart()
        {
            return start;
        }

        public OffsetTime getEnd()
        {
            return end;
        }

        public Station getStation()
        {
            return station;
        }

        public String getContent()
        {
            return schedule.getContent();
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
    public static class MapArea
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
}
