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
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nio.ReadBuffer;
import org.vesalainen.util.HexUtil;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FastMessage extends PgnMessage
{
    private final static int MAX_FAST_SIZE = 223*8; // bits
    
    public FastMessage(Executor executor, MessageClass mc, int canId, int len, String comment)
    {
        super(executor, mc, canId, mc.isRepeating()?223:len, comment);
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
    protected boolean update(long time, int canId, ReadBuffer data)
    {
        millisSupplier = ()->time;
        setCurrentBytes(data.remaining());
        data.get(buf);
        if (PGN.pgn(canId) != getPgn())
        {
            warning("pgn %d != %d", PGN.pgn(canId), getPgn());
        }
        info("%s: %x %s", name, canId, HexUtil.toString(buf).toLowerCase());
        return true;
    }
    
}
