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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleFrame implements Frame
{
    private final String bus;
    private final int canId;
    private final byte[] data;
    private final long millis;

    public SimpleFrame(String bus, int canId, byte[] data, long millis)
    {
        this.bus = bus;
        this.canId = canId;
        this.data = data;
        this.millis = millis;
    }

    @Override
    public String getBus()
    {
        return bus;
    }
    
    @Override
    public long getMillis()
    {
        return millis;
    }

    @Override
    public byte getData(int index)
    {
        return data[index];
    }

    @Override
    public int getDataLength()
    {
        return data.length;
    }

    @Override
    public int getCanId()
    {
        return canId;
    }
    
}
