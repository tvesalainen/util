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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import static org.vesalainen.ham.itshfbc.LocationFormatter.format;
import org.vesalainen.ham.jaxb.BroadcastStations;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastStationsFile
{
    public static final Comparator<ScheduleType> SCHEDULE_COMP = new ScheduleComp();
    private static JAXBContext jaxbCtx;
    private static ObjectFactory objectFactory;
    private static DatatypeFactory dataTypeFactory;
    private BroadcastStations stations;
    private File file;
    
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

    public BroadcastStationsFile(File file)
    {
        this.file = file;
        stations = objectFactory.createBroadcastStations();
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
    public TransmitterType addTransmitter(StationType station)
    {
        TransmitterType transmitterType = objectFactory.createTransmitterType();
        station.getTransmitter().add(transmitterType);
        return transmitterType;
    }
    public ScheduleType addSchedule(StationType station)
    {
        ScheduleType scheduleType = objectFactory.createScheduleType();
        station.getSchedule().add(scheduleType);
        return scheduleType;
    }
    public MapType addMap(StationType station)
    {
        MapType mapType = objectFactory.createMapType();
        station.getMap().add(mapType);
        return mapType;
    }
    public List<StationType> getStations()
    {
        return stations.getStation();
    }
    public void load() throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        stations = (BroadcastStations) unmarshaller.unmarshal(file);
    }
    public void store() throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
        {
            store(writer);
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
