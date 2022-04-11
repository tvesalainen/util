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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import static org.vesalainen.modbus.FunctionCode.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 * AbstractModbusClient handles basic modbus protocol
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T>
 * @see <a href="https://modbus.org/docs/Modbus_Application_Protocol_V1_1b.pdf">MODBUS APPLICATION PROTOCOL SPECIFICATION V1.1b </a>
 */
public abstract class AbstractModbusClient<T extends ReadableByteChannel & WritableByteChannel> extends JavaLogging
{
    protected final T channel;
    protected final ByteBuffer sendBuffer;
    protected final ByteBuffer receiveBuffer;

    protected AbstractModbusClient(T channel, int size)
    {
        super(AbstractModbusClient.class);
        this.channel = channel;
        this.sendBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
        this.receiveBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
    }
    
    public void setShort(int unitId, int address, short value) throws IOException
    {
        short transaction = putWriteMultipleRegistersRequestHeader(unitId, address, 1);
        sendBuffer.putShort((short) value);
        send(transaction, (short)6, (byte)unitId);
        readWriteMultipleRegistersResponseHeader();
    }
    public void setInt(int unitId, int address, int value) throws IOException
    {
        short transaction = putWriteMultipleRegistersRequestHeader(unitId, address, 2);
        sendBuffer.putInt(value);
        send(transaction, (short)6, (byte)unitId);
        readWriteMultipleRegistersResponseHeader();
    }
    public String getString(int unitId, int address, int words) throws IOException
    {
        short transaction = putReadRegistersRequestHeader(unitId, address, words);
        send(transaction, (short)(3+2*words), (byte)unitId);
        readReadRegistersResponseHeader();
        byte[] buf = new byte[2*words];
        receiveBuffer.get(buf);
        return new String(buf, US_ASCII);
    }
    public int getUnsignedShort(int unitId, int address) throws IOException
    {
        return Short.toUnsignedInt(getShort(unitId, address));
    }
    public short getShort(int unitId, int address) throws IOException
    {
        short transaction = putReadRegistersRequestHeader(unitId, address, 1);
        send(transaction, (short)5, (byte)unitId);
        readReadRegistersResponseHeader();
        return receiveBuffer.getShort();
    }
    public long getUnsignedInt(int unitId, int address) throws IOException
    {
        return Integer.toUnsignedLong(getInt(unitId, address));
    }
    public int getInt(int unitId, int address) throws IOException
    {
        short transaction = putReadRegistersRequestHeader(unitId, address, 2);
        send(transaction, (short)7, (byte)unitId);
        readReadRegistersResponseHeader();
        return receiveBuffer.getInt();
    }

    private short putWriteMultipleRegistersRequestHeader(int unitId, int address, int words) throws IOException
    {
        short transaction = startTransaction((byte) unitId);
        sendBuffer.put(WRITE_MULTIPLE_REGISTERS.code());
        sendBuffer.putShort((short) address);
        sendBuffer.putShort((short) words);
        sendBuffer.put((byte) (words*2));
        return transaction;
    }
    private short putReadRegistersRequestHeader(int unitId, int address, int words) throws IOException
    {
        short transaction = startTransaction((byte) unitId);
        sendBuffer.put(READ_HOLDING_REGISTERS.code());
        sendBuffer.putShort((short) address);
        sendBuffer.putShort((short) words);
        return transaction;
    }
    private void send(short transaction, short dataLength, byte unit) throws IOException
    {
        sendBuffer.flip();
        sendBuffer.putShort(4, (short) (sendBuffer.remaining()-6));
        channel.write(sendBuffer);
        receiveBuffer.clear();
        TcpUtil.readModbusMessage(channel, receiveBuffer);
        receiveBuffer.flip();
        short transactionId = receiveBuffer.getShort();
        if (transactionId != transaction)
        {
            throw new IOException("waited transaction "+transaction+" got "+transactionId);
        }
        fine("received %d", transactionId);
        short protocolId = receiveBuffer.getShort();
        if (protocolId != 0)
        {
            throw new IOException("waited protocol 0 got "+protocolId);
        }
        short length = receiveBuffer.getShort();
        if (length != dataLength)
        {
            //throw new IOException("waited length "+dataLength+" got "+length);
        }
        byte unitId = receiveBuffer.get();
        if (unitId != unit)
        {
            throw new IOException("waited unitId "+unitId+" got "+unit);
        }
    }
    private void readWriteMultipleRegistersResponseHeader() throws IOException
    {
        byte functionCode = receiveBuffer.get();
        byte exception = receiveBuffer.get();
        if (functionCode != WRITE_MULTIPLE_REGISTERS.code())
        {
            ExceptionCode[] values = ExceptionCode.values();
            if (exception > 0 && exception < values.length)
            {
                throw new IOException("modbus exception code "+values[exception]);
            }
            else
            {
                throw new IOException("modbus exception code "+exception);
            }
        }
        receiveBuffer.get();
        receiveBuffer.get();
        receiveBuffer.get();
    }

    private void readReadRegistersResponseHeader() throws IOException
    {
        byte functionCode = receiveBuffer.get();
        byte exception = receiveBuffer.get();
        if (functionCode != READ_HOLDING_REGISTERS.code())
        {
            ExceptionCode[] values = ExceptionCode.values();
            if (exception > 0 && exception < values.length)
            {
                throw new IOException("modbus exception code "+values[exception]);
            }
            else
            {
                throw new IOException("modbus exception code "+exception);
            }
        }
    }
    protected abstract short startTransaction(byte unitId);



}
