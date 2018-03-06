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

import org.vesalainen.ham.OffsetTimeRange;
import java.time.Duration;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import static org.vesalainen.ham.BroadcastStationsFile.DATA_TYPE_FACTORY;
import static org.vesalainen.ham.BroadcastStationsFile.OBJECT_FACTORY;
import static org.vesalainen.ham.itshfbc.LocationFormatter.format;
import org.vesalainen.ham.station.DefaultCustomizer;
import org.vesalainen.ham.jaxb.HfFaxType;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.lang.Primitives;
import org.vesalainen.navi.Area;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.Regex;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.ham.itshfbc.RFaxParserImpl")
@GrammarDef()
public abstract class RFaxParser implements ParserInfo //extends Tracer
{
    
    public static RFaxParser getInstance()
    {
        return (RFaxParser) GenClassFactory.getGenInstance(RFaxParser.class);
    }

    @ParseMethod(start="mapLine", whiteSpace ={"mapWS", "quot"})
    public abstract List<MapType> parseMapLine(String text, @ParserContext("StationCustomizer") DefaultCustomizer customizer);
    
    @Rule("mapTitle? map*")
    protected List<MapType> mapLine(List<MapType> list)
    {
        return list;
    }
    @Rule("mapString scale? projection? lat lat")
    protected MapType map(String name, String scale, String projection, double latFrom, double latTo)
    {
        Area area = Area.getPolar(latFrom, latTo);
        MapType map = OBJECT_FACTORY.createMapType();
        map.setName(name);
        map.setScale(scale);
        map.setProjection(projection);
        for (Location loc : area.getLocations())
        {
            map.getCorners().add(format(loc));
        }
        return map;
    }
    @Rule("mapString scale? projection? lat lat lon lon lon")
    protected MapType map(String name, String scale, String projection, double latFrom, double latTo, double lonFrom, double midLon, double lonTo)
    {
        Area area = Area.getPolar(latFrom, latTo, lonFrom, midLon, lonTo);
        MapType map = OBJECT_FACTORY.createMapType();
        map.setName(name);
        map.setScale(scale);
        map.setProjection(projection);
        for (Location loc : area.getLocations())
        {
            map.getCorners().add(format(loc));
        }
        return map;
    }
    @Rule("mapString scale? projection? lat lat lon lon")
    protected MapType map(String name, String scale, String projection, double latFrom, double latTo, double lonFrom, double lonTo, @ParserContext("StationCustomizer") DefaultCustomizer customizer)
    {
        Area area = Area.getSquare(latFrom, latTo, lonFrom, lonTo);
        MapType map = OBJECT_FACTORY.createMapType();
        map.setName(name);
        map.setScale(scale);
        map.setProjection(projection);
        for (Location loc : area.getLocations())
        {
            map.getCorners().add(format(loc));
        }
        return map;
    }
    @Rule("coordinate coordinate coordinate coordinate")
    protected MapType map(Location sw, Location se, Location nw, Location ne)
    {
        Area area = Area.getArea(sw, se, nw, ne);
        MapType map = OBJECT_FACTORY.createMapType();
        for (Location loc : area.getLocations())
        {
            map.getCorners().add(format(loc));
        }
        return map;
    }
    @Rule("mapString scale? projection? coordinate coordinate coordinate coordinate")
    protected MapType map(String name, String scale, String projection, Location sw, Location se, Location nw, Location ne)
    {
        Area area = Area.getArea(sw, se, nw, ne);
        MapType map = OBJECT_FACTORY.createMapType();
        map.setName(name);
        map.setScale(scale);
        map.setProjection(projection);
        for (Location loc : area.getLocations())
        {
            map.getCorners().add(format(loc));
        }
        return map;
    }
    @Rule("lat lon")
    protected Location coordinate(double lat, double lon)
    {
        return new Location(lat, lon);
    }
    @ParseMethod(start="hfFax")
    public abstract HfFaxType[] parseHfFax(String text, @ParserContext("StationCustomizer") DefaultCustomizer customizer);
    
