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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.NANOS;
import java.util.concurrent.Future;
import java.util.function.LongSupplier;
import static java.util.logging.Level.*;
import org.apache.commons.net.ntp.NtpUtils;
import static org.apache.commons.net.ntp.NtpV3Packet.*;
import org.apache.commons.net.ntp.TimeStamp;
import org.vesalainen.math.MoreMath;
import static org.vesalainen.net.sntp.NtpV4Packet.Mode.*;
import static org.vesalainen.net.sntp.ReferenceClock.*;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SNTPServer extends JavaLogging implements Runnable
{
    private static final int INIT_LOOP = 10;
    private static final long B16 = 65536;
    private final Clock clock;
    private final CachedScheduledThreadPool executor;
    private Future<?> future;
    private final int poll;
    private final long rootDelay;
    private final int rootDispersion;
    private final int precision;
    private ReferenceClock referenceClock;

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
        if (poll < NTP_MINPOLL || poll > NTP_MAXPOLL)
        {
            throw new IllegalArgumentException("poll out of range");
        }
        this.poll = poll;
        this.referenceClock = referenceClock;
        this.clock = clock;
        this.executor = executor;
        this.precision = calcPrecision();
        config("precision=%d", precision);
        this.rootDelay = rootDelay;
        this.rootDispersion = 0;
    }
    private int calcPrecision()
    {
        Instant i1;
        Instant i0 = i1 = clock.instant();
        config("%s", i0);
        while (i0.equals(i1))
        {
            i1 = clock.instant();
            config("%s", i1);
        }
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
    private Instant instant()
    {
        return clock.instant().plusMillis(rootDelay);
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

    @Override
    public void run()
    {
        try
        {
            UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("0.0.0.0", 123, 64, true, false);
            config("SNTP listening %s", channel);
            ByteBuffer bb = ByteBuffer.allocateDirect(64);
            while (true)
            {
                channel.read(bb);
                handlePacket(channel, bb, instant());
                bb.clear();
            }
        }
        catch (IOException ex)
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
        SocketAddress addr = channel.getFromAddress();
        info("NTP packet from %s mode=%s%n", addr,
                message.getMode());
        if (message.getMode() == CLIENT)
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
            response.setReferenceTime(response.getReceiveTime());
            response.setReferenceId(referenceClock);

            // Transmit time is time reply sent by server (t3)
            response.setTransmitInstant(instant());

            ByteBuffer buffer = response.getBuffer();
            buffer.clear();
            channel.send(buffer, addr);
        }
    }
}
