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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.j1939.pgnMXBean;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PgnMessage extends SingleMessage implements pgnMXBean
{
    protected final int pgn;
    protected final int source;
    protected final int priority;
    
    public PgnMessage(Executor executor, MessageClass messageClass, int canId, int len, String comment)
    {
        super(executor, messageClass, canId, len, comment);
        this.source = PGN.sourceAddress(canId);
        this.priority = PGN.messagePriority(canId);
        this.pgn = PGN.pgn(canId);
    }
    @Override
    protected ObjectName getObjectName()
    {
            try
            {
                return new ObjectName("org.vesalainen.can:type="+comment);
            }
            catch (MalformedObjectNameException ex)
            {
                throw new IllegalArgumentException(ex);
            }
    }

    @Override
    public int getPgn()
    {
        return pgn;
    }

    @Override
    public int getSource()
    {
        return source;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
    
    
}
