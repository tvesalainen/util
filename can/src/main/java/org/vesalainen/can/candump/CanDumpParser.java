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

import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import org.vesalainen.can.SimpleFrame;
import org.vesalainen.can.socketcand.SocketCandParser;
import org.vesalainen.can.socketcand.SocketCandService;
import org.vesalainen.lang.Primitives;
import org.vesalainen.math.MoreMath;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.ParserConstants;
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.AbstractParser;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
@Rule(left="candump", value={"frame*"})
public abstract class CanDumpParser extends AbstractParser implements ParserInfo
{
    @Rule("'\\(' time '\\)' string hex '#' data")
    protected void frame(long time, String bus, int rawId, byte[] data, @ParserContext("CanDumpService") CanDumpService svc)
    {
        if (svc.isEnabled(bus))
        {
            int canId = svc.rawToCanId(rawId);
            svc.frame(new SimpleFrame(bus, canId, data, time));
        }
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
