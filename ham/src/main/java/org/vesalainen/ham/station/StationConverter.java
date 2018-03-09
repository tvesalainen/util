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
package org.vesalainen.ham.station;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.SEVERE;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.BroadcastStationsFile;
import static org.vesalainen.ham.BroadcastStationsFile.SCHEDULE_COMP;
import org.vesalainen.ham.LocationParser;
import org.vesalainen.ham.itshfbc.GeoDB;
import org.vesalainen.ham.itshfbc.GeoLocation;
import org.vesalainen.ham.itshfbc.RFaxParser;
import org.vesalainen.ham.station.DefaultCustomizer;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;
import org.vesalainen.ham.jaxb.HfFaxType;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.StationType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.regex.SyntaxErrorException;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Pair;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StationConverter extends JavaLogging
{
    private Duration maxDuration;
    private RFaxParser parser = RFaxParser.getInstance();

    private enum State {CALL, TIME, MAP};
    private Path dir;
    private Path out;
    private BroadcastStationsFile xml;
    private Map<String, Location> map;
    private Map<String,MapList<StationType,String>> retrans = new HashMap<>();
    private ObjectFactory factory = new ObjectFactory();
    private final DatatypeFactory dataTypeFactory;

    public StationConverter(Path dir, Path out) throws DatatypeConfigurationException
    {
        super(StationConverter.class);
        this.dir = dir;
        config("in dir %s", dir);
        this.out = out;
        config("out %s", out);
        this.dataTypeFactory = DatatypeFactory.newInstance();
        this.maxDuration = dataTypeFactory.newDurationDayTime(true, 0, 1, 0, 0);
        xml = new BroadcastStationsFile(out);
    }

    public void convert() throws IOException
    {
        Path stations = dir.resolve("stations.cvs");
        try (BufferedReader br = Files.newBufferedReader(stations))
        {
            String line = br.readLine();
            while (line != null)
            {
                String[] split = line.split("\\,");
                Path file = dir.resolve(split[0]);
                config("input %s", file);
                DefaultCustomizer customizer = DefaultCustomizer.getInstance(split[0]);
                config("customizer %s", customizer.getClass().getSimpleName());
                Location location = LocationParser.parse(split[1]);
                StationType station = convert(file, location, customizer);
                line = br.readLine();
            }
        }
        handleCallSigns();
        handleRetrans();
        mergeSchedules();
        handleDurations();
        checkMaps();
        xml.store();
    }
    public StationType convert(Path in, Location location, DefaultCustomizer customizer)
    {
        try (BufferedReader br = Files.newBufferedReader(in))
        {
            State state = State.CALL;
            String line = br.readLine();
            finest("in: '%s'", line);
            StationType station = xml.addStation(line.trim(), location);
            line = br.readLine();
            while (line != null)
            {
                finest("in: '%s'", line);
                if (!line.startsWith("#"))
                {
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
                                case CALL:
                                    if (customizer.isScheduleStart(line))
                                    {
                                        state = State.TIME;
                                    }
                                    else
                                    {
                                        call(station, line, customizer);
                                    }
                                    break;
                                case TIME:
                                    if (customizer.isMapStart(line))
                                    {
                                        state = State.MAP;
                                        map(station, line, customizer);
                                    }
                                    else
                                    {
                                        time(station, line, customizer);
                                    }
                                    break;
                                case MAP:
                                        map(station, line, customizer);
                                    break;
                            }
                        }
                    }
                }
                line = br.readLine();
            }
            customizer.after(station);
            return station;
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "%S", ex.getMessage());
            return null;
        }
    }
    private void call(StationType station, String line, DefaultCustomizer customizer)
    {
        if (line.length() > 5)
        {
            try
            {
                String transmitterLine = customizer.transmitterLine(line);
                finest("cust: '%s'", transmitterLine);
                TransmitterType transmitter = parser.parseTransmitter(transmitterLine, customizer);
                customizer.after(transmitter, transmitterLine);
                station.getTransmitter().add(transmitter);
            }
            catch (SyntaxErrorException ex)
            {
                warning(ex.getMessage());
                if (ex.getCause() != null)
                {
                    throw ex;
                }
            }
        }
    }

    private void time(StationType station, String line, DefaultCustomizer customizer)
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
                            String sceduleLine = customizer.scheduleLine(line);
                            finest("cust: '%s'", sceduleLine);
                            HfFaxType[] hfFaxes = parser.parseHfFax(sceduleLine, customizer);
                            for (HfFaxType hfFax : hfFaxes)
                            {
                                customizer.after(hfFax, sceduleLine);
                                station.getHfFax().add(hfFax);
                            }
                        }
                        catch (SyntaxErrorException ex)
                        {
                            warning(ex.getMessage());
                            if (ex.getCause() != null)
                            {
                                throw ex;
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
    private void map(StationType station, String line, DefaultCustomizer customizer)
    {
        if (line.length() > 5)
        {
            try
            {
                String mapLine = customizer.mapLine(line);
                finest("cust: '%s'", mapLine);
                List<MapType> maps = parser.parseMapLine(mapLine, customizer);
                for (MapType m : maps)
                {
                    customizer.after(m, mapLine);
                    station.getMap().add(m);
                }
            }
            catch (SyntaxErrorException ex)
            {
                warning(ex.getMessage());
                if (ex.getCause() != null)
                {
                    throw ex;
                }
            }
        }
    }
    private void checkMaps()
    {
        xml.getStationTypes().forEach(this::checkMaps);
    }
    private void checkMaps(StationType station)
    {
        Map<String, MapType> map = station.getMap().stream().collect(Collectors.toMap((m)->m.getName(), (m)->m));
        for (HfFaxType scedule : station.getHfFax())
        {
            for (String m : scedule.getMap())
            {
                if (m != null)
                {
                    if (!map.containsKey(m))
                    {
                        warning("%s %s notfound", station.getName(), m);
                    }
                }
            }
        }
    }
    private void mergeSchedules()
    {
        xml.getStationTypes().forEach(this::mergeSchedules);
    }
    private void mergeSchedules(StationType station)
    {
        MapList<Pair<XMLGregorianCalendar,List<String>>,HfFaxType> ml = new HashMapList<>();
        for (HfFaxType hfFax : station.getHfFax())
        {
            ml.add(new Pair(hfFax.getTime(), hfFax.getWeekdays()), hfFax);
        }
        station.getHfFax().clear();
        ml.forEach((t,l)->
        {
            HfFaxType st = l.get(0);
            station.getHfFax().add(st);
            if (l.size() > 1)
            {
                st.setContent(l.stream().map((s)->s.getContent()).collect(Collectors.joining(" / ")));
            }
        });
    }
    private void handleDurations()
    {
        xml.getStationTypes().forEach(this::handleDurations);
    }
    private void handleDurations(StationType station)
    {
        List<HfFaxType> hfFaxes = station.getHfFax();
        if (!hfFaxes.isEmpty())
        {
            hfFaxes.sort(SCHEDULE_COMP);
            ScheduleType prev = null;
            for (ScheduleType cur : hfFaxes)
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
            ScheduleType ls = hfFaxes.get(hfFaxes.size()-1);
            GregorianCalendar first = hfFaxes.get(0).getTime().toGregorianCalendar();
            GregorianCalendar last = ls.getTime().toGregorianCalendar();
            Duration duration = dataTypeFactory.newDuration(24*60*60000-(last.getTimeInMillis()-first.getTimeInMillis()));
            if (duration.isShorterThan(maxDuration) && ls.getDuration() == null)
            {
                ls.setDuration(duration);
            }
        }
    }
    private void handleCallSigns()
    {
        xml.getStationTypes().stream().forEach(this::handleCallSigns);
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
        Map<XMLGregorianCalendar, HfFaxType> map = station.getHfFax().stream().collect(Collectors.toMap((HfFaxType s)->s.getTime(), (HfFaxType s)->s));
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
                    HfFaxType trg = map.get(cal);
                    if (trg == null)
                    {
                        throw new IllegalArgumentException(p+" not found");
                    }
                    HfFaxType ns = factory.createHfFaxType();
                    int t = Integer.parseInt(start[si]);
                    ns.setTime(dataTypeFactory.newXMLGregorianCalendarTime(t/100, t%100, 0, 0));
                    ns.setContent("RE: "+trg.getContent());
                    ns.setRpm(trg.getRpm());
                    ns.setIoc(trg.getIoc());
                    ns.setValid(trg.getValid());
                    ns.getMap().addAll(trg.getMap());
                    station.getHfFax().add(ns);
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
    private Map<String, Integer[]> createTestConditions()
    {
        Map<String,Integer[]> map = new HashMap<>();
        map.put("CAPE NAVAL, SOUTH AFRICA", new Integer[]{4, 9, 4});
        map.put("TOKYO, JAPAN", new Integer[]{3, 75, 6});
        map.put("PEVEK, CHUKOTKA PENINSULA", new Integer[]{1, 6, 0});
        map.put("TAIPEI, REPUBLIC OF CHINA", new Integer[]{4, 38, 1});
        map.put("SEOUL, REPUBLIC OF KOREA", new Integer[]{5, 62, 3});
        map.put("BANGKOK, THAILAND", new Integer[]{1, 26, 1});
        map.put("KYODO NEWS AGENCY, JAPAN/SINGAPORE", new Integer[]{8, 30, 0});
        map.put("NORTHWOOD, UNITED KINGDOM (PERSIAN GULF)", new Integer[]{3, 69, 1});
        map.put("RIO DE JANEIRO, BRAZIL", new Integer[]{2, 10, 4});
        map.put("VALPARAISO PLAYA ANCHA, CHILE  (CBV)", new Integer[]{3, 12, 2});
        map.put("PUNTA ARENAS MAGALLANES, CHILE (CBM)", new Integer[]{2, 12, 2});
        map.put("HALIFAX, NOVA SCOTIA, CANADA", new Integer[]{5, 34, 9});
        map.put("IQALUIT, CANADA", new Integer[]{2, 8, 0});
        map.put("RESOLUTE, CANADA", new Integer[]{2, 8, 0});
        map.put("SYDNEY - NOVA SCOTIA, CANADA", new Integer[]{2, 5, 0});
        map.put("INUVIK, CANADA", new Integer[]{2, 4, 0});
        map.put("KODIAK, ALASKA, U.S.A.", new Integer[]{4, 50, 7});
        map.put("PT. REYES, CALIFORNIA, U.S.A.", new Integer[]{5, 83, 10});
        map.put("NEW ORLEANS, LOUISIANA, U.S.A", new Integer[]{4, 61, 6});
        map.put("BOSTON, MASSACHUSETTS, U.S.A.", new Integer[]{4, 71, 8});
        map.put("CHARLEVILLE, AUSTRALIA", new Integer[]{5, 66, 11});
        map.put("WILUNA, AUSTRALIA", new Integer[]{5, 66, 11});
        map.put("WELLINGTON, NEW ZEALAND", new Integer[]{5, 64, 2});
        map.put("HONOLULU, HAWAII, U.S.A.", new Integer[]{3, 96, 15});
        map.put("ATHENS, GREECE", new Integer[]{2, 11, 3});
        map.put("MURMANSK, RUSSIA", new Integer[]{4, 6, 3});
        map.put("HAMBURG/PINNEBERG, GERMANY", new Integer[]{3, 44, 0});
        map.put("NORTHWOOD, UNITED KINGDOM", new Integer[]{4, 93, 0});
        return map;
    }
    int testCases()
    {
        final IntReference errors = new IntReference(0);
        Map<String, StationType> stations = new HashMap<>();
        xml.getStationTypes().forEach((s)->
        {
            StationType old = stations.put(s.getName(), s);
            if (old != null)
            {
                errors.add(1);
                warning("%s is duplicate", s.getName());
            }
        });
        Map<String, Integer[]> testConditions = createTestConditions();
        testConditions.forEach((n,t)->
        {
            StationType station = stations.get(n);
            if (station == null)
            {
                errors.add(1);
                warning("%s not found", n);
            }
            else
            {
                check(errors, station, t);
            }
        });
        return errors.getValue();
    }
    private void check(IntReference errors, StationType station, Integer[] testCondition)
    {
        boolean ok = true;
        if (testCondition[0] != station.getTransmitter().size())
        {
            errors.add(1);
            warning("Transmitter count %d != %d", station.getTransmitter().size(), testCondition[0]);
            ok = false;
        }
        if (testCondition[1] != station.getHfFax().size())
        {
            errors.add(1);
            warning("Schedule count %d != %d", station.getHfFax().size(), testCondition[1]);
            ok = false;
        }
        if (testCondition[2] != station.getMap().size())
        {
            errors.add(1);
            warning("Map count %d != %d", station.getMap().size(), testCondition[2]);
            ok = false;
        }
        if (ok)
        {
            config("%s ok", station.getName());
        }
        else
        {
            warning("%s failed", station.getName());
        }
    }
}
