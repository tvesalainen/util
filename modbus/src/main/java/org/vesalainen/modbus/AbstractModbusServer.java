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
        byte functionCode = in.get();
        out.put(functionCode);
        switch (functionCode)
        {
            case 3:
                readHoldingRegisters(in, out);
                break;
            case 16:
                writeMultipleRegisters(in, out);
                break;
            default:
                handleRequest(functionCode, in, out);
                break;
        }
    }
    /**
     * Override to implement unsupported function code
     * @param functionCode
     * @param in
     * @param out
     * @throws ModbusException 
     */
    protected void handleRequest(byte functionCode, ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    private void writeMultipleRegisters(ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        short address = in.getShort();
        checkAddress(address);
        short words = in.getShort();
        if (words < 1 || words > 123)
        {
            throw new ModbusException(ILLEGAL_DATA_VALUE_03);
        }
        byte bytes = in.get();
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
    /**
     * Override to implement short write
     * @param address
     * @param value
     * @throws ModbusException 
     */
    protected void writeRegister(short address, short value) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
    /**
     * Override to implement int write
     * @param address
     * @param value
     * @throws ModbusException 
     */
    protected void writeRegister(short address, int value) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
    /**
     * Override to implement write of words registers
     * @param address
     * @param words
     * @param out
     * @throws ModbusException 
     */
    protected void writeRegister(short address, short words, ByteBuffer out) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }

    private void readHoldingRegisters(ByteBuffer in, ByteBuffer out) throws ModbusException
    {
        short address = in.getShort();
        checkAddress(address);
        short words = in.getShort();
        if (words < 1 || words > 125)
        {
            throw new ModbusException(ILLEGAL_DATA_VALUE_03);
        }
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
    /**
     * Override to check address. If address is not ok throw 
     * ModbusException(ILLEGAL_DATA_ADDRESS_02)
     * @param address
     * @throws ModbusException 
     */
    protected abstract void checkAddress(short address) throws ModbusException;
            
    /**
     * Override to implement reading of short register
     * @param address
     * @return
     * @throws ModbusException 
     */
    protected short readHoldingRegisterShort(short address) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
    /**
     * Override to implement reading of int register
     * @param address
     * @return
     * @throws ModbusException 
     */
    protected int readHoldingRegisterInt(short address) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
    /**
     * Override to implement reading of words registers
     * @param address
     * @param words
     * @param out
     * @throws ModbusException 
     */
    protected void readHoldingRegisters(short address, short words, ByteBuffer out) throws ModbusException
    {
        throw new ModbusException(ILLEGAL_FUNCTION_01);
    }
}
