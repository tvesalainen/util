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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.can.cannelloni.CannelloniService;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.socketcand.SocketCandService;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractCanService extends JavaLogging implements Frame, Runnable, AutoCloseable
{
    protected final static int CAN_EFF_FLAG = 0x80000000; /* EFF/SFF is set in the MSB */
    protected final static int CAN_RTR_FLAG = 0x40000000; /* remote transmission request */
    protected final static int CAN_ERR_FLAG = 0x20000000; /* error frame */

    /* valid bits in CAN ID for frame formats */
    protected final static int CAN_SFF_MASK = 0x000007FF; /* standard frame format (SFF) */
    protected final static int CAN_EFF_MASK = 0x1FFFFFFF; /* extended frame format (EFF) */
    protected final static int CAN_ERR_MASK = 0x1FFFFFFF; /* omit EFF, RTR, ERR flags */
    
    protected final Map<Integer,Frame> queueMap = new HashMap<>();
    protected final FrameQueue defaultQueue = new FrameQueue(8192, 0, this, "Default");
    protected final Map<Integer,AbstractMessage> procMap = new ConcurrentHashMap<>();
    protected final Map<Integer,PgnHandler> pgnHandlers = new HashMap<>();
    protected final CachedScheduledThreadPool executor;
    protected final AbstractMessageFactory messageFactory;
    private Future<?> future;
    private List<Runnable> startables = new ArrayList<>();

    protected AbstractCanService(CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        this(executor, new DefaultMessageFactory(compiler));
    }

    public AbstractCanService(CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory)
    {
        super(AbstractCanService.class);
        this.executor = executor;
        this.messageFactory = messageFactory;
        executor.execute(defaultQueue);
    }

    public static AbstractCanService openCannelloni(String address, int local, int remote, int bufferSize, SignalCompiler compiler) throws IOException
    {
        return openCannelloni(address, local, remote, bufferSize, new CachedScheduledThreadPool(), compiler);
    }
    public static AbstractCanService openCannelloni(String address, int local, int remote, int bufferSize, CachedScheduledThreadPool executor, SignalCompiler compiler) throws IOException
    {
        return new CannelloniService(address, local, remote, bufferSize, executor, compiler);
    }
    public static AbstractCanService openCannelloni(String address, int local, int remote, int bufferSize, CachedScheduledThreadPool executor, AbstractMessageFactory messsageFactory) throws IOException
    {
        return new CannelloniService(address, local, remote, bufferSize, executor, messsageFactory);
    }
    public static AbstractCanService openSocketCand(String canBus, SignalCompiler compiler) throws IOException
    {
        return openSocketCand(canBus, new CachedScheduledThreadPool(), compiler);
    }
    public static AbstractCanService openSocketCand(String canBus, CachedScheduledThreadPool executor, SignalCompiler compiler) throws IOException
    {
        return new SocketCandService(canBus, executor, compiler);
    }
    public static AbstractCanService openSocketCand(String canBus, CachedScheduledThreadPool executor, AbstractMessageFactory messsageFactory) throws IOException
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
    protected void started()
    {
        startables.forEach((r)->executor.submit(r));
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
            throw new RuntimeException("\nEFF/SFF is set in the MSB ");
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

    public void queue(long time, int canId, int dataLength, long data)
    {
        Frame frame = queueMap.get(PGN.addressedPgn(canId));
        if (frame != null)
        {
            frame.frame(time, canId, dataLength, data);
        }
        else
        {
            defaultQueue.frame(time, canId, dataLength, data);
        }
    }
    
    public void send(int canId, byte... data) throws IOException
    {
        send(canId, data.length, DataUtil.asLong(data));
    }
    public abstract void send(int canId, int length, long data) throws IOException;
    @Override
    public void frame(long time, int canId, int dataLength, long data)
    {
        PgnHandler pgnHandler = pgnHandlers.get(PGN.pgn(canId));
        if (pgnHandler != null)
        {
            pgnHandler.frame(time, canId, dataLength, data);
        }
        else
        {
            Frame frame = queueMap.get(PGN.addressedPgn(canId));
            if (frame != null)
            {
                frame.frame(time, canId, dataLength, data);
            }
            else
            {
                AbstractMessage proc = getProc(canId);
                if (proc == null)
                {
                    finest("needs compiling %d", canId);
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
                proc.frame(time, canId, dataLength, data);
            }
        }
    }
    public void addN2K()
    {
        DBC.addN2K();
    }
    public <T> void addDBCFile(T path)
    {
        DBC.addDBCFile(path);
    }
    protected void compile(int canId)
    {
        AbstractMessage msg = null;
        try
        {
            MessageClass msgCls = DBC.getMessage(canId);
            if (msgCls != null)
            {
                msg = compile(canId, msgCls);
                info("compiled canId %d", canId);
            }
            else
            {
                int pgn = PGN.pgn(canId);
                msgCls = DBC.getPgnMessage(pgn);
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
            if (msg.hasSignals())
            {
                FrameQueue frameQueue = new FrameQueue(1024, canId, msg, msg.getComment());
                executor.execute(frameQueue);
                queueMap.put(PGN.addressedPgn(canId), frameQueue);
                info("made own queue for %d %d %s", canId, PGN.addressedPgn(canId), msg.getComment());
            }
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
    public void addPgnHandler(PgnHandler pgnHandler)
    {
        pgnHandler.init(this, executor);
        for (int pgn : pgnHandler.pgnsToHandle())
        {
            MessageClass mc = DBC.getPgnMessage(pgn);
            if (mc != null)
            {
                pgnHandler.init(pgn, mc);
            }
            else
            {
                throw new UnsupportedOperationException(pgn+" PGN not supported");
            }
            pgnHandlers.put(pgn, pgnHandler);
        }
        startables.add(pgnHandler::start);
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
