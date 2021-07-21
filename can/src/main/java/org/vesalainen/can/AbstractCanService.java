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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.DBCParser;
import org.vesalainen.can.dbc.MessageClass;
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
    protected final Map<Integer,AbstractMessage> procMap = new ConcurrentHashMap<>();
    protected final Map<Integer,MessageClass> canIdMap = new HashMap<>();
    protected final Map<Integer,MessageClass> pgnMap = new HashMap<>();
    protected final CachedScheduledThreadPool executor;
    protected final SignalCompiler compiler;
    private Future<?> future;

    protected AbstractCanService(CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        super(AbstractCanService.class);
        this.executor = executor;
        this.compiler = compiler;
    }

    public static AbstractCanService openSocketCan2Udp(String host, int port, SignalCompiler compiler) throws IOException
    {
        return openSocketCan2Udp(host, port, new CachedScheduledThreadPool(), compiler);
    }
    public static AbstractCanService openSocketCan2Udp(String host, int port, CachedScheduledThreadPool executor, SignalCompiler compiler) throws IOException
    {
        UnconnectedDatagramChannel udc = UnconnectedDatagramChannel.open(host, port, 16, true, false);
        return new SocketCanService((ByteChannel)udc, executor, compiler);
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
            procMap.put(canId, AbstractMessage.NULL_MESSAGE);
            executor.submit((Runnable) ()->compile(canId));
        }
    }

    public abstract ByteBuffer getFrame();
    
    public abstract int getLength();

    public abstract void readData(byte[] data, int offset);
    
    protected void compile(int canId)
    {
        try
        {
            MessageClass msgCls = canIdMap.get(canId);
            if (msgCls != null)
            {
                procMap.put(canId, compile(canId, msgCls));
                info("compiled canId %d", canId);
            }
            else
            {
                int pgn = PGN.pgn(canId);
                msgCls = pgnMap.get(pgn);
                if (msgCls != null)
                {
                    procMap.put(canId, compilePGN(canId, (MessageClass) msgCls));
                    info("compiled canId %d %s", canId, msgCls.getName());
                }
            }
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "compiling %d", PGN.pgn(canId));
        }
    }
    
    protected AbstractMessage compile(int canId, MessageClass mc)
    {
        SingleMessage sm = new SingleMessage(canId, mc.getMinSize(), mc.getName());
        finer("compile(%s)", mc);
        addSignals(mc, sm);
        return sm;
    }
    protected AbstractMessage compilePGN(int canId, MessageClass mc)
    {
        SingleMessage sm;
        switch ((String)mc.getAttributeValue("MessageType"))
        {
            case "Single":
                sm = new SingleMessage(canId, mc.getMinSize(), mc.getName());
                break;
            case "Fast":
                sm = new FastMessage(canId, mc.getMinSize(), mc.getName());
                break;
            default:
                throw new UnsupportedOperationException(mc.getAttributeValue("MessageType")+"not supported");
        }
        finer("compile(%s)", mc);
        if (mc.getName().startsWith("ais"))
        {
            finer("ais(%s)", mc);
        }
        addSignals(mc, sm);
        return sm;
    }
    private void addSignals(MessageClass mc, SingleMessage sm)
    {
        sm.addBegin(mc, compiler);
        mc.forEach((s)->
        {
            finer("add signal %s", s);
            sm.addSignal(mc, s, compiler);
        });
        sm.addEnd(mc, compiler);
    }
    
    public void addDBCFile(Path path)
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        parser.parse(path, dbcFile);
        String protocolType = (String)dbcFile.getAttributeValue("ProtocolType");
        switch (protocolType)
        {
            case "":
            case "StandardDBC":
                dbcFile.forEach((mc)->
                {
                    canIdMap.put(mc.getId(), mc);
                });
                break;
            case "N2K":
                dbcFile.forEach((mc)->
                {
                    pgnMap.put(mc.getId(), mc);
                });
                break;
            default:
                throw new UnsupportedOperationException(protocolType+" not supported");
        }
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
