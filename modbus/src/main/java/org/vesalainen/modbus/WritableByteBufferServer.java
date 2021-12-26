package org.vesalainen.modbus;


import java.nio.ByteBuffer;
import org.vesalainen.modbus.ModbusException;
import org.vesalainen.modbus.ReadableByteBufferServer;

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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WritableByteBufferServer extends ReadableByteBufferServer
{
    
    public WritableByteBufferServer(int size, int offset)
    {
        super(size, offset);
    }

    @Override
    protected void writeRegister(short address, short words, ByteBuffer out) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        for (int ii=0;ii<words;ii+=2)
        {
            buf.putShort(actualAddress+ii, buf.getShort());
        }
    }

    @Override
    protected void writeRegister(short address, int value) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        buf.putInt(actualAddress, value);
    }

    @Override
    protected void writeRegister(short address, short value) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        buf.putShort(actualAddress, value);
    }
    
}