    @Rule("start ws content ws rpm valid? mapRef?")
    protected HfFaxType[] hfFax(XMLGregorianCalendar[] startArr, String ws1, String content, String ws2, int[] rpm, XMLGregorianCalendar[] valid, String[] map)
    {
        HfFaxType[] arr = new HfFaxType[startArr.length];
        int index = 0;
        for (XMLGregorianCalendar start : startArr)
        {
            HfFaxType hfFax = OBJECT_FACTORY.createHfFaxType();
            hfFax.setTime(start);
            hfFax.setContent(content);
            hfFax.setRpm(rpm[0]);
            hfFax.setIoc(rpm[1]);
            if (valid != null)
            {
                if (valid.length == startArr.length)
                {
                    hfFax.setValid(valid[index]);
                }
                else
                {
                    hfFax.setValid(valid[index/4]);
                }
            }
            if (map != null)
            {
                for (String m : map)
                {
                    hfFax.getMap().add(m);
                }
            }
            arr[index++] = hfFax;
        }
        return arr;
    }
    @Rule("ws? mapRefString '/' mapRefString")
    protected String[] mapRef(String ws, String map1, String map2)
    {
        return new String[]{map1, map2};
    }
    @Rule("ws? mapRefString")
    protected String[] mapRef(String ws, String map)
    {
        return new String[]{map};
    }
    @Rule("time4")
    @Rule("time4 '/\\-+'")
    @Rule("'\\-+/' time4")
    protected XMLGregorianCalendar[] start(Integer start)
    {
        XMLGregorianCalendar time = DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(start/100, start%100, 0, 0);
        return new XMLGregorianCalendar[]{time};
    }
    @Rule("time4 '/' time4")
    protected XMLGregorianCalendar[] start(Integer start1, Integer start2, @ParserContext("StationCustomizer") DefaultCustomizer customizer)
    {
        return customizer.convertStart(start1, start2);
    }
    @Rule("contentString")
    protected String content(String str)
    {
        return str;
    }
    @Rule("content ws contentString")
    protected String content(String content, String ws, String str)
    {
        return content+ws+str;
    }
    @Rule("rpm60")
    @Rule("rpm90")
    @Rule("rpm120")
    protected int[] rpm(int[] rpm)
    {
        return rpm;
    }
    @Rule("ws? time4")
    protected XMLGregorianCalendar[] valid(String ws, Integer time)
    {
        return new XMLGregorianCalendar[]{DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(time/100, time%100, 0, 0)};
    }
    @Rule("ws? time2")
    protected XMLGregorianCalendar[] valid(String ws, Integer[] time)
    {
        return new XMLGregorianCalendar[]{
            DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(time[0], 0, 0, 0),
            DATA_TYPE_FACTORY.newXMLGregorianCalendarTime(time[1], 0, 0, 0)
        };
    }
    @ParseMethod(start="transmitter", whiteSpace ={"whiteSpace", "quot"})
    public abstract TransmitterType parseTransmitter(String text, @ParserContext("StationCustomizer") DefaultCustomizer customizer);
    
    @Rule("callSign? frequency ranges? emissionClass power?")
    protected TransmitterType transmitter(String call, Double freq, List<OffsetTimeRange> ranges, String emission, Double power, @ParserContext("StationCustomizer") DefaultCustomizer customizer)
    {
        TransmitterType transmitter = OBJECT_FACTORY.createTransmitterType();
        transmitter.setCallSign(call);
        transmitter.setFrequency(freq);
        transmitter.getTime().addAll(customizer.convertRanges(freq, ranges).stream().map((r)->r.toTimeRangeType()).collect(Collectors.toList()));
        if (power != null)
        {
            transmitter.setPower(power);
        }
        transmitter.setEmission(emission);
        return transmitter;
    }
    @Rule("number 'kHz'")
    protected Double frequency(Double freq)
    {
        return freq;
    }
    @Rule("number '[kK]W'")
    protected Double power(Double power)
    {
        return power;
    }
    @Rule("range")
    protected List<OffsetTimeRange> ranges(OffsetTimeRange range)
    {
        List<OffsetTimeRange> ranges = new ArrayList<>();
        ranges.add(range);
        return ranges;
    }
    @Rule("ranges '\\,' range")
    protected List<OffsetTimeRange> ranges(List<OffsetTimeRange> ranges, OffsetTimeRange range)
    {
        ranges.add(range);
        return ranges;
    }
    @Rule("number")
    protected OffsetTimeRange range(Double from)
    {
        int t = from.intValue();
        return new OffsetTimeRange(OffsetTime.of(t/100, t%100, 0, 0, ZoneOffset.UTC), Duration.ofHours(1));
    }
    @Rule("number 'Z'? '\\-' number 'Z'?")
    protected OffsetTimeRange range(Double from, Double to)
    {
        return new OffsetTimeRange(from.intValue(), to.intValue());
    }
    @Rule("'ALL BROADCAST TIMES'")
    @Rule("'All Broadcast Times'")
    @Rule("'\\*'")
    protected List<OffsetTimeRange> ranges(@ParserContext("StationCustomizer") DefaultCustomizer customizer)
    {
        return null;
    }
    @Terminal(expression = "60/576", priority=1)
    protected int[] rpm60()
    {
        return new int[]{60, 576};
    }
    @Terminal(expression = "90/576", priority=1)
    protected int[] rpm90()
    {
        return new int[]{90, 576};
    }
    @Terminal(expression = "120/576", priority=1)
    protected int[] rpm120()
    {
        return new int[]{120, 576};
    }

