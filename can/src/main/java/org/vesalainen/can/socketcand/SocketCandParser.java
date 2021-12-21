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
package org.vesalainen.can.socketcand;

import java.nio.channels.ByteChannel;
import org.vesalainen.can.DataUtil;
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
@Rule(left="socketcand", value={"hi busOk rawOk frame*"})
public abstract class SocketCandParser extends AbstractParser implements ParserInfo
{
    private long data;
    @Rule("'<' 'hi' '>'")
    protected void hi(@ParserContext("SocketCandService") SocketCandService svc, @ParserContext(ParserConstants.INPUTREADER) InputReader input)
    {
        svc.openBus();
        svc.setInput(input);
    }
    @Rule("'<' 'ok' '>'")
    protected void busOk(@ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.rawMode();
    }
    @Rule("'<' 'ok' '>'")
    protected void rawOk(@ParserContext("SocketCandService") SocketCandService svc)
    {
    }
    @Rule("'<' 'frame' hex time data '>'")
    protected void frame(int canId, long time, int dataLength, @ParserContext(ParserConstants.INPUTREADER) InputReader input, @ParserContext("SocketCandService") SocketCandService svc)
    {
        if (dataLength < 0 || dataLength > 8)
        {
            throw new IllegalArgumentException(dataLength+" illegal");
        }
        svc.queue(time, canId, dataLength, data);
    }
    
    @Terminal(expression="[0-9a-fA-F]+")
    protected int data(CharSequence data, @ParserContext("SocketCandService") SocketCandService svc)
    {
        this.data = DataUtil.asLong(data);
        return DataUtil.length(data);
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected long time(CharSequence time, @ParserContext("SocketCandService") SocketCandService svc)
    {
        long m = 0;
        boolean decimal = false;
        int dec = 0;
        int length = time.length();
        for (int ii=0;ii<length;ii++)
        {
            int cc = time.charAt(ii);
            if (cc == '.')
            {
                decimal = true;
            }
            else
            {
                m*=10;
                m+=Character.digit(cc, 10);
                if (decimal)
                {
                    dec++;
                    if (dec == 3)
                    {
                        break;
                    }
                }
            }
        }
        if (dec != 3)
        {
            throw new IllegalArgumentException(time.toString());
        }
        return m;
    }
    
    @ParseMethod(start="socketcand", whiteSpace={"whiteSpace"}, charSet="US-ASCII")
    public void parse(ByteChannel channel, @ParserContext("SocketCandService") SocketCandService svc)
    {
        throw new UnsupportedOperationException();
    }
    public static SocketCandParser getInstance()
    {
        return (SocketCandParser) GenClassFactory.loadGenInstance(SocketCandParser.class);
    }
}
