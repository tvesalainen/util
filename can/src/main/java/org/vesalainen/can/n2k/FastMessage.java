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
package org.vesalainen.can.n2k;

import java.util.concurrent.Executor;
import org.vesalainen.can.PgnMessage;
import org.vesalainen.can.dbc.MessageClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastMessage extends PgnMessage
{
    private final static int MAX_FAST_SIZE = 223*8; // bits
    private FastReader reader;
    private byte packetId;
    private int byteCount;
    private int byteMax;
    
    public FastMessage(Executor executor, MessageClass mc, int canId, int len, String comment)
    {
        super(executor, mc, canId, mc.isRepeating()?223:len, comment);
        this.reader = new FastReader(name, buf);
    }

    @Override
    public int getMaxBytes()
    {
        return 223;
    }

    @Override
    public int getMaxBits()
    {
        return MAX_FAST_SIZE;
    }

    @Override
    protected boolean update(long time, int canId, int dataLength, long data)
    {
        millisSupplier = ()->time;
        return reader.update(time, canId, dataLength, data);
    }
    
}