    @Terminal(expression = "(LAMBERT|Lambert|POLAR|MERCATOR|Lambert_Conformal_Conic)", options={Regex.Option.CASE_INSENSITIVE})
    protected String projection(String p)
    {
        return p.replace('_', ' ');
    }
    @Terminal(expression = "[0-9]{4}")
    protected Integer time4(int value)
    {
        return value;
    }

    @Terminal(expression = "[0-9]{2}/[0-9]{2}")
    protected Integer[] time2(String value)
    {
        return new Integer[]{Primitives.parseInt(value, 0, 2), Primitives.parseInt(value, 3, 5)};
    }

    @Terminal(expression = "\\([^\\)]*\\)")
    protected abstract void quot();

    @Terminal(expression = "[0-9]+(\\.[0-9]+)?[NS]")
    protected double lat(String str)
    {
        switch (str.charAt(str.length()-1))
        {
            case 'N':
                return Primitives.findDouble(str);
            case 'S':
                return -Primitives.findDouble(str);
            default:
                throw new IllegalArgumentException(str);
        }
    }

    @Terminal(expression = "[0-9]+(\\.[0-9]+)?[EW]")
    protected double lon(String str)
    {
        switch (str.charAt(str.length()-1))
        {
            case 'E':
                return Primitives.findDouble(str);
            case 'W':
                return -Primitives.findDouble(str);
            default:
                throw new IllegalArgumentException(str);
        }
    }

    @Terminal(expression = "1:[0-9\\,]+")
    protected abstract String scale(String scale);

    @Terminal(expression = "([0-9]{1,2}|[A-Za-z][A-Za-z0-9'’]*)")
    protected abstract String mapRefString(String value);

    @Terminal(expression = "([0-9]{1,2}|[A-Za-z][A-Za-z0-9'’/]*)[\\.:]?")
    protected String mapString(String value)
    {
        if (value.endsWith(".") || value.endsWith(":"))
        {
            return value.substring(0, value.length()-1);
        }
        else
        {
            return value;
        }
    }

    @Terminal(expression = "(MAP AREAS:|MAP AREA:)")
    protected abstract void mapTitle();

    @Terminal(expression = "[A-Za-z\\(][^\n]*")
    protected abstract void stringExt();

    @Terminal(expression = "[A-Za-z0-9]+")
    protected abstract String string0(String value);

    @Terminal(expression = "[A-Za-z0-9]*[A-Za-z/]+[A-Za-z0-9]*")
    protected abstract String string(String value);

    @Terminal(expression = "[A-Za-z0-9]*[A-Za-z/]+[A-Za-z0-9\\-]*")
    protected abstract String callSign(String value);

    @Terminal(expression = "[A-Za-z/\\(\\)'\\,\\&\\*\\-_:\\+#\\.]+")
    protected abstract String string2(String value);

    @Terminal(expression = "[A-Za-z0-9/\\(\\)'\\,\\&\\*\\-_:\\+#\\.]+")
    protected abstract String contentString(String value);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)?")
    protected Double number(String value)
    {
        return Double.valueOf(value);
    }

    @Terminal(expression = "[ \t\\.\\-\\,:]+")
    protected abstract void mapWS();

    @Terminal(expression = "[ \t]+")
    protected abstract void whiteSpace();

    @Terminal(expression = "[ \t]+")
    protected String ws(String ws)
    {
        return ws;
    }

    @Terminal(expression = "[NAHRJBCFGDPKLMQVWX][0-37-9X][NABCDEFWX]")
    protected String emissionClass(String emission)
    {
        return emission;
    }

}
