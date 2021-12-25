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

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;

/**
 * ModbusTcpClient handles modbus tcp protocol.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://www.modbus.org/docs/Modbus_Messaging_Implementation_Guide_V1_0b.pdf">MODBUS Messaging on TCP/IP Implementation Guide V1.0b</a>
 */
public class ModbusTcpClient extends AbstractModbusClient implements Runnable, AutoCloseable
{
    private final AtomicInteger transactionIdentifier = new AtomicInteger();
    private final InetSocketAddress socketAddress;
    private Thread reader;
    private Map<Short,Runnable> transactionMap = new HashMap<>();
    private volatile boolean waitingForReadingFinish;
    
    public static ModbusTcpClient open(String inetAddress) throws IOException
    {
        return open(InetAddress.getByName(inetAddress));
    }
    public static ModbusTcpClient open(InetAddress inetAddress) throws IOException
    {
        return open(new InetSocketAddress(inetAddress, 502));
    }
    public static ModbusTcpClient open(InetSocketAddress socketAddress) throws IOException
    {
        ModbusTcpClient modbusTcp = new ModbusTcpClient(socketAddress);
        modbusTcp.open();
        return modbusTcp;
    }
    private ModbusTcpClient(InetSocketAddress socketAddress) throws IOException
    {
        super(SocketChannel.open(socketAddress), 260);
        this.socketAddress = socketAddress;
    }
    private void open() throws IOException
    {
        this.reader = new Thread(this, "ModbusTcp reader");
        reader.start();
    }

    @Override
    protected short startTransaction(byte unitId, int dataLength, Runnable after)
    {
        short transaction = (short) transactionIdentifier.getAndIncrement();
        fine("start transaction %d", transaction);
        transactionMap.put(transaction, after);
        sendBuffer.clear();
        sendBuffer.putShort(transaction);
        sendBuffer.putShort((short) 0);
        sendBuffer.putShort((short) (dataLength+1));
        sendBuffer.put(unitId);
        return transaction;
    }

    @Override
    protected void waitForResponce(short transaction)
    {
        fine("wait for transaction %d", transaction);
        while (transactionMap.containsKey(transaction))
        {
            LockSupport.park();
        }
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                int rc = channel.read(receiveBuffer);
                if (rc == -1)
                {
                    throw new EOFException();
                }
                receiveBuffer.flip();
                while (receiveBuffer.hasRemaining())
                {
                    receiveBuffer.mark();
                    try
                    {
                        processReceived();
                    }
                    catch (BufferUnderflowException ex)
                    {
                        receiveBuffer.reset();
                        receiveBuffer.compact();
                        break;  // partially read responce
                    }
                }
                if (!receiveBuffer.hasRemaining())
                {
                    receiveBuffer.clear();  // all were read
                }
            }
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "%s", ex);
        }
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
    }

    private void processReceived()
    {
        short transactionId = receiveBuffer.getShort();
        fine("received %d", transactionId);
        short protocolId = receiveBuffer.getShort();
        short length = receiveBuffer.getShort();
        if (receiveBuffer.remaining() < length)
        {
            throw new BufferUnderflowException();
        }
        byte unitId = receiveBuffer.get();
        Runnable call = transactionMap.remove(transactionId);
        call.run();
    }

    @Override
    protected void waitForReadFinish(Thread thread)
    {
        fine("release thread %s", thread.getName());
        waitingForReadingFinish = true;
        LockSupport.unpark(thread);
        while (waitingForReadingFinish)
        {
            LockSupport.park();
        }
        fine("released thread %s", thread.getName());
    }

    @Override
    protected void releaseReader()
    {
        if (waitingForReadingFinish)
        {
            waitingForReadingFinish = false;
            LockSupport.unpark(reader);
        }
    }
    
}
