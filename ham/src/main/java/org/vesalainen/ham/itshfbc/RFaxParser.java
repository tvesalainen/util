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
import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.ham.jaxb.ObjectFactory;
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
    private ObjectFactory factory = new ObjectFactory();
    
    public static RFaxParser getInstance()
    {
        return (RFaxParser) GenClassFactory.getGenInstance(RFaxParser.class);
    }
    
    @ParseMethod(start="transmitter", whiteSpace ={"whiteSpace", "quot"})
    public abstract TransmitterType parseTransmitter(String text);
    
    @Rule("string? frequency times? 'UTC'? 'J3C' power?")
    protected TransmitterType transmitter(String call, Double freq, String times, Double power)
    {
        TransmitterType transmitter = factory.createTransmitterType();
        transmitter.setCallSign(call);
        transmitter.setFrequency(freq);
        transmitter.setTimes(times);
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
    @Rule("time")
    protected String times(String time)
    {
        return time;
    }
    @Rule("times '\\,' time")
    protected String times(String times, String time)
    {
        return times+','+time;
    }
    @Rule("number")
    protected String time(Double from)
    {
        int t = from.intValue();
        return String.format("%04d-%04d", t, t+59);
    }
    @Rule("number 'Z'? '\\-' number 'Z'? offset?")
    protected String time(Double from, Double to, Integer offset)
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
    protected String times(Integer offset)
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
    },
    options =
    {
        Regex.Option.CASE_INSENSITIVE
    })
    @Terminal(expression = "\\([^\\)]*\\)")
    protected abstract void quot();

    @Terminal(expression = "[A-Za-z0-9]*[A-Za-z/]+[A-Za-z0-9]*")
    protected abstract String string(String value);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)?")
    protected Double number(String value)
    {
        return Double.valueOf(value);
    }

    @Terminal(expression = "[ \t]+")
    protected abstract void whiteSpace();

}
