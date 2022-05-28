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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.can.can2udp.Can2UdpService;
import org.vesalainen.can.cannelloni.CannelloniService;
import org.vesalainen.can.dbc.DBC;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.j1939.AddressManager;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.socketcand.SocketCandService;
import org.vesalainen.nio.ReadBuffer;
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
    
    protected final Map<Integer,AbstractMessage> procMap = new HashMap<>();
    protected AddressManager addressManager;
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
    }

    public static AbstractCanService openCan2Udp(String address, int local, SignalCompiler compiler) throws IOException
    {
        return openCan2Udp(address, local, new CachedScheduledThreadPool(), compiler);
    }
    public static AbstractCanService openCan2Udp(String address, int local, CachedScheduledThreadPool executor, SignalCompiler compiler) throws IOException
    {
        return new Can2UdpService(address, local, executor, compiler);
    }
    public static AbstractCanService openCan2Udp(String address, int local, CachedScheduledThreadPool executor, AbstractMessageFactory messsageFactory) throws IOException
    {
        return new Can2UdpService(address, local, executor, messsageFactory);
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

    public void sendPgn(int pgn, int priority, byte... data) throws IOException
    {
        sendPgn(pgn, priority, data.length, data);
    }
    public void sendPgn(int pgn, byte to, int priority, byte... data) throws IOException
    {
        sendPgn(pgn, to, priority, data.length, data);
    }
    public void sendPgn(int pgn, byte to, int priority, int length, byte[] data) throws IOException
    {
        if (addressManager != null)
        {
            byte ownSA = addressManager.getOwnSA();
            if (AddressManager.isPeer(ownSA))
            {
                int pf = PGN.pduFormat(pgn);
                if (pf < 0xf0)
                {
                    int canId = PGN.canId(priority, pgn, to, ownSA);
                    sendRaw(canId, length, data);
                }
                else
                {
                    throw new IllegalArgumentException("pgn is not peer-to-peer");
                }
            }
            else
            {
                throw new IllegalArgumentException("no own address");
            }
        }
        else
        {
            throw new IllegalArgumentException("no addressManager");
        }
    }
    public void sendPgn(int pgn, int priority, int length, byte[] data) throws IOException
    {
        if (addressManager != null)
        {
            byte ownSA = addressManager.getOwnSA();
            if (AddressManager.isPeer(ownSA))
            {
                int pf = PGN.pduFormat(pgn);
                if (pf >= 0xf0)
                {
                    int canId = PGN.canId(priority, pgn, ownSA);
                    sendRaw(canId, length, data);
                }
                else
                {
                    throw new IllegalArgumentException("pgn is peer-to-peer");
                }
            }
            else
            {
                throw new IllegalArgumentException("no own address");
            }
        }
        else
        {
            throw new IllegalArgumentException("no addressManager");
        }
    }
    public void sendRaw(int canId, byte... data) throws IOException
    {
        sendRaw(canId, data.length, data);
    }
    public abstract void sendRaw(int canId, int length, byte[] data) throws IOException;

    @Override
    public void frame(long time, int canId, ReadBuffer data)
    {
        if (addressManager != null)
        {
            addressManager.frame(time, canId, data);
        }
        AbstractMessage proc = getProc(canId);
        if (proc == null)
        {
            finest("needs compiling %d pgn %d", canId, PGN.pgn(canId));
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
        proc.frame(time, canId, data);
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
                fine("compiled canId %d", canId);
            }
            else
            {
                int pgn = PGN.pgn(canId);
                msgCls = DBC.getPgnMessage(pgn);
                if (msgCls != null)
                {
                    msg = compilePgn(canId, (MessageClass) msgCls);
                    fine("compiled canId %d %s", canId, msgCls.getName());
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
            AbstractMessage old = procMap.put(canId, msg);
            if (old != null)
            {
                old.unregisterMBean();
            }
            msg.registerMBean();
        }
    }

    private AbstractMessage getProc(int canId)
    {
        return procMap.get(canId);
    }

    public AbstractMessage compile(int canId, MessageClass mc)
    {
        return messageFactory.createMessage(executor, canId, mc);
    }
    public AbstractMessage compilePgn(int canId, MessageClass mc)
    {
        return messageFactory.createPgnMessage(executor, canId, mc);
    }
    public void setAddressManager(AddressManager addressManager)
    {
        if (future != null)
        {
            throw new IllegalStateException("Started already. Add AddressManager before started!");
        }
        addressManager.init(this, executor);
        this.addressManager = addressManager;
        startables.add(addressManager::start);
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
