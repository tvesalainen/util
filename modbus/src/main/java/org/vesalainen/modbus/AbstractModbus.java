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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;
import static org.vesalainen.modbus.FunctionCode.READ_HOLDING_REGISTERS;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractModbus extends JavaLogging
{
    protected final ByteBuffer sendBuffer;
    protected final ByteBuffer receiveBuffer;
    protected final ReentrantLock sendLock = new ReentrantLock();

    protected AbstractModbus(int size)
    {
        this.sendBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
        this.receiveBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
    }
    
    public short getShort(int unitId, int address) throws IOException
    {
        short transaction = startTransaction((byte) unitId, 5);
        sendLock.lock();
        try
        {
            sendBuffer.put(READ_HOLDING_REGISTERS.code());
            sendBuffer.putShort((short) address);
            sendBuffer.putShort((short)1);
            send();
        }
        finally
        {
            sendLock.unlock();
        }
        waitFor(transaction);
        try
        {
            byte functionCode = receiveBuffer.get();
            byte length = receiveBuffer.get();
            if (functionCode != READ_HOLDING_REGISTERS.code())
            {
                throw new IOException("modbus exception code "+length);
            }
            return receiveBuffer.getShort();
        }
        finally
        {
            releaseReader();
        }
    }

    protected abstract short startTransaction(byte unitId, int dataLength);
    protected abstract void waitFor(short transaction);
    protected abstract void releaseReader();
    protected abstract void send() throws IOException;

}
