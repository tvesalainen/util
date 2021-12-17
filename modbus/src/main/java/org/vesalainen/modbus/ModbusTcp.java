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
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ModbusTcp extends AbstractModbus implements Runnable, AutoCloseable
{
    private final AtomicInteger transactionIdentifier = new AtomicInteger();
    private final InetSocketAddress socketAddress;
    private SocketChannel channel;
    private Thread reader;
    private Map<Short,Thread> transactionMap = new HashMap<>();
    private volatile boolean waitingForReadingFinish;
    
    public static ModbusTcp open(String inetAddress) throws IOException
    {
        return open(InetAddress.getByName(inetAddress));
    }
    public static ModbusTcp open(InetAddress inetAddress) throws IOException
    {
        return open(new InetSocketAddress(inetAddress, 502));
    }
    public static ModbusTcp open(InetSocketAddress socketAddress) throws IOException
    {
        ModbusTcp modbusTcp = new ModbusTcp(socketAddress);
        modbusTcp.open();
        return modbusTcp;
    }
    private ModbusTcp(InetSocketAddress socketAddress) throws IOException
    {
        super(260);
        this.socketAddress = socketAddress;
    }
    private void open() throws IOException
    {
        this.channel = SocketChannel.open(socketAddress);
        this.reader = new Thread(this, "ModbusTcp reader");
        reader.start();
    }

    @Override
    protected short startTransaction(byte unitId, int dataLength)
    {
        short id = (short) transactionIdentifier.getAndIncrement();
        sendBuffer.clear();
        sendBuffer.putShort(id);
        sendBuffer.putShort((short) 0);
        sendBuffer.putShort((short) (dataLength+1));
        sendBuffer.put(unitId);
        return id;
    }

    @Override
    protected void waitFor(short transaction)
    {
        transactionMap.put(transaction, Thread.currentThread());
        while (transactionMap.containsKey(transaction))
        {
            LockSupport.park();
        }
    }

    @Override
    protected void send() throws IOException
    {
        sendBuffer.flip();
        channel.write(sendBuffer);
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
            log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() throws Exception
    {
        channel.close();
    }

    private void processReceived()
    {
        short transactionId = receiveBuffer.getShort();
        short protocolId = receiveBuffer.getShort();
        short length = receiveBuffer.getShort();
        if (receiveBuffer.remaining() < length)
        {
            throw new BufferUnderflowException();
        }
        byte unitId = receiveBuffer.get();
        Thread thread = transactionMap.remove(transactionId);
        waitingForReadingFinish = true;
        LockSupport.unpark(thread);
        while (waitingForReadingFinish)
        {
            LockSupport.park();
        }
    }

    @Override
    protected void releaseReader()
    {
        waitingForReadingFinish = false;
        LockSupport.unpark(reader);
    }
    
}
