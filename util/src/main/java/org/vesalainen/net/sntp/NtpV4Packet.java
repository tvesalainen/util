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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NtpV4Packet
{
    public static final int PORT = 123;   // NTP port number                  |
    public static final int VERSION = 4;   // NTP version number                   |
    public static final int MINPOLL = 4;   // minimum poll exponent (16 s)     |
    public static final int MAXPOLL = 17;   // maximum poll exponent (36 h)     |
    public static final int MAXDISP = 16000;   // maximum dispersion (16 s)        |
    public static final int MINDISP = 5;   // minimum dispersion increment (s) |
    public static final int MAXSTRAT = 16;   // maximum stratum number   

    public enum LeapIndicator{NO_WARNING, LAST_MINUTE_OF_DAY_61, LAST_MINUTE_OF_DAY_59, UNKNOWN};
    public enum Mode{NA, SYMMETRIC_ACTIVE, SYMMETRIC_PASSIVE, CLIENT, SERVER, BROADCAST_SERVER, BROADCAST_CLIENT};
    private static final long NTP2UNIX = 2208988800L;
    private static final long NANOMUL = 1000000000L;
    private static final long MASK32 = 0xffffffffL;
    private static final long MAX32 = 0x100000000L;

    protected ByteBuffer buffer;

    public NtpV4Packet()
    {
        this(ByteBuffer.allocateDirect(48));
    }

    public NtpV4Packet(ByteBuffer buffer)
    {
        this.buffer = buffer.order(BIG_ENDIAN);
        setVersion(4);
    }
    
    public static double log2(long x)
    {
        return x < 0L ? 1.0 / (1L<<(-x)) : 1L<<x;
    }
    public void setReferenceId(ReferenceClock referenceClock)
    {
        setReferenceId(referenceClock.name());
    }

    public void setReferenceId(String referenceId)
    {
        buffer.putInt(12, 0);
        byte[] refId = referenceId.getBytes(US_ASCII);
        int len = Math.min(refId.length, 4);
        for (int ii=0;ii<len;ii++)
        {
            buffer.put(12+ii, refId[ii]);
        }
    }
    static int millis2NtpShort(long millis)
    {
        return (int) ((millis*0x10000L)/1000L);
    }
    static long ntpShort2Millis(long ntpShort)
    {
        return (ntpShort*1000L)/0x10000L;
    }
    static long millis2NtpTimestamp(long millis)
    {
        return toNtpTimestamp(millis/1000, (int) ((millis%1000)*1000000L));
    }
    static long instant2NtpTimestamp(Instant instant)
    {
        return toNtpTimestamp(instant.getEpochSecond(), instant.getNano());
    }
    static long toNtpTimestamp(long epochSecond, int nano)
    {
        long seconds = epochSecond+NTP2UNIX;
        long fraction = (MAX32*nano)/NANOMUL;
        return (seconds<<32)+fraction;
    }
    static Instant ntpTimestamp2Instant(long ntpTimestamp)
    {
        int seconds = (int) (((ntpTimestamp>>>32) & MASK32)-NTP2UNIX);
        long nanos = (NANOMUL*(ntpTimestamp & MASK32))/MAX32;
        return Instant.ofEpochSecond(Integer.toUnsignedLong(seconds), nanos);
    }
    public LeapIndicator getLeapIndicator()
    {
        return LeapIndicator.values()[(buffer.get(0)>>6)&0x3];
    }

    public void setLeapIndicator(LeapIndicator leapIndicator)
    {
        buffer.put(0, (byte)(buffer.get(0) & 0x3f | (leapIndicator.ordinal() << 6)));
    }

    public Mode getMode()
    {
        return Mode.values()[(buffer.get(0)&7)];
    }

    public void setMode(Mode mode)
    {
        buffer.put(0, (byte)(buffer.get(0) & 0xf8 | mode.ordinal()));
    }

    public int getPoll()
    {
        return buffer.get(2);
    }

    public void setPoll(int poll)
    {
        if (poll < MINPOLL || poll > MAXPOLL)
        {
            throw new IllegalArgumentException();
        }
        buffer.put(2, (byte) poll);
    }

    public int getPrecision()
    {
        return buffer.get(3);
    }

    public void setPrecision(int precision)
    {
        buffer.put(3, (byte) precision);
    }
    /**
     * Returns root-delay in milli seconds
     * @return 
     */
    public long getRootDelay()
    {
        return ntpShort2Millis(getRootDelayRaw());
    }
    public int getRootDelayRaw()
    {
        return buffer.getInt(4);
    }
    public void setRootDelay(long millis)
    {
        if (millis < 0 || millis > MAXDISP)
        {
            throw new IllegalArgumentException();
        }
        setRootDelayRaw(millis2NtpShort(millis));
    }
    public void setRootDelayRaw(int delay)
    {
        buffer.putInt(4, delay);
    }
    public long getRootDispersion()
    {
        return ntpShort2Millis(getRootDispersionRaw());
    }
    public int getRootDispersionRaw()
    {
        return buffer.getInt(8);
    }
    public void setRootDispersion(long millis)
    {
        if (millis < 0 || millis > MAXDISP)
        {
            throw new IllegalArgumentException();
        }
        setRootDispersionRaw(millis2NtpShort(millis));
    }
    public void setRootDispersionRaw(int dispersion)
    {
        buffer.putInt(8, dispersion);
    }
    public int getVersion()
    {
        return (buffer.get(0)>>3) & 7;
    }

    public final void setVersion(int version)
    {
        if (version < 1 || version > 4)
        {
            throw new IllegalArgumentException();
        }
        buffer.put(0, (byte)(buffer.get(0) & 0xc7 | (version << 3)));
    }

    public int getStratum()
    {
        return buffer.get(1);
    }

    public void setStratum(int stratum)
    {
        if (stratum < 1 || stratum > MAXSTRAT)
        {
            throw new IllegalArgumentException();
        }
        buffer.put(1, (byte) stratum);
    }

    public String getReferenceIdString()
    {
        int stratum = getStratum();
        if (stratum <= 1 || stratum > 15)
        {
            byte[] refId = getReferenceId();
            int len = 4;
            while (len > 0 && refId[len-1] == 0)
            {
                len--;
            }
            return new String(refId, 0, len, US_ASCII);
        }
        else
        {
            byte[] referenceId = getReferenceId();
            try
            {
                InetAddress addr = InetAddress.getByAddress(referenceId);
                return addr.getHostAddress();
            }
            catch (UnknownHostException ex)
            {
                return "????";
            }
        }
    }

    public byte[] getReferenceId()
    {
        byte[] refId = new byte[4];
        for (int ii=0;ii<4;ii++)
        {
            refId[ii] = buffer.get(12+ii);
        }
        return refId;
    }
        
    public boolean referenceEquals(InetAddress address)
    {
        int stratum = getStratum();
        if (stratum <= 1 || stratum > 15)
        {
            return false;
        }
        byte[] referenceId = getReferenceId();
        if (address instanceof Inet4Address)
        {
            Inet4Address address4 = (Inet4Address) address;
            return Arrays.equals(referenceId, address4.getAddress());
        }
        else
        {
            try
            {
                Inet6Address address6 = (Inet6Address) address;
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] digest = md5.digest(address6.getAddress());
                for (int ii=0;ii<4;ii++)
                {
                    if (digest[ii] != referenceId[ii])
                    {
                        return false;
                    }
                }
                return true;
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    public Instant getReferenceInstant()
    {
        return ntpTimestamp2Instant(getReferenceTime());
    }

    public Instant getOriginateInstant()
    {
        return ntpTimestamp2Instant(getOriginateTime());
    }

    public Instant getReceiveInstant()
    {
        return ntpTimestamp2Instant(getReceiveTime());
    }

    public Instant getTransmitInstant()
    {
        return ntpTimestamp2Instant(getTransmitTime());
    }

    public void setReferenceInstant(Instant ts)
    {
        setReferenceTime(instant2NtpTimestamp(ts));
    }

    public void setOriginateInstant(Instant ts)
    {
        setOriginateTime(instant2NtpTimestamp(ts));
    }

    public void setReceiveInstant(Instant ts)
    {
        setReceiveTime(instant2NtpTimestamp(ts));
    }

    public void setTransmitInstant(Instant ts)
    {
        setTransmitTime(instant2NtpTimestamp(ts));
    }

    public void setReferenceTime(long ts)
    {
        buffer.putLong(16, ts);
    }

    public void setOriginateTime(long ts)
    {
        buffer.putLong(24, ts);
    }

    public void setReceiveTime(long ts)
    {
        buffer.putLong(32, ts);
    }

    public void setTransmitTime(long ts)
    {
        buffer.putLong(40, ts);
    }

    public long getReferenceTime()
    {
        return buffer.getLong(16);
    }

    public long getOriginateTime()
    {
        return buffer.getLong(24);
    }

    public long getReceiveTime()
    {
        return buffer.getLong(32);
    }

    public long getTransmitTime()
    {
        return buffer.getLong(40);
    }

    public ByteBuffer getBuffer()
    {
        return buffer;
    }

}
