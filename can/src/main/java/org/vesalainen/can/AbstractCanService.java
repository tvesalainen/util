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
package org.vesalainen.can;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.DBCParser;
import org.vesalainen.can.dict.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractCanService extends JavaLogging implements Runnable, AutoCloseable
{
    protected Map<Integer,AbstractMessage> procMap = new ConcurrentHashMap<>();
    protected Map<Integer,MessageClass> canIdMap = new HashMap<>();
    protected Map<Integer,MessageClass> pgnMap = new HashMap<>();
    protected CachedScheduledThreadPool executor;
    private Future<?> future;

    protected AbstractCanService(CachedScheduledThreadPool executor)
    {
        super(AbstractCanService.class);
        this.executor = executor;
    }

    public static AbstractCanService openSocketCan2Udp(String host, int port) throws IOException
    {
        return openSocketCan2Udp(host, port, new CachedScheduledThreadPool());
    }
    public static AbstractCanService openSocketCan2Udp(String host, int port, CachedScheduledThreadPool executor) throws IOException
    {
        UnconnectedDatagramChannel udc = UnconnectedDatagramChannel.open(host, port, 16, true, false);
        return new SocketCanService((ByteChannel)udc, executor);
    }
    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException("started already");
        }
        future = executor.submit(this);
    }
    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException("not started");
        }
        future.cancel(true);
        future = null;
    }
    public void startAndWait() throws InterruptedException, ExecutionException
    {
        start();
        future.get();
    }
    final protected void rawFrame(int canId)
    {
        AbstractMessage proc = procMap.get(canId);
        if (proc != null)
        {
            if (proc.update(this))
            {
                proc.execute(executor);
            }
        }
        else
        {
            procMap.put(canId, NullMessage.NULL_MESSAGE);
            executor.submit((Runnable) ()->compile(canId));
        }
    }

    protected abstract int getLength();

    protected abstract void readData(byte[] data, int offset);
    
    protected void compile(int canId)
    {
        MessageClass msgCls = canIdMap.get(canId);
        if (msgCls != null)
        {
            procMap.put(canId, compile(msgCls));
        }
        else
        {
            int pgn = PGN.pgn(canId);
            msgCls = pgnMap.get(pgn);
            if (msgCls != null)
            {
                procMap.put(canId, compile(msgCls));
            }
        }
    }
    
    protected AbstractMessage compile(MessageClass mc)
    {
        SignalMessage sm = new SignalMessage(mc.getSize());
        mc.forEach((s)->
        {
            sm.addSignal(s.getStartBit(), s.getSize(), s.getByteOrder(), s.getFactor(), s.getFactor(), (d)->System.err.println(s.getName()+"="+d));
        });
        return sm;
    }
    
    public void addDBCFile(Path path)
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        parser.parse(path, dbcFile);
        dbcFile.forEach((mc)->
        {
            canIdMap.put(mc.getId(), mc);
        });
    }
    @Override
    public void close() throws Exception
    {
        if (future != null)
        {
            stop();
        }
    }

}
