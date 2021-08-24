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
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.DBCParser;
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
    protected void frame(int canId, @ParserContext(ParserConstants.INPUTREADER) InputReader input, @ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.frame(canId);
    }
    
    @Terminal(expression="[0-9a-fA-F]+")
    protected void data(InputReader input, @ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.setData(input.getStart(), input.getLength());
    }

    @Terminal(expression = "[\\+\\-]?[0-9]+\\.[0-9]+")
    protected void time(InputReader input, @ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.setTime(input.getStart(), input.getLength());
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
