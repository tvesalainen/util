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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static org.vesalainen.modbus.ExceptionCode.GATEWAY_PATH_UNAVAILABLE_0A;
import static org.vesalainen.modbus.ExceptionCode.SLAVE_DEVICE_FAILURE_04;
import static org.vesalainen.modbus.ModbusTcpClient.TCP_MODBUS_ADU;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ModbusTcpServer extends JavaLogging implements Runnable
{
    private Thread listener;
    private final int port;
    private final ExecutorService executor;
    private final Map<Byte,AbstractModbusServer> map = new HashMap<>();
    private Future<?> future;

    ModbusTcpServer()
    {
        this(Executors.newCachedThreadPool());
    }

    public ModbusTcpServer(ExecutorService executor)
    {
        this(502, executor);
    }

    public ModbusTcpServer(int port, ExecutorService executor)
    {
        super(ModbusTcpServer.class);
        this.port = port;
        this.executor = executor;
    }
    
    public void start()
    {
        future = executor.submit(this);
    }

    public void stop()
    {
        future.cancel(true);
    }
    public void addServer(byte unitId, AbstractModbusServer server)
    {
        map.put(unitId, server);
    }
    @Override
    public void run()
    {
        try
        {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(port));
            while (true)
            {
                SocketChannel sc = ssc.accept();
                Listener listener = new Listener(sc);
                executor.execute(listener);
            }
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, "%s", ex);
        }
    }
    private class Listener implements Runnable
    {
        private final SocketChannel channel;
        private final ByteBuffer in = ByteBuffer.allocateDirect(TCP_MODBUS_ADU);
        private final ByteBuffer out = ByteBuffer.allocateDirect(TCP_MODBUS_ADU);

        public Listener(SocketChannel channel)
        {
            this.channel = channel;
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    in.clear();
                    while (in.position() < 7)
                    {
                        channel.read(in);
                    }
                    short transaction = in.getShort(0);
                    short protocol = in.getShort(2);
                    short dataLength = in.getShort(4);
                    byte unitId = in.get(6);
                    int length = dataLength + 6;
                    while (in.position() < length)
                    {
                        channel.read(in);
                    }
                    byte functionCode = in.get(7);
                    in.position(7);
                    out.clear();
                    out.putShort(transaction);
                    out.putShort(protocol);
                    out.putShort(dataLength);
                    out.put(unitId);
                    out.mark();
                    try
                    {
                        AbstractModbusServer server = map.get(unitId);
                        if (server != null)
                        {
                            server.handleRequest(in, out);
                        }
                        else
                        {
                            throw new ModbusException(GATEWAY_PATH_UNAVAILABLE_0A);
                        }
                    }
                    catch (ModbusException ex)
                    {
                        out.reset();
                        out.putShort(4, (short)3);  // data length
                        out.put((byte) (functionCode | 0x80));
                        out.put((byte) ex.getExceptionCode().ordinal());
                    }
                    catch (Throwable ex)
                    {
                        out.reset();
                        out.putShort(4, (short)3);  // data length
                        out.putShort(4, (short)3);
                        out.put((byte) (functionCode | 0x80));
                        out.put((byte) SLAVE_DEVICE_FAILURE_04.ordinal());
                        log(SEVERE, ex, "Function code %d, %s", functionCode, ex.getMessage());
                    }
                    out.flip();
                    channel.write(out);
                }
                catch (IOException ex)
                {
                    log(Level.SEVERE, "%s", ex);
                }
            }
        }
        
    }
}
