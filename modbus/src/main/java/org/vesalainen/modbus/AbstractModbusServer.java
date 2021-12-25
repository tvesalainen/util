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
import static java.util.logging.Level.*;
import static org.vesalainen.modbus.ExceptionCode.*;
import static org.vesalainen.modbus.FunctionCode.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractModbusServer extends JavaLogging
{

    public AbstractModbusServer()
    {
        super(AbstractModbusServer.class);
    }
    
    public void handleRequest(ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        out.mark();
        byte functionCode = in.get();
        FunctionCode[] values = FunctionCode.values();
        if (functionCode >= values.length || values[functionCode] == null)
        {
            throw new ModbusException(ILLEGAL_FUNCTION_01);
        }
        FunctionCode code = values[functionCode];
        out.put(functionCode);
        switch (code)
        {
            case READ_HOLDING_REGISTERS:
                readHoldingRegisters(in, out);
                break;
            case WRITE_MULTIPLE_REGISTERS:
                writeMultipleRegisters(in, out);
                break;
        }
        handleRequest(functionCode, in, out);
    }

    protected abstract void handleRequest(byte functionCode, ByteBuffer in, ByteBuffer out) throws ModbusException;

    private void writeMultipleRegisters(ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        short address = in.getShort();
        short words = in.getShort();
        
        out.putShort(address);
        out.putShort(words);
        
        switch (words)
        {
            case 1:
                writeRegister(address, in.getShort());
                break;
            case 2:
                writeRegister(address, in.getInt());
                break;
            default:
                writeRegister(address, words, out);
        }
    }

    protected void writeRegister(short address, short value) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
    protected void writeRegister(short address, int value) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    protected void writeRegister(short address, short words, ByteBuffer out) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    private void readHoldingRegisters(ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        short address = in.getShort();
        short words = in.getShort();
        
        out.put((byte) (words*2));
        
        switch (words)
        {
            case 1:
                out.putShort(readHoldingRegisterShort(address));
                break;
            case 2:
                out.putInt(readHoldingRegisterInt(address));
                break;
            default:
                readHoldingRegisters(address, words, out);
                break;
        }
    }

    protected short readHoldingRegisterShort(short address) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    protected int readHoldingRegisterInt(short address) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    private void readHoldingRegisters(short address, short words, ByteBuffer out) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
}
