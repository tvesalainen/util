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

import com.opera.core.systems.scope.protos.PrefsProtos;
import java.time.Instant;
import org.apache.commons.net.ntp.TimeStamp;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.net.sntp.NtpV4Packet.LeapIndicator;
import static org.vesalainen.net.sntp.NtpV4Packet.LeapIndicator.*;
import org.vesalainen.net.sntp.NtpV4Packet.Mode;
import static org.vesalainen.net.sntp.NtpV4Packet.Mode.*;
import static org.vesalainen.net.sntp.ReferenceClock.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NtpV4PacketTest
{
    
    public NtpV4PacketTest()
    {
    }

    @Test
    public void testEpoch3()
    {
        Instant exp = Instant.parse("2019-09-24T01:32:23.123Z");
        long ntp = NtpV4Packet.instant2NtpTimestamp(exp);
        Instant got = NtpV4Packet.ntpTimestamp2Instant(ntp);
        assertEquals(exp.getEpochSecond(), got.getEpochSecond());
        assertTrue(Math.abs(exp.getNano() - got.getNano()) < 5);
    }
    @Test
    public void testEpoch4()
    {
        Instant exp = Instant.parse("2020-09-24T23:59:59.123456789Z");
        long ntp = NtpV4Packet.instant2NtpTimestamp(exp);
        Instant got = NtpV4Packet.ntpTimestamp2Instant(ntp);
        assertEquals(exp.getEpochSecond(), got.getEpochSecond());
        assertTrue(Math.abs(exp.getNano() - got.getNano()) < 5);
    }
    @Test
    public void testEpoch5()
    {
        Instant exp = Instant.parse("2020-09-24T23:59:59.123Z");
        long ntp = NtpV4Packet.millis2NtpTimestamp(exp.toEpochMilli());
        Instant got = NtpV4Packet.ntpTimestamp2Instant(ntp);
        assertEquals(exp.getEpochSecond(), got.getEpochSecond());
        assertTrue(Math.abs(exp.getNano() - got.getNano()) < 5);
    }
    @Test
    public void testEpoch6()
    {
        Instant exp = Instant.parse("2038-09-24T23:59:59.123Z");
        long ntp = NtpV4Packet.millis2NtpTimestamp(exp.toEpochMilli());
        TimeStamp ts = TimeStamp.getNtpTime(exp.toEpochMilli());
        assertEquals(ts.ntpValue(), ntp);
        Instant got = NtpV4Packet.ntpTimestamp2Instant(ntp);
        //assertEquals(ts.getTime(), got.toEpochMilli());
        assertEquals(exp.getEpochSecond(), got.getEpochSecond());
        assertTrue(Math.abs(exp.getNano() - got.getNano()) < 5);
    }
    @Test
    public void test()
    {
        NtpV4Packet p = new NtpV4Packet();
        for (LeapIndicator li : LeapIndicator.values())
        {
            for (Mode mode : Mode.values())
            {
                p.setMode(mode);
                assertEquals(mode, p.getMode());
            }
            p.setLeapIndicator(li);
            assertEquals(li, p.getLeapIndicator());
            assertEquals(4, p.getVersion());
        }
        p.setStratum(1);
        p.setPoll(6);
        p.setPrecision(-10);
        p.setRootDelay(12345);
        p.setRootDispersion(1234);
        p.setReferenceId(GPS);
        Instant ref = Instant.parse("2020-09-24T23:59:51.123456789Z");
        Instant ori = Instant.parse("2020-09-24T23:59:52.223456789Z");
        Instant rec = Instant.parse("2020-09-24T23:59:53.323456789Z");
        Instant tra = Instant.parse("2020-09-24T23:59:54.423456789Z");
        p.setReferenceInstant(ref);
        p.setOriginateInstant(ori);
        p.setReceiveInstant(rec);
        p.setTransmitInstant(tra);
        
        assertEquals(BROADCAST_CLIENT, p.getMode());
        assertEquals(UNKNOWN, p.getLeapIndicator());
        assertEquals(4, p.getVersion());
        assertEquals(1, p.getStratum());
        assertEquals(6, p.getPoll());
        assertEquals(-10, p.getPrecision());
        assertEquals(12344, p.getRootDelay());
        assertEquals(1233, p.getRootDispersion());
        assertEquals("GPS", p.getReferenceIdString());
        
        assertEquals(NtpV4Packet.ntpTimestamp2Instant(NtpV4Packet.instant2NtpTimestamp(ref)), p.getReferenceInstant());
        assertEquals(NtpV4Packet.ntpTimestamp2Instant(NtpV4Packet.instant2NtpTimestamp(ori)), p.getOriginateInstant());
        assertEquals(NtpV4Packet.ntpTimestamp2Instant(NtpV4Packet.instant2NtpTimestamp(rec)), p.getReceiveInstant());
        assertEquals(NtpV4Packet.ntpTimestamp2Instant(NtpV4Packet.instant2NtpTimestamp(tra)), p.getTransmitInstant());
    }
    
}
