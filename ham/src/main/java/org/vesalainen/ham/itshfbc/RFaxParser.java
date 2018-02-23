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
    
    @Rule("string? frequency times? 'J3C' power?")
    protected TransmitterType transmitter(String call, Double freq, String times, Double power)
    {
        TransmitterType transmitter = factory.createTransmitterType();
        transmitter.setCallSign(call);
        transmitter.setFrequency((long) (freq.doubleValue()*1000));
        transmitter.setTimes(times);
        transmitter.setPower((long) (power.doubleValue()*1000));
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
    @Rule("number 'Z' '\\-' number 'Z'")
    protected String times(Double from, Double to)
    {
        return String.format("%02d00-%02d00", from.intValue(), to.intValue());
    }
    @Rule("'ALL BROADCAST TIMES'")
    protected String times()
    {
        return "0000-2359";
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

    @Terminal(expression = "[A-Za-z][A-Za-z0-9]+")
    protected abstract String string(String value);

    @Terminal(expression = "[\\+\\-]?[0-9]+(\\.[0-9]+)?")
    protected Double number(String value)
    {
        return Double.valueOf(value);
    }

    @Terminal(expression = "[ \t]+")
    protected abstract void whiteSpace();

}
