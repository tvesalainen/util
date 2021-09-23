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

import java.util.concurrent.Executor;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractMessageFactory extends JavaLogging
{
    
    public AbstractMessageFactory()
    {
        super(AbstractMessageFactory.class);
    }

    public AbstractMessage createMessage(Executor executor, int canId, MessageClass mc)
    {
        SingleMessage sm = new SingleMessage(executor, mc, canId, mc.getMinSize(), mc.getName());
        finer("compile(%s)", mc);
        SignalCompiler comp = getCompiler(mc);
        if (comp.needCompilation(canId))
        {
            sm.addSignals(comp);
        }
        return sm;
    }

    public AbstractMessage createPgnMessage(Executor executor, int canId, MessageClass mc)
    {
        SingleMessage sm;
        String type = (String) mc.getAttributeValue("MessageType");
        type = type != null ? type : "Single";
        switch (type)
        {
            case "Single":
                sm = new PgnMessage(executor, mc, canId, mc.getMinSize(), mc.getName());
                break;
            case "Fast":
                sm = new FastMessage(executor, mc, canId, mc.getMinSize(), mc.getName());
                break;
            default:
                throw new UnsupportedOperationException(mc.getAttributeValue("MessageType") + " not supported");
        }
        finer("compile(%s)", mc);
        SignalCompiler comp = getCompiler(mc);
        if (comp.needCompilation(canId))
        {
            sm.addSignals(comp);
        }
        return sm;
    }

    protected abstract SignalCompiler getCompiler(MessageClass mc);
    
}
