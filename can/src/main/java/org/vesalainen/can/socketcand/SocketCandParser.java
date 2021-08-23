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
import org.vesalainen.parser.ParserInfo;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.util.AbstractParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@GenClassname()
@GrammarDef()
@Rule(left="socketcand", value={"hi ok frame*"})
public abstract class SocketCandParser extends AbstractParser implements ParserInfo
{
    @Rule("'<' 'hi' '>'")
    protected void hi(@ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.openBus();
    }
    @Rule("'<' 'ok' '>'")
    protected void ok(@ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.rawMode();
    }
    @Rule("'<' 'frame' hex double hex hex hex hex hex hex hex hex '>'")
    protected void frame(long canId, double time, int d1, int d2, int d3, int d4, int d5, int d6, int d7, int d8, @ParserContext("SocketCandService") SocketCandService svc)
    {
        svc.openBus();
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
