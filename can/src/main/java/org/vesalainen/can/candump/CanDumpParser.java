/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.candump;

import java.nio.channels.ReadableByteChannel;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.MoreMath;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.ReservedWords;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
@Rule(left="candump", value={"line*"})
@ReservedWords({"interface", "family", "type", "proto"})
public abstract class CanDumpParser extends AbstractParser implements ParserInfo
{
    @Rule("hashComment")
    protected void line()
    {
    }
    @Rule("interface '=' identifier '\\,' family '=' int '\\,' type '=' int '\\,' proto '=' int")
    protected void line(String bus, int family, int type, int proto, @ParserContext("CanDumpService") CanDumpService svc)
    {
        svc.setBus(bus);
    }
    @Rule("'<0x' hex '>' data2")
    protected void line(int rawId, byte[] data, @ParserContext("CanDumpService") CanDumpService svc)
    {
        int canId = svc.rawToCanId(rawId);
        svc.frame(System.currentTimeMillis(), canId, data.length, data);
    }
    @Rule("identifier hex data2")
    protected void line(String bus, int rawId, byte[] data, @ParserContext("CanDumpService") CanDumpService svc)
    {
        if (svc.isEnabled(bus))
        {
            int canId = svc.rawToCanId(rawId);
            svc.frame(System.currentTimeMillis(), canId, data.length, data);
        }
    }
    @Rule("'\\(' time '\\)' identifier hex '#' data")
    protected void line(long time, String bus, int rawId, byte[] data, @ParserContext("CanDumpService") CanDumpService svc)
    {
        if (svc.isEnabled(bus))
        {
            int canId = svc.rawToCanId(rawId);
            svc.frame(System.currentTimeMillis(), canId, data.length, data);
        }
    }
    @Rule("'\\[1\\]' hbyte")
    protected byte[] data2(byte b1)
    {
        return new byte[]{b1};
    }
    @Rule("'\\[2\\]' hbyte hbyte")
    protected byte[] data2(byte b1, byte b2)
    {
        return new byte[]{b1, b2};
    }
    @Rule("'\\[3\\]' hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3)
    {
        return new byte[]{b1, b2, b3};
    }
    @Rule("'\\[4\\]' hbyte hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3, byte b4)
    {
        return new byte[]{b1, b2, b3, b4};
    }
    @Rule("'\\[5\\]' hbyte hbyte hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3, byte b4, byte b5)
    {
        return new byte[]{b1, b2, b3, b4, b5};
    }
    @Rule("'\\[6\\]' hbyte hbyte hbyte hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6)
    {
        return new byte[]{b1, b2, b3, b4, b5, b6};
    }
    @Rule("'\\[7\\]' hbyte hbyte hbyte hbyte hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7)
    {
        return new byte[]{b1, b2, b3, b4, b5, b6, b7};
    }
    @Rule("'\\[8\\]' hbyte hbyte hbyte hbyte hbyte hbyte hbyte hbyte")
    protected byte[] data2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8)
    {
        return new byte[]{b1, b2, b3, b4, b5, b6, b7, b8};
    }
    @Terminal(expression = "[0-9a-fA-F][0-9a-fA-F]", radix=16, signed=false)
    protected byte hbyte(byte b)
    {
        return b;
    }
    @Terminal(expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected long time(CharSequence seq)
    {
        int length = seq.length();
        int idx = CharSequences.indexOf(seq, '.');
        if (idx == -1)
        {
            throw new IllegalArgumentException(seq+" not valid time");
        }
        long sec = Primitives.parseLong(seq, 0, idx);
        long frac = Primitives.parseLong(seq, idx+1, length);
        int mag = length - idx - 1;
        int coef = MoreMath.power(10, mag-3);
        return sec*1000+frac/coef;
    }
    @Terminal(expression="[0-9a-fA-F]+")
    protected byte[] data(String str)
    {
        return HexUtil.fromString(str);
    }
    @Terminal(expression = "[ \t]+")
    protected abstract void ws();
    
    @ParseMethod(start="candump", whiteSpace={"whiteSpace"}, charSet="US-ASCII")
    public void parse(ReadableByteChannel channel, @ParserContext("CanDumpService") CanDumpService svc)
    {
        throw new UnsupportedOperationException();
    }
    public static CanDumpParser getInstance()
    {
        return (CanDumpParser) GenClassFactory.loadGenInstance(CanDumpParser.class);
    }

}
