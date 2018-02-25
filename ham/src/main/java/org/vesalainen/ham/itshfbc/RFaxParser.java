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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.vesalainen.ham.jaxb.ObjectFactory;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.TransmitterType;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.Regex;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname("org.vesalainen.ham.itshfbc.RFaxParserImpl")
@GrammarDef()
public abstract class RFaxParser
{
    private ObjectFactory factory;
    private DatatypeFactory dataTypeFactory;
    
    public static RFaxParser getInstance()
    {
        return (RFaxParser) GenClassFactory.getGenInstance(RFaxParser.class);
    }

    public RFaxParser()
    {
        try
        {
            this.factory = new ObjectFactory();
            this.dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @ParseMethod(start="schedule", whiteSpace ={"whiteSpace"})
    public abstract ScheduleType[] parseSchedule(String text);
    
    @Rule("start '\\-' start content rpm '/' number")
    protected ScheduleType[] schedule(XMLGregorianCalendar[] startArr, XMLGregorianCalendar[] endArr, String content, int rpm, Double ioc)
    {
        ScheduleType schedule = factory.createScheduleType();
        schedule.setTime(startArr[0]);
        GregorianCalendar time1 = startArr[0].toGregorianCalendar();
        GregorianCalendar time2 = endArr[0].toGregorianCalendar();
        schedule.setDuration(dataTypeFactory.newDurationDayTime(time2.getTimeInMillis()-time1.getTimeInMillis()));
        schedule.setContent(content);
        schedule.setRpm(rpm);
        schedule.setIoc(ioc.intValue());
        return new ScheduleType[]{schedule};
    }
    @Rule("start content rpm '/' number valid? mapString?")
    protected ScheduleType[] schedule(XMLGregorianCalendar[] startArr, String content, int rpm, Double ioc, XMLGregorianCalendar[] valid, String map)
    {
        ScheduleType[] arr = new ScheduleType[startArr.length];
        int index = 0;
        for (XMLGregorianCalendar start : startArr)
        {
            ScheduleType schedule = factory.createScheduleType();
            schedule.setTime(start);
            schedule.setContent(content);
            schedule.setRpm(rpm);
            schedule.setIoc(ioc.intValue());
            if (valid != null)
            {
                if (valid.length == startArr.length)
                {
                    schedule.setValid(valid[index]);
                }
                else
                {
                    schedule.setValid(valid[index/4]);
                }
            }
            schedule.setMap(map);
            arr[index++] = schedule;
        }
        return arr;
    }
    @Rule("time4")
    @Rule("time4 '/\\-+'")
    @Rule("'\\-+/' time4")
    protected XMLGregorianCalendar[] start(Integer start)
    {
        XMLGregorianCalendar time = dataTypeFactory.newXMLGregorianCalendarTime(start/100, start%100, 0, 0);
        return new XMLGregorianCalendar[]{time};
    }
    @Rule("time4 '/' time4 aring?")
    protected XMLGregorianCalendar[] start(Integer start1, Integer start2, Character aring)
    {
        if (aring == null)
        {
            XMLGregorianCalendar time1 = dataTypeFactory.newXMLGregorianCalendarTime(start1/100, start1%100, 0, 0);
            XMLGregorianCalendar time2 = dataTypeFactory.newXMLGregorianCalendarTime(start2/100, start2%100, 0, 0);
            return new XMLGregorianCalendar[]{time1, time2};
        }
        else
        {
            XMLGregorianCalendar[] arr = new XMLGregorianCalendar[8];
            for (int ii=0;ii<4;ii++)
            {
                arr[ii] = dataTypeFactory.newXMLGregorianCalendarTime(start1/100, 15*ii, 0, 0);
                arr[ii+4] = dataTypeFactory.newXMLGregorianCalendarTime(start2/100, 15*ii, 0, 0);
            }
            return arr;
        }
    }
    @Rule("string0 string2*")
    protected String content(String str, List<String> list)
    {
        list.add(0, str);
        return list.stream().collect(Collectors.joining(" "));
    }
    @Rule("rpm60")
    @Rule("rpm90")
    @Rule("rpm120")
    protected int rpm(int rpm)
    {
        return rpm;
    }
    @Rule("time4")
    protected XMLGregorianCalendar[] valid(Integer time)
    {
        return new XMLGregorianCalendar[]{dataTypeFactory.newXMLGregorianCalendarTime(time/100, time%100, 0, 0)};
    }
    @Rule("time2 '/' time2")
    protected XMLGregorianCalendar[] valid(Integer time1, Integer time2)
    {
        return new XMLGregorianCalendar[]{
            dataTypeFactory.newXMLGregorianCalendarTime(time1, 0, 0, 0),
            dataTypeFactory.newXMLGregorianCalendarTime(time2, 0, 0, 0)
        };
    }
    @ParseMethod(start="transmitter", whiteSpace ={"whiteSpace", "quot"})
    public abstract TransmitterType parseTransmitter(String text);
    
    @Rule("string? frequency ranges? 'UTC'? 'J3C' power?")
    protected TransmitterType transmitter(String call, Double freq, String ranges, Double power)
    {
        TransmitterType transmitter = factory.createTransmitterType();
        transmitter.setCallSign(call);
        transmitter.setFrequency(freq);
        transmitter.setTimes(ranges);
        if (power != null)
        {
            transmitter.setPower(power);
        }
        return transmitter;
    }
    @Rule("number 'kHz'")
    protected Double frequency(Double freq)
    {
        return freq;
    }
    @Rule("number 'kW'")
    protected Double power(Double power)
    {
        return power;
    }
    @Rule("range")
    protected String ranges(String range)
    {
        return range;
    }
    @Rule("ranges '\\,' range")
    protected String ranges(String ranges, String range)
    {
        return ranges+','+range;
    }
    @Rule("number")
    protected String range(Double from)
    {
        int t = from.intValue();
        return String.format("%04d-%04d", t, t+59);
    }
    @Rule("number 'Z'? '\\-' number 'Z'? offset?")
    protected String range(Double from, Double to, Integer offset)
    {
        if (offset != null)
        {
            List<String> list = new ArrayList<>();
            int lim = ((to.intValue()/100)+24)%24;
            if (from < to)
            {
                lim = to.intValue()/100;
            }
            else
            {
                lim = to.intValue()/100+24;
            }
            for (int ii=from.intValue()/100;ii<lim;ii++)
            {
                list.add(String.format("%02d%02d-%02d%02d", ii%24, offset%60, (ii+(offset+15)/60)%24, (offset+15)%60));
            }
            return list.stream().collect(Collectors.joining(","));
        }
        return String.format("%04d-%04d", from.intValue(), to.intValue());
    }
    @Rule("'ALL BROADCAST TIMES' offset?")
    @Rule("'All Broadcast Times' offset?")
    @Rule("'\\*' offset?")
    protected String ranges(Integer offset)
    {
        if (offset != null)
        {
            List<String> list = new ArrayList<>();
            for (int ii=0;ii<24;ii++)
            {
                list.add(String.format("%02d%02d-%02d%02d", ii, offset%60, ii+(offset+15)/60, (offset+15)%60));
            }
            return list.stream().collect(Collectors.joining(","));
        }
        return null;
    }
    @Rule("number")
    protected Integer offset(Double offset)
    {
        return offset.intValue();
    }
    @ReservedWords(value =
    {
        "kHz",
        "J3C",
        "kW",
        "lpm90",
        "lpm120"
    },
    options =
    {
        Regex.Option.CASE_INSENSITIVE
    })
    @Terminal(expression = "60", options={Regex.Option.ACCEPT_IMMEDIATELY})
    protected int rpm60()
    {
        return 60;
    }
    @Terminal(expression = "90", options={Regex.Option.ACCEPT_IMMEDIATELY})
    protected int rpm90()
    {
        return 90;
    }
    @Terminal(expression = "120", options={Regex.Option.ACCEPT_IMMEDIATELY})
    protected int rpm120()
    {
        return 120;
    }

    @Terminal(expression = "[0-9]{4}")
    protected Integer time4(int value)
    {
        return value;
    }

    @Terminal(expression = "[0-9]{2}")
    protected Integer time2(int value)
    {
        return value;
    }

    @Terminal(expression = "\\([^\\)]*\\)")
    protected abstract void quot();

    @Terminal(expression = "([0-9]|[A-Za-z][A-Za-z0-9'’/]*)")
    protected abstract String mapString(String value);

    @Terminal(expression = "[A-Za-z0-9]+")
    protected abstract String string0(String value);

    @Terminal(expression = "[A-Za-z0-9]*[A-Za-z/]+[A-Za-z0-9]*")
    protected abstract String string(String value);

    @Terminal(expression = "[A-Za-z0-9]*[A-Za-z/\\(\\)'\\,\\&\\*\\-_:\\+#\\.]+[A-Za-z0-9]*")
    protected abstract String string2(String value);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)?")
    protected Double number(String value)
    {
        return Double.valueOf(value);
    }
    @Terminal(expression = "ö")
    protected Character aring()
    {
        return 'ö';
    }

    @Terminal(expression = "[ \t]+")
    protected abstract void whiteSpace();

}
