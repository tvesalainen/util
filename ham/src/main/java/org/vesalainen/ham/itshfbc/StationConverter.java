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
package org.vesalainen.ham.itshfbc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.BroadcastStationsFile;
import static org.vesalainen.ham.BroadcastStationsFile.SCHEDULE_COMP;
import org.vesalainen.ham.LocationParser;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.text.CamelCase;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StationConverter
{
    private Duration maxDuration;
    private RFaxParser parser = RFaxParser.getInstance();

    private enum State {NA, CALL, TIME, MAP};
    private Path in;
    private Path out;
    private BroadcastStationsFile xml;
    private Map<String, Location> map;
    private Map<String,MapList<StationType,String>> retrans = new HashMap<>();
    private ObjectFactory factory = new ObjectFactory();
    private final DatatypeFactory dataTypeFactory;

    public StationConverter(Path in, Path out) throws DatatypeConfigurationException
    {
        this.in = in;
        this.out = out;
        this.dataTypeFactory = DatatypeFactory.newInstance();
        this.maxDuration = dataTypeFactory.newDurationDayTime(true, 0, 1, 0, 0);
    }

    public void createFileList(Path dir) throws IOException
    {
        map = createStations();
        Path file = dir.resolve("stations.cvs");
        try (BufferedWriter bw = Files.newBufferedWriter(file))
        {
            for (Entry<String, Location> e : map.entrySet())
            {
                String filename = createFilename(e.getKey());
                bw.write(String.format("%s, %s", filename, e.getValue()));
                bw.newLine();
            }
        }
    }
    public void createFiles(Path dir) throws IOException
    {
        BufferedWriter writer = null;
        map = createStations();
        try (BufferedReader br = Files.newBufferedReader(in))
        {
            String line = br.readLine();
            while (line != null)
            {
                Iterator<String> iterator = map.keySet().iterator();
                while (iterator.hasNext())
                {
                    String name = iterator.next();
                    if (line.startsWith(name))
                    {
                        iterator.remove();
                        String filename = createFilename(name);
                        Path file = dir.resolve(filename);
                        if (writer != null)
                        {
                            writer.close();
                        }
                        writer = Files.newBufferedWriter(file);
                    }
                }
                if (writer != null)
                {
                    writer.write(line);
                    writer.newLine();
                }
                line = br.readLine();
            }
        }
        writer.close();
    }
    private String createFilename(String str)
    {
        String[] split = str.split("\\,");
        return CamelCase.camelCase(split[0])+".txt";
    }
    public void convert() throws IOException
    {
        map = createStations();
        xml = new BroadcastStationsFile(out.toFile());
        try (BufferedReader br = Files.newBufferedReader(in))
        {
            State state = State.MAP;
            StationType station = null;
            String line = br.readLine();
            while (line != null)
            {
                line = line.trim();
                if (line.contains("INFORMATION DATED") && station != null)
                {
                    station.setInfo(line);
                }
                else
                {
                    if (line.contains("not currently active") && station != null)
                    {
                        station.setActive(false);
                    }
                    else
                    {
                        switch (state)
                        {
                            case NA:
                                if (line.startsWith("CALL"))
                                {
                                    state = State.CALL;
                                }
                                break;
                            case CALL:
                                if (line.startsWith("TIME"))
                                {
                                    state = State.TIME;
                                }
                                else
                                {
                                    call(station, line);
                                }
                                break;
                            case TIME:
                                if (line.startsWith("MAP"))
                                {
                                    state = State.MAP;
                                    map(station, line);
                                }
                                else
                                {
                                    time(station, line);
                                }
                                break;
                            case MAP:
                                StationType stat = station(line);
                                if (stat != null)
                                {
                                    station = stat;
                                    state = State.NA;
                                }
                                else
                                {
                                    if (station != null)
                                    {
                                        map(station, line);
                                    }
                                }
                                break;
                        }
                    }
                }
                line = br.readLine();
            }
        }
        handleCallSigns();
        handleRetrans();
        handleDurations();
        checkMaps();
        xml.store();
    }
    private void call(StationType station, String line)
    {
        if (line.length() > 5)
        {
            try
            {
                TransmitterType transmitter = parser.parseTransmitter(line);
                station.getTransmitter().add(transmitter);
            }
            catch (SyntaxErrorException ex)
            {
                System.err.println(ex.getMessage());
                if (ex.getCause() != null)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void time(StationType station, String line)
    {
        if (line.contains("RETRANSMISSION OF "))
        {
            MapList<StationType, String> m = retrans.get("RETRANSMISSION OF ");
            if (m == null)
            {
                m = new HashMapList<>();
                retrans.put("RETRANSMISSION OF ", m);
            }
            m.add(station, line);
        }
        else
        {
            if (line.contains("REBROADCAST OF "))
            {
                MapList<StationType, String> m = retrans.get("REBROADCAST OF ");
                if (m == null)
                {
                    m = new HashMapList<>();
                    retrans.put("REBROADCAST OF ", m);
                }
                m.add(station, line);
            }
            else
            {
                if (line.contains("Repetition chart "))
                {
                    MapList<StationType, String> m = retrans.get("Repetition chart ");
                    if (m == null)
                    {
                        m = new HashMapList<>();
                        retrans.put("Repetition chart ", m);
                    }
                    m.add(station, line);
                }
                else
                {
                    if (isSchedule(line))
                    {
                        try
                        {
                            ScheduleType[] schedules = parser.parseSchedule(line);
                            for (ScheduleType schedule : schedules)
                            {
                                station.getSchedule().add(schedule);
                            }
                        }
                        catch (SyntaxErrorException ex)
                        {
                            System.err.println(ex.getMessage());
                            if (ex.getCause() != null)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isSchedule(String line)
    {
        if (line.length() < 4)
        {
            return false;
        }
        if (line.contains("RTTY"))
        {
            return false;
        }
        if (line.startsWith("----"))
        {
            return true;
        }
        for (int ii=0;ii<4;ii++)
        {
            if (!Character.isDigit(line.charAt(ii)))
            {
                return false;
            }
        }
        return true;
    }
    private void map(StationType station, String line)
    {
        if (line.length() > 5)
        {
            try
            {
                List<MapType> maps = parser.parseMapLine(line);
                station.getMap().addAll(maps);
            }
            catch (SyntaxErrorException ex)
            {
                System.err.println(ex.getMessage());
                if (ex.getCause() != null)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    private StationType station(String line)
    {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            String name = iterator.next();
            if (line.startsWith(name))
            {
                Location loc = map.get(name);
                iterator.remove();
                return xml.addStation(name, loc);
            }
        }
        return null;
    }
    private void checkMaps()
    {
        xml.getStations().forEach(this::checkMaps);
    }
    private void checkMaps(StationType station)
    {
        Map<String, MapType> map = station.getMap().stream().collect(Collectors.toMap((m)->m.getName(), (m)->m));
        for (ScheduleType scedule : station.getSchedule())
        {
            for (String m : scedule.getMap())
            {
                if (m != null)
                {
                    if (!map.containsKey(m))
                    {
                        System.err.println(station.getName()+" "+m+" not found");
                    }
                }
            }
        }
    }
    private void handleDurations()
    {
        xml.getStations().forEach(this::handleDurations);
    }
    private void handleDurations(StationType station)
    {
        List<ScheduleType> schedules = station.getSchedule();
        schedules.sort(SCHEDULE_COMP);
        ScheduleType prev = null;
        for (ScheduleType cur : schedules)
        {
            if (prev != null)
            {
                GregorianCalendar time1 = prev.getTime().toGregorianCalendar();
                GregorianCalendar time2 = cur.getTime().toGregorianCalendar();
                Duration duration = dataTypeFactory.newDuration(time2.getTimeInMillis()-time1.getTimeInMillis());
                if (duration.isShorterThan(maxDuration) && prev.getDuration() == null)
                {
                    prev.setDuration(duration);
                }
            }
            prev = cur;
        }
        ScheduleType ls = schedules.get(schedules.size()-1);
        GregorianCalendar first = schedules.get(0).getTime().toGregorianCalendar();
        GregorianCalendar last = ls.getTime().toGregorianCalendar();
        Duration duration = dataTypeFactory.newDuration(24*60*60000-(last.getTimeInMillis()-first.getTimeInMillis()));
        if (duration.isShorterThan(maxDuration) && ls.getDuration() == null)
        {
            ls.setDuration(duration);
        }
    }
    private void handleCallSigns()
    {
        xml.getStations().stream().forEach(this::handleCallSigns);
    }
    private void handleCallSigns(StationType station)
    {
        TransmitterType prev = null;
        for (TransmitterType tr : station.getTransmitter())
        {
            if (prev != null && tr.getCallSign() == null)
            {
                tr.setCallSign(prev.getCallSign());
            }
            prev = tr;
        }
    }
    private void handleRetrans()
    {
        retrans.forEach(this::handleRetrans);
    }
    private void handleRetrans(String key, MapList<StationType,String> ml)
    {
        ml.forEach((s,l)->handleRetrans(key, s, l));
    }
    private void handleRetrans(String key, StationType station, List<String> lines)
    {
        Map<XMLGregorianCalendar, ScheduleType> map = station.getSchedule().stream().collect(Collectors.toMap((ScheduleType s)->s.getTime(), (ScheduleType s)->s));
        for (String line : lines)
        {
            String[] start = line.substring(0, line.indexOf(' ')).split("/");
            int idx = line.indexOf(key)+key.length();
            String ss = line.substring(idx);
            String[] ptr = ss.substring(0, Math.min(9,ss.length())).split("/");
            int si = 0;
            int pi = 0;
            for (String str : start)
            {
                if (!str.startsWith("--"))
                {
                    String p = ptr[pi].substring(0, 4);
                    int tt = Integer.parseInt(p);
                    XMLGregorianCalendar cal = dataTypeFactory.newXMLGregorianCalendarTime(tt/100, tt%100, 0, 0);
                    ScheduleType trg = map.get(cal);
                    if (trg == null)
                    {
                        throw new IllegalArgumentException(p+" not found");
                    }
                    ScheduleType ns = factory.createScheduleType();
                    int t = Integer.parseInt(start[si]);
                    ns.setTime(dataTypeFactory.newXMLGregorianCalendarTime(t/100, t%100, 0, 0));
                    ns.setContent(key+trg.getContent());
                    ns.setRpm(trg.getRpm());
                    ns.setIoc(trg.getIoc());
                    ns.setValid(trg.getValid());
                    ns.getMap().addAll(trg.getMap());
                    station.getSchedule().add(ns);
                    pi++;
                }
                si++;
            }
        }
    }
    private Map<String, Location> createStations() throws IOException
    {
        Map<String,Location> map = new HashMap<>();
        GeoDB db = new GeoDB();
        GeoLocation loc = db.search(100, of ("CITY", "CAPE TOWN"), of ("NATION", "SOUTH AFRICA"));
        map.put("CAPE NAVAL, SOUTH AFRICA", loc.getLocation());
        loc = db.search(100, of ("CITY", "TOKYO") , of ("NATION", "JAPAN"));
        map.put("TOKYO, JAPAN", loc.getLocation());
        loc = db.search(100, of ("CITY", "PEVEK") , of ("NATION", "RUSSIA"));
        map.put("PEVEK, CHUKOTKA PENINSULA", loc.getLocation());
        loc = db.search(100, of ("CITY", "TAIPEI") , of ("NATION", "TAIWAN"));
        map.put("TAIPEI, REPUBLIC OF CHINA", loc.getLocation());
        loc = db.search(100, of ("CITY", "SEOUL") , of ("NATION", "SOUTH KOREA"));
        map.put("SEOUL, REPUBLIC OF KOREA", loc.getLocation());
        loc = db.search(100, of ("CITY", "BANGKOK") , of ("NATION", "THAILAND"));
        map.put("BANGKOK, THAILAND", loc.getLocation());
        loc = db.search(100, of ("CITY", "TOKYO") , of ("NATION", "JAPAN"));
        map.put("KYODO NEWS AGENCY, JAPAN/SINGAPORE", loc.getLocation());
        loc = db.search(100, of ("CITY", "LONDON") , of ("NATION", "UNITED KINGDOM"));
        map.put("NORTHWOOD, UNITED KINGDOM (PERSIAN GULF)", loc.getLocation());
        loc = db.search(100, of ("CITY", "RIO DE JANEIRO") , of ("NATION", "BRAZIL"));
        map.put("RIO DE JANEIRO, BRAZIL", loc.getLocation());
        loc = db.search(100, of ("CITY", "VALPARAISO") , of ("NATION", "CHILE"));
        map.put("VALPARAISO PLAYA ANCHA, CHILE", loc.getLocation());
        loc = db.search(100, of ("CITY", "PUNTA ARENAS") , of ("NATION", "CHILE"));
        map.put("PUNTA ARENAS MAGALLANES, CHILE", loc.getLocation());
        loc = db.search(100, of ("CITY", "HALIFAX"), of("STATE", "NS") , of ("NATION", "CANADA"));
        map.put("HALIFAX, NOVA SCOTIA, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "FROBISHER BAY") , of ("NATION", "CANADA"));
        map.put("IQALUIT, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "RESOLUTE") , of ("NATION", "CANADA"));
        map.put("RESOLUTE, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "SYDNEY"), of("STATE", "NS") , of ("NATION", "CANADA"));
        map.put("SYDNEY - NOVA SCOTIA, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "INUVIK") , of ("NATION", "CANADA"));
        map.put("INUVIK, CANADA", loc.getLocation());
        loc = db.search(100, of ("CITY", "KODIAK"), of("STATE", "AK") , of ("NATION", "USA"));
        map.put("KODIAK, ALASKA, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "SAN FRANCISCO"), of("STATE", "CA") , of ("NATION", "USA"));
        map.put("PT. REYES, CALIFORNIA, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "NEW ORLEANS"), of("STATE", "LA") , of ("NATION", "USA"));
        map.put("NEW ORLEANS, LOUISIANA, U.S.A", loc.getLocation());
        loc = db.search(100, of ("CITY", "BOSTON"), of("STATE", "MA") , of ("NATION", "USA"));
        map.put("BOSTON, MASSACHUSETTS, U.S.A.", loc.getLocation());
        map.put("CHARLEVILLE, AUSTRALIA", LocationParser.parse("26°19'49''S 146°15'51''E"));
        map.put("WILUNA, AUSTRALIA", LocationParser.parse("26°20'27''S 120°33'24''E "));
        loc = db.search(100, of ("CITY", "WELLINGTON") , of ("NATION", "NEW ZEALAND"));
        map.put("WELLINGTON, NEW ZEALAND", loc.getLocation());
        loc = db.search(100, of ("CITY", "HONOLULU"), of ("NATION", "USA"));
        map.put("HONOLULU, HAWAII, U.S.A.", loc.getLocation());
        loc = db.search(100, of ("CITY", "ATHENS") , of ("NATION", "GREECE"));
        map.put("ATHENS, GREECE", loc.getLocation());
        loc = db.search(100, of ("CITY", "MURMANSK") , of ("NATION", "RUSSIA"));
        map.put("MURMANSK, RUSSIA", loc.getLocation());
        loc = db.search(100, of ("CITY", "HAMBURG") , of ("NATION", "GERMANY"));
        map.put("HAMBURG/PINNEBERG, GERMANY", loc.getLocation());
        loc = db.search(100, of ("CITY", "LONDON") , of ("NATION", "UNITED KINGDOM"));
        map.put("NORTHWOOD, UNITED KINGDOM", loc.getLocation());
        return map;
    }
}
