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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.DBCParser;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.socketcand.SocketCandService;
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
    protected final ExecutorService executor;
    protected final AbstractMessageFactory messageFactory;
    private Future<?> future;
    private ReentrantLock compileLock = new ReentrantLock();

    protected AbstractCanService(ExecutorService executor, SignalCompiler compiler)
    {
        this(executor, new DefaultMessageFactory(compiler));
    }

    public AbstractCanService(ExecutorService executor, AbstractMessageFactory messageFactory)
    {
        super(AbstractCanService.class);
        this.executor = executor;
        this.messageFactory = messageFactory;
    }

    public static AbstractCanService openSocketCand(String canBus, SignalCompiler compiler) throws IOException
    {
        return openSocketCand(canBus, new CachedScheduledThreadPool(), compiler);
    }
    public static AbstractCanService openSocketCand(String canBus, ExecutorService executor, SignalCompiler compiler) throws IOException
    {
        return new SocketCandService(canBus, executor, compiler);
    }
    public static AbstractCanService openSocketCand(String canBus, ExecutorService executor, AbstractMessageFactory messsageFactory) throws IOException
    {
        return new SocketCandService(canBus, executor, messsageFactory);
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
    public final int rawToCanId(int rawId)
    {
        if ((rawId & 0b1100000000000000000000000000000) != 0)
        {
            throw new RuntimeException("\nEFF/SFF is set in the MSB "+getHexData());
        }
        else
        {
            if ((rawId & 0b10000000000000000000000000000000) != 0)
            {
                return rawId & 0b11111111111111111111111111111;
            }
            else
            {
                return rawId;
            }
        }
    }
    final protected void rawFrame(Frame frame)
    {
        int canId = frame.getCanId();
        AbstractMessage proc = getProc(canId);
        if (proc == null)
        {
            finest("needs compiling %d", canId);
            compileLock.lock();
            try
            {
                proc = getProc(canId);
                if (proc == null)
                {
                    finest("compiling %d", canId);
                    compile(canId);
                    proc = getProc(canId);
                }
                else
                {
                    finest("had it compiled %d", canId);
                }
            }
            finally
            {
                compileLock.unlock();
            }
        }
        proc.rawUpdate(frame);
    }

    protected void compile(int canId)
    {
        AbstractMessage msg = null;
        try
        {
            MessageClass msgCls = canIdMap.get(canId);
            if (msgCls != null)
            {
                msg = compile(canId, msgCls);
                info("compiled canId %d", canId);
            }
            else
            {
                int pgn = PGN.pgn(canId);
                msgCls = pgnMap.get(pgn);
                if (msgCls != null)
                {
                    msg = compilePgn(canId, (MessageClass) msgCls);
                    info("compiled canId %d %s", canId, msgCls.getName());
                }
            }
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "compiling %d", PGN.pgn(canId));
        }
        if (msg == null)
        {
            msg = AbstractMessage.getNullMessage(executor, canId);
        }
        addProc(canId, msg);
    }
    
    private void addProc(int canId, AbstractMessage msg)
    {
        if (msg != null)
        {
            AbstractMessage old = procMap.put(PGN.addressedPgn(canId), msg);
            if (old != null)
            {
                old.unregisterMBean();
            }
            msg.registerMBean();
        }
    }

    private AbstractMessage getProc(int canId)
    {
        return procMap.get(PGN.addressedPgn(canId));
    }

    protected AbstractMessage compile(int canId, MessageClass mc)
    {
        return messageFactory.createMessage(executor, canId, mc);
    }
    protected AbstractMessage compilePgn(int canId, MessageClass mc)
    {
        return messageFactory.createPgnMessage(executor, canId, mc);
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

    protected abstract String getHexData();
}
