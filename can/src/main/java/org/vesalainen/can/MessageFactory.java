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
package org.vesalainen.can;

import java.util.Objects;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MessageFactory extends JavaLogging
{
    protected final SignalCompiler compiler;

    public MessageFactory(SignalCompiler compiler)
    {
        super(MessageFactory.class);
        this.compiler = compiler;
    }
    
    public AbstractMessage createMessage(int canId, MessageClass mc)
    {
        SingleMessage sm;
        String type = (String)mc.getAttributeValue("MessageType");
        type = type != null ? type : "Single";
        switch (type)
        {
            case "Single":
                sm = new SingleMessage(canId, mc.getMinSize(), mc.getName());
                break;
            case "Fast":
                sm = new FastMessage(canId, mc.getMinSize(), mc.getName());
                break;
            default:
                throw new UnsupportedOperationException(mc.getAttributeValue("MessageType")+" not supported");
        }
        finer("compile(%s)", mc);
        sm.addSignals(mc, compiler);
        return sm;
    }
}
