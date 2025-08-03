/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import org.vesalainen.time.MutableInstant;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NTPTimestamp extends MutableInstant
{
    private static final long NTP2UNIX = 2208988800L;
    private static final long NANOMUL = 1000000000L;
    private static final long MASK32 = 0xffffffffL;
    private static final long MAX32 = 0x100000000L;

    public NTPTimestamp()
    {
    }

    public NTPTimestamp(TemporalAccessor accessor)
    {
        super(accessor);
    }

    public NTPTimestamp(MutableInstant mi)
    {
        super(mi);
    }

    public NTPTimestamp(long second, long nano)
    {
        super(second, nano);
    }
    
    static long millis2NtpTimestamp(long millis)
    {
        return toNtpTimestamp(millis/1000, (int) ((millis%1000)*1000000L));
    }
    static long toNtpTimestamp(long epochSecond, int nano)
    {
        long seconds = epochSecond+NTP2UNIX;
        long fraction = (MAX32*nano)/NANOMUL;
        return (seconds<<32)+fraction;
    }
    public long toNtpTimestamp()
    {
        rl.lock();
        try
        {
            return toNtpTimestamp(second, (int) nano);
        }
        finally
        {
            rl.unlock();
        }
    }
    public void setNTPTimestamp(long ntpTimestamp)
    {
        int seconds = (int) (((ntpTimestamp>>>32) & MASK32)-NTP2UNIX);
        long nanos = (NANOMUL*(ntpTimestamp & MASK32))/MAX32;
        set(Integer.toUnsignedLong(seconds), nanos);
    }
    public static NTPTimestamp fromNTPTimestamp(long ntpTimestamp)
    {
        int seconds = (int) (((ntpTimestamp>>>32) & MASK32)-NTP2UNIX);
        long nanos = (NANOMUL*(ntpTimestamp & MASK32))/MAX32;
        return new NTPTimestamp(Integer.toUnsignedLong(seconds), nanos);
    }
}
