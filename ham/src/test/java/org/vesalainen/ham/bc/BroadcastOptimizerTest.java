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

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
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
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINE);
    }

    @Test
    public void test1() throws IOException, JAXBException
    {
        dumpStations();
        BroadcastOptimizer opt = new BroadcastOptimizer();
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plus(1, ChronoUnit.DAYS);
        OffsetDateTime now = start;
        while (now.isBefore(end))
        {
            BestStation bestStation = opt.bestStation(new Location(9, -79), now);
            now = TimeUtils.next(now, bestStation.getTo());
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
