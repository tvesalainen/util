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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ModbusTcpClient handles modbus tcp protocol client part.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://www.modbus.org/docs/Modbus_Messaging_Implementation_Guide_V1_0b.pdf">MODBUS Messaging on TCP/IP Implementation Guide V1.0b</a>
 */
public class ModbusTcpClient extends AbstractModbusClient implements AutoCloseable
{
    public static final int TCP_MODBUS_ADU = 260;
    private final AtomicInteger transactionIdentifier = new AtomicInteger();
    private final InetSocketAddress socketAddress;
    
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
        return modbusTcp;
    }
    private ModbusTcpClient(InetSocketAddress socketAddress) throws IOException
    {
        super(SocketChannel.open(socketAddress), TCP_MODBUS_ADU);
        this.socketAddress = socketAddress;
    }

    @Override
    protected short startTransaction(byte unitId)
    {
        short transaction = (short) transactionIdentifier.getAndIncrement();
        fine("start transaction %d", transaction);
        sendBuffer.clear();
        sendBuffer.putShort(transaction);
        sendBuffer.putShort((short) 0);
        sendBuffer.putShort((short) 0); // will be set later
        sendBuffer.put(unitId);
        return transaction;
    }


    @Override
    public void close() throws IOException
    {
        channel.close();
    }

}
