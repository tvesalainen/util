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
    protected final ReentrantLock sendLock = new ReentrantLock();

    protected AbstractModbusClient(T channel, int size)
    {
        super(AbstractModbusClient.class);
        this.channel = channel;
        this.sendBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
        this.receiveBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.BIG_ENDIAN);
    }
    
    public void setShort(int unitId, int address, short value) throws IOException
    {
        Thread currentThread = Thread.currentThread();
        short transaction = 0;
        sendLock.lock();
        try
        {
            transaction = putWriteMultipleRegistersRequestHeader(unitId, address, 1, ()->waitForReadFinish(currentThread));
            sendBuffer.putShort((short) value);
            send();
        }
        finally
        {
            sendLock.unlock();
        }
        try
        {
            waitForResponce(transaction);
            readWriteMultipleRegistersResponseHeader();
        }
        finally
        {
            releaseReader();
        }
    }
    public String getString(int unitId, int address, int words) throws IOException
    {
        Thread currentThread = Thread.currentThread();
        short transaction = 0;
        sendLock.lock();
        try
        {
            transaction = putReadRegistersRequestHeader(unitId, address, words, ()->waitForReadFinish(currentThread));
            send();
        }
        finally
        {
            sendLock.unlock();
        }
        try
        {
            waitForResponce(transaction);
            readReadRegistersResponseHeader();
            byte[] buf = new byte[2*words];
            receiveBuffer.get(buf);
            return new String(buf, US_ASCII);
        }
        finally
        {
            releaseReader();
        }
    }
    public int getUnsignedShort(int unitId, int address) throws IOException
    {
        return Short.toUnsignedInt(getShort(unitId, address));
    }
    public short getShort(int unitId, int address) throws IOException
    {
        Thread currentThread = Thread.currentThread();
        short transaction = 0;
        sendLock.lock();
        try
        {
            transaction = putReadRegistersRequestHeader(unitId, address, 1, ()->waitForReadFinish(currentThread));
            send();
        }
        finally
        {
            sendLock.unlock();
        }
        try
        {
            waitForResponce(transaction);
            readReadRegistersResponseHeader();
            return receiveBuffer.getShort();
        }
        finally
        {
            releaseReader();
        }
    }
    public long getUnsignedInt(int unitId, int address) throws IOException
    {
        return Integer.toUnsignedLong(getInt(unitId, address));
    }
    public int getInt(int unitId, int address) throws IOException
    {
        Thread currentThread = Thread.currentThread();
        short transaction = 0;
        sendLock.lock();
        try
        {
            transaction = putReadRegistersRequestHeader(unitId, address, 2, ()->waitForReadFinish(currentThread));
            send();
        }
        finally
        {
            sendLock.unlock();
        }
        try
        {
            waitForResponce(transaction);
            readReadRegistersResponseHeader();
            return receiveBuffer.getInt();
        }
        finally
        {
            releaseReader();
        }
    }
    public void getShort(int unitId, int address, IntConsumer consumer) throws IOException
    {
        Runnable after = ()->
        {
            try
            {
                readReadRegistersResponseHeader();
                consumer.accept(receiveBuffer.getShort());
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        };
        sendLock.lock();
        try
        {
            putReadRegistersRequestHeader(unitId, address, 1, after);
            send();
        }
        finally
        {
            sendLock.unlock();
        }
    }
    public void getInt(int unitId, int address, IntConsumer consumer) throws IOException
    {
        Runnable after = ()->
        {
            try
            {
                readReadRegistersResponseHeader();
                consumer.accept(receiveBuffer.getInt());
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        };
        sendLock.lock();
        try
        {
            putReadRegistersRequestHeader(unitId, address, 2, after);
            send();
        }
        finally
        {
            sendLock.unlock();
        }
    }
    public void getString(int unitId, int address, int words, Consumer<String> consumer) throws IOException
    {
        Runnable after = ()->
        {
            try
            {
                readReadRegistersResponseHeader();
                byte[] buf = new byte[2*words];
                receiveBuffer.get(buf);
                consumer.accept(new String(buf, US_ASCII));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        };
        sendLock.lock();
        try
        {
            putReadRegistersRequestHeader(unitId, address, words, after);
            send();
        }
        finally
        {
            sendLock.unlock();
        }
    }

    private short putWriteMultipleRegistersRequestHeader(int unitId, int address, int words, Runnable after) throws IOException
    {
        short transaction = startTransaction((byte) unitId, 8, after);
        sendBuffer.put(WRITE_MULTIPLE_REGISTERS.code());
        sendBuffer.putShort((short) address);
        sendBuffer.putShort((short) words);
        sendBuffer.put((byte) (words*2));
        return transaction;
    }
    private short putReadRegistersRequestHeader(int unitId, int address, int words, Runnable after) throws IOException
    {
        short transaction = startTransaction((byte) unitId, 5, after);
        sendBuffer.put(READ_HOLDING_REGISTERS.code());
        sendBuffer.putShort((short) address);
        sendBuffer.putShort((short) words);
        return transaction;
    }
    private void send() throws IOException
    {
        sendBuffer.flip();
        channel.write(sendBuffer);
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
    protected abstract short startTransaction(byte unitId, int dataLength, Runnable after);
    protected abstract void waitForResponce(short transaction);
    protected abstract void waitForReadFinish(Thread thread);
    protected abstract void releaseReader();



}
