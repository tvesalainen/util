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
    protected final MessageFactory messageFactory;
    private Future<?> future;

    protected AbstractCanService(CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        this(executor, new MessageFactory(executor, compiler));
    }

    public AbstractCanService(CachedScheduledThreadPool executor, MessageFactory messageFactory)
    {
        super(AbstractCanService.class);
        this.executor = executor;
        this.messageFactory = messageFactory;
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
                proc.execute();
            }
        }
        else
        {
            addProc(canId, AbstractMessage.getNullMessage(executor, canId));
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
                addProc(canId, compile(canId, msgCls));
                info("compiled canId %d", canId);
            }
            else
            {
                int pgn = PGN.pgn(canId);
                msgCls = pgnMap.get(pgn);
                if (msgCls != null)
                {
                    addProc(canId, compilePgn(canId, (MessageClass) msgCls));
                    info("compiled canId %d %s", canId, msgCls.getName());
                }
            }
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "compiling %d", PGN.pgn(canId));
        }
    }
    
    private void addProc(int canId, AbstractMessage msg)
    {
        if (msg != null)
        {
            AbstractMessage old = procMap.put(canId, msg);
            if (old != null)
            {
                old.unregisterMBean();
            }
            msg.registerMBean();
        }
    }

    protected AbstractMessage compile(int canId, MessageClass mc)
    {
        return messageFactory.createMessage(canId, mc);
    }
    protected AbstractMessage compilePgn(int canId, MessageClass mc)
    {
        return messageFactory.createPgnMessage(canId, mc);
    }
    public void addN2K()
    {
        addDBCFile(AbstractCanService.class.getResourceAsStream("/n2k.dbc"));
    }
    public <T> void addDBCFile(T path)
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        parser.parse(path, dbcFile);
        String protocolType = (String)dbcFile.getAttributeValue("ProtocolType");
        if (protocolType == null)
        {
            protocolType = "";
        }
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
                    pgnMap.put(PGN.pgn(mc.getId()), mc);
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
