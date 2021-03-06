/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.bc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.ham.Schedule;
import org.vesalainen.ham.Station;
import org.vesalainen.ham.TimeUtils;
import org.vesalainen.ham.bc.BroadcastOptimizer.BestStation;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastOptimizerTest
{
    
    public BroadcastOptimizerTest()
    {
        //JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }

    //@Test
    public void test0() throws IOException, JAXBException
    {
        Location location = new Location(9, -79);
        BroadcastOptimizer opt = new BroadcastOptimizer();
        OffsetDateTime start = OffsetDateTime.of(2018, 4, 4, 17, 59, 0, 0, ZoneOffset.UTC);
        for (int ii=0;ii<15;ii++)
        {
            BestStation bs = opt.bestStation(location, start);
            assertEquals("NEW ORLEANS, LOUISIANA, U.S.A", bs.getStation().getName());
            assertEquals(17.1464, bs.getFrequency(), 1e-8);
            OffsetTime to = bs.getTo();
            start = start.with(to);
        }
    }
    @Test
    public void test1() throws IOException, JAXBException
    {
        //dumpStations();
        PrintStream out = new PrintStream("c:\\temp\\pacific_fax_schedules_2018.txt");
        out.printf("Best Frequency/Station for time of day for specific coordinates\r\n");
        out.printf("Note! Frequency is dial frequency. (1.9 KHz less than in rfax.pdf)\r\n");
        out.printf("Mode is Upper Side Band (USB). F3C in some radios\r\n");
        out.printf("Station data is from NOAA http://www.nws.noaa.gov/om/marine/rfax.pdf\r\n");
        out.printf("Uses VOACAP for radio weather predictions\r\n");
        out.printf("THIS IS EXPERIMENTAL!!!\r\n");
        out.printf("Comments: Timo Vesalainen S/Y Iiris timo.vesalainen@iki.fi \r\n");
        BroadcastOptimizer opt = new BroadcastOptimizer();
        for (Location loc : new Location[]{
            new Location(5, -92),
            new Location(1, -104),
            new Location(-2, -116),
            new Location(-6, -127)
        })
        {
            OffsetDateTime start = OffsetDateTime.now();
            out.printf("\r\n\r\nHF Fax Schedule at %s %s\r\n\r\n", loc, start);
            OffsetDateTime end = start.plus(1, ChronoUnit.DAYS);
            OffsetDateTime now = start;
            while (now.isBefore(end))
            {
                BestStation bestStation = opt.bestStation(loc, now);
                out.printf("%s - %s %f MHz %s %s\r\n", 
                        bestStation.getFrom(),
                        bestStation.getTo(),
                        bestStation.getFrequency() - 0.0019,
                        bestStation.getContent(),
                        bestStation.getStation().getName()
                );
                now = TimeUtils.next(now, bestStation.getTo());
            }
        }
    }
    private void dumpStations() throws IOException, JAXBException
    {
        BroadcastStationsFile bsf = new BroadcastStationsFile();
        bsf.load();
        List<Schedule> list = bsf.getStations()
                .values()
                .stream()
                .collect(
                        ArrayList::new,
                        (List<Schedule> r,Station s)->r.addAll(s.getSchedules()), 
                        (List<Schedule> r1,List<Schedule> r2)->r1.addAll(r2));
        list.sort(null);
        for (Schedule s : list)
        {
            System.err.println(s);
        }
    }
}
