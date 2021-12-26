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
package org.vesalainen.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.vesalainen.modbus.ExceptionCode.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ReadableByteBufferServer extends AbstractModbusServer
{
    protected final int size;
    protected final int offset;
    protected final ByteBuffer buf;

    public ReadableByteBufferServer(int words, int offset)
    {
        this.size = words*2;
        this.offset = offset;
        this.buf = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN);
    }

    protected int getActualAddress(short address)
    {
        return 2*(address-offset);
    }
    @Override
    protected void checkAddress(short address) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        if (actualAddress < 0 || actualAddress >= size)
        {
            throw new ModbusException(ILLEGAL_DATA_ADDRESS_02);
        }
    }

    @Override
    protected void readHoldingRegisters(short address, short words, ByteBuffer out) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        for (int ii=0;ii<words;ii++)
        {
            out.putShort(buf.getShort(actualAddress+ii));
        }
    }

    @Override
    protected int readHoldingRegisterInt(short address) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        return buf.getInt(actualAddress);
    }

    @Override
    protected short readHoldingRegisterShort(short address) throws ModbusException
    {
        int actualAddress = getActualAddress(address);
        return buf.getShort(actualAddress);
    }
    

}
