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
package org.vesalainen.can.candump;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CanDumpService extends AbstractCanService
{
    private final ReadableByteChannel channel;
    private String bus;

    public CanDumpService(String bus, Path path, CachedScheduledThreadPool executor, SignalCompiler compiler) throws IOException
    {
        this(bus, Files.newByteChannel(path, READ), executor, compiler);
    }
    public CanDumpService(String bus, Path path, CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory) throws IOException
    {
        this(bus, Files.newByteChannel(path, READ), executor, messageFactory);
    }
    public CanDumpService(String bus, ReadableByteChannel channel, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        super(executor, compiler);
        this.channel = channel;
        this.bus = bus;
    }
    
    public CanDumpService(String bus, ReadableByteChannel channel, CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory)
    {
        super(executor, messageFactory);
        this.bus = bus;
        this.channel = channel;
    }

    public String getBus()
    {
        return bus;
    }

    public void setBus(String bus)
    {
        this.bus = bus;
    }

    boolean isEnabled(String canBus)
    {
        return bus == null || bus.equals(canBus);
    }
    @Override
    public void run()
    {
        try
        {
            CanDumpParser parser = CanDumpParser.getInstance();
            parser.parse(channel, this);
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Override
    public void sendRaw(int canId, int length, byte[] data) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
