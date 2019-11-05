/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.net.sntp;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Clock;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.NANOS;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.Supplier;
import static java.util.logging.Level.*;
import org.vesalainen.math.MoreMath;
import static org.vesalainen.net.sntp.NtpV4Packet.Mode.*;
import static org.vesalainen.net.sntp.ReferenceClock.*;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.time.AdjustableClock;
import org.vesalainen.time.SimpleAdjustableClock;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SNTPServer extends JavaLogging implements Runnable
{
    private static final int PORT = 123;    // NTP port number                  |
    private static final int VERSION = 4;    // NTP version number                   |
    private static final double TOLERANCE = 15e-6;    // frequency tolerance PHI (s/s)    |
    private static final int MINPOLL = 4;    // minimum poll exponent (16 s)     |
    private static final int MAXPOLL = 17;    // maximum poll exponent (36 h)     |
    private static final int MAXDISP = 16;    // maximum dispersion (16 s)        |
    private static final double MINDISP = .005;    // minimum dispersion increment (s) |
    private static final int MAXDIST = 1;    // distance threshold (1 s)         |
    private static final int MAXSTRAT = 16;    // maximum stratum number     
    
    private static final int SERVER_TIMEOUT_MINUTES = 10;
    private static final long SERVER_TIMEOUT = MILLISECONDS.convert(SERVER_TIMEOUT_MINUTES, MINUTES);
    private static final long B16 = 65536;
    private final AdjustableClock clock;
    private final CachedScheduledThreadPool executor;
    private Future<?> future;
    private final int poll;
    private final int precision;
    private ReferenceClock referenceClock;
    private Supplier<Instant> reference;
    private Server server;
    private long request;
    private UnconnectedDatagramChannel channel4;
    private UnconnectedDatagramChannel channel6;
    private List<InetAddress> ownAddresses = new ArrayList<>();

    public SNTPServer()
    {
        this(6, 0, GPS, Clock.systemUTC(), new CachedScheduledThreadPool());
    }
    /**
     * 
     * @param poll log2
     * @param rootDelay in millis
     * @param referenceClock
     * @param clock
     * @param executor 
     */
    public SNTPServer(int poll, long rootDelay, ReferenceClock referenceClock, Clock clock, CachedScheduledThreadPool executor)
    {
        super(SNTPServer.class);
        if (poll < MINPOLL || poll > MAXPOLL)
        {
            throw new IllegalArgumentException("poll out of range");
        }
        this.poll = poll;
        this.referenceClock = referenceClock;
        if (clock instanceof AdjustableClock)
        {
            this.clock = (AdjustableClock) clock;
            this.clock.adjust(rootDelay*1000000L);
        }
        else
        {
            this.clock = new SimpleAdjustableClock(clock, rootDelay*1000000L);
        }
        this.reference = reference;
        this.executor = executor;
        this.precision = calcPrecision();
        config("precision=%d", precision);
    }
    private int calcPrecision()
    {
        Instant i1;
        Instant i0 = i1 = clock.instant();
        while (i0.equals(i1))
        {
            i1 = clock.instant();
        }
        config("%s", i0);
        config("%s", i1);
        long prec = i0.until(i1, NANOS);
        double s = prec / 1000000000.0;
        return (int) -Math.round(MoreMath.log(2, 1.0/s));
    }
    private int millis2Short(long millis)
    {
        return (int) ((millis * B16) / 1000);
    }
    private int nano2Short(long nanos)
    {
        return (int) ((nanos * B16) / 1000000000);
    }
    private int pow2(int exp)
    {
        int p = 2;
        for (int ii=1;ii<exp;ii++)
        {
            p *= 2;
        }
        return p;
    }
    private Instant instant()
    {
        return clock.instant();
    }
    private Instant reference()
    {
        return clock.reference();
    }
    public long offset()
    {
        return clock.offset()/1000000L;
    }
    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException("already running");
        }
        future = executor.submit(this);
    }

    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException("not running");
        }
        future.cancel(true);
        future = null;
    }

    public void setServer(String host)
    {
        try
        {
            for (InetAddress address : InetAddress.getAllByName(host))
            {
                setServer(new InetSocketAddress(address, 123));
            }
        }
        catch (UnknownHostException ex)
        {
            warning("host %s unknown", host);
        }
    }
    public void setServer(InetSocketAddress address)
    {
        if (server != null)
        {
            throw new IllegalArgumentException();
        }
        config("set NTP server %s", address);
        server = new Server(address);
        server.schedule();
    }
    @Override
    public void run()
    {
        try
        {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements())
            {
                NetworkInterface ni = nis.nextElement();
                if (ni.isUp())
                {
                    config("Network Interface %s", ni);
                    Enumeration<InetAddress> ia = ni.getInetAddresses();
                    while (ia.hasMoreElements())
                    {
                        InetAddress addr = ia.nextElement();
                        config("  %s", addr.getHostAddress());
                        if (!addr.isLoopbackAddress())
                        {
                            ownAddresses.add(addr);
                        }
                    }
                }
            }
            channel4 = UnconnectedDatagramChannel.open("0.0.0.0", 123, 64, true, true);
            channel4.configureBlocking(false);
            config("SNTP listening %s", channel4);
            
            channel6 = UnconnectedDatagramChannel.open("::", 123, 64, true, true);
            channel6.configureBlocking(false);
            config("SNTP listening %s", channel6);
            
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            Selector selector = Selector.open();
            channel4.register(selector, SelectionKey.OP_READ, channel4);
            channel6.register(selector, SelectionKey.OP_READ, channel6);
            while (true)
            {
                int count = selector.select();
                Instant instant = instant();
                for (SelectionKey sk : selector.selectedKeys())
                {
                    UnconnectedDatagramChannel rc = (UnconnectedDatagramChannel) sk.attachment();
                    int cnt = rc.read(bb);
                    if (cnt > 0)
                    {
                        handlePacket(rc, bb, instant);
                    }
                    bb.clear();
                }
            }
        }
        catch (Throwable ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
        finally
        {
            future = null;
        }
    }

    protected void handlePacket(UnconnectedDatagramChannel channel, ByteBuffer bb, Instant rcvTime) throws IOException
    {
        NtpV4Packet message = new NtpV4Packet(bb);
        InetSocketAddress addr = channel.getFromAddress();
        fine("NTP packet %s %s from %s", message.getMode(), message.getReferenceIdString(), addr);
        switch (message.getMode())
        {
            case CLIENT:
                handleClient(channel, rcvTime, message, addr);
                break;
            case SERVER:
                handleServer(rcvTime, message, addr);
                break;
            default:
                warning("NTP packet %s %s from %s", message.getMode(), message.getReferenceIdString(), addr);
        }
    }

    private void handleServer(Instant t3, NtpV4Packet message, InetSocketAddress address) throws IOException
    {
        long originateTime = message.getOriginateTime();
        if (originateTime == request && !message.referenceEquals(ownAddresses))
        {
            server.set(message, t3);
        }
    }
    private void handleClient(UnconnectedDatagramChannel channel, Instant rcvTime, NtpV4Packet message, InetSocketAddress addr) throws IOException
    {
        NtpV4Packet response = new NtpV4Packet();

        response.setStratum(1);
        response.setMode(SERVER);
        response.setPrecision(precision);
        response.setPoll(poll);
        response.setRootDelayRaw(0);
        response.setRootDispersionRaw(0);

        // originate time as defined in RFC-1305 (t1)
        response.setOriginateTime(message.getTransmitTime());
        // Receive Time is time request received by server (t2)
        response.setReceiveInstant(rcvTime);
        response.setReferenceInstant(reference());
        response.setReferenceId(referenceClock);

        // Transmit time is time reply sent by server (t3)
        response.setTransmitInstant(instant());

        ByteBuffer buffer = response.getBuffer();
        buffer.clear();
        channel.send(buffer, addr);
    }
    private long sendRequest(InetSocketAddress address) throws IOException
    {
        NtpV4Packet request = new NtpV4Packet();

        request.setStratum(1);
        request.setMode(CLIENT);
        request.setPrecision(precision);
        request.setPoll(poll);
        request.setRootDelayRaw(0);
        request.setRootDispersionRaw(0);
        request.setReferenceId(referenceClock);

        request.setTransmitInstant(instant());

        ByteBuffer buffer = request.getBuffer();
        buffer.clear();
        if (address.getAddress() instanceof Inet6Address)
        {
            channel6.send(buffer, address);
        }
        else
        {
            channel4.send(buffer, address);
        }
        return request.getTransmitTime();
    }
    private class Server
    {
        private InetSocketAddress address;
        private int stratum;
        private int poll;
        private int precision;
        private int rootDelay;
        private int rootDispersion;
        private String referenceId;
        private Instant referenceTimestamp;
        private ScheduledFuture<?> future;
        private int rate = 64;

        public Server(InetSocketAddress address)
        {
            this.address = address;
        }
        
        public void set(NtpV4Packet message, Instant t4)
        {
            stratum = message.getStratum();
            if (stratum == 0)
            {
                String kissCode = message.getReferenceIdString();
                fine("KoD %s", kissCode);
                switch (kissCode)
                {
                    case "DENY":
                    case "RSTR":
                        future.cancel(true);
                        break;
                    case "RATE":
                        rate *= 2;
                        schedule();
                        break;
                }
            }
            else
            {
                poll = message.getPoll();
                precision = message.getPrecision();
                rootDelay = message.getRootDelayRaw();
                rootDispersion = message.getRootDispersionRaw();
                referenceId = message.getReferenceIdString();
                referenceTimestamp = message.getReferenceInstant();

                Instant t1 = message.getOriginateInstant();
                Instant t2 = message.getReceiveInstant();
                Instant t3 = message.getTransmitInstant();
                // offset = ((t2-t1)+(t3-t4))/2
                long d12 = t1.until(t2, NANOS);
                long d34 = t4.until(t3, NANOS);

                long off = (d12 + d34) / 2L;
                clock.adjust(off);
                // delay = (t4-t1) - (t3-t2)
                long d14 = t1.until(t4, NANOS);
                long d23 = t2.until(t3, NANOS);

                long del = d14 - d23;
                finest("t1=%s", t1);
                finest("t2=%s", t2);
                finest("t3=%s", t3);
                finest("t4=%s", t4);
                fine("%s offset=%f delay=%f rt=%f", address, (double)off/1000000000.0, (double)del/1000000000.0, (double)clock.offset()/1000000000.0);
            }
        }

        public void schedule()
        {
            if (future != null)
            {
                future.cancel(true);
            }
            future = executor.scheduleWithFixedDelay(this::poll, rate, rate, SECONDS);
        }
        private void poll()
        {
            try
            {
                request = sendRequest(address);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        @Override
        public String toString()
        {
            return "Server{" + "address=" + address + '}';
        }
        
    }
}
