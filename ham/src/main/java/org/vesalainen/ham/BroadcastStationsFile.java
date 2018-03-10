/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, orTimeRanges
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY orTimeRanges FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.nio.file.Path;
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
import static org.vesalainen.ham.itshfbc.LocationFormatter.format;
import org.vesalainen.ham.jaxb.BroadcastStations;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastStationsFile
{
    public static final ObjectFactory OBJECT_FACTORY;
    public static final DatatypeFactory DATA_TYPE_FACTORY;
    public static final long DEF_DURATION_MINUTES = 15;
    public static final Comparator<ScheduleType> SCHEDULE_COMP = new ScheduleComp();
    private static JAXBContext jaxbCtx;
    private BroadcastStations stations;
    private URL url;
    
    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.ham.jaxb");
            OBJECT_FACTORY = new ObjectFactory();
            DATA_TYPE_FACTORY = DatatypeFactory.newInstance();
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

    public BroadcastStationsFile(Path file)
    {
        this(toUrl(file));
    }
    public BroadcastStationsFile(URL url)
    {
        this.url = url;
        stations = OBJECT_FACTORY.createBroadcastStations();
    }
    private static URL toUrl(Path file)
    {
        try
        {
            return file.toUri().toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public StationType addStation(String name, Location location)
    {
        StationType stationType = OBJECT_FACTORY.createStationType();
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
        return stations.getStation().stream().map(Station::new).collect(Collectors.toMap((Station s)->s.getName(), (org.vesalainen.ham.Station s)->s));
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
}
