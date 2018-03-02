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
package org.vesalainen.ham.itshfbc;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.itshfbc.station.DefaultCustomizer;
import org.vesalainen.ham.jaxb.MapType;
import org.vesalainen.ham.jaxb.ScheduleType;
import org.vesalainen.ham.jaxb.TransmitterType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RFaxParserTest
{
    RFaxParser parser = RFaxParser.getInstance();
    public RFaxParserTest()
    {
    }

    @Test
    public void testSchedule()
    {
        DefaultCustomizer customizer = DefaultCustomizer.getInstance("Tokyo.txt");
        ScheduleType[] schedules = parser.parseSchedule("-------/1220 12/24/48/72HR OCEAN WAVE PROG      120/576 0000", customizer);
        assertEquals(1, schedules.length);
        ScheduleType schedule = schedules[0];
        assertEquals(12, schedule.getTime().getHour());
        assertEquals(20, schedule.getTime().getMinute());
        assertEquals("12/24/48/72HR OCEAN WAVE PROG", schedule.getContent());
        assertEquals(120, schedule.getRpm());
        assertEquals(576, schedule.getIoc());
        assertEquals(0, schedule.getValid().getHour());
        assertEquals(0, schedule.getValid().getMinute());
    }
    @Test
    public void testSchedule2()
    {
        DefaultCustomizer customizer = DefaultCustomizer.getInstance("foo.txt");
        ScheduleType[] schedules = parser.parseSchedule("0255/1455  SEA STATE ANALYSIS, WIND/WAVE ANALYSIS  120/576      00/12    1/8", customizer);
    }
    @Test
    public void testMap1()
    {
        DefaultCustomizer customizer = DefaultCustomizer.getInstance("PtReyes.txt");
        List<MapType> maps = parser.parseMapLine("MAP AREAS:   1.  20N - 70N,   115W - 135E        2.  20N - 70N,   115W - 175W", customizer);
        assertEquals(2, maps.size());
        assertEquals("1", maps.get(0).getName());
        assertEquals("2", maps.get(1).getName());
    }
    @Test
    public void testMap2()
    {
        DefaultCustomizer customizer = DefaultCustomizer.getInstance("PtReyes.txt");
        String mapLine = customizer.mapLine("             5.  05N - 55N,  EAST OF 180W        6.  23N - 42N,  EAST OF 150W");
        List<MapType> maps = parser.parseMapLine(mapLine, customizer);
        assertEquals(2, maps.size());
        assertEquals("5", maps.get(0).getName());
        assertEquals("6", maps.get(1).getName());
    }
    @Test
    public void testMap3()
    {
        DefaultCustomizer customizer = DefaultCustomizer.getInstance("PtReyes.txt");
        String mapLine = customizer.mapLine("             7.  05N - 32N,  EAST OF 130W        8.  18N - 62N,  EAST OF 157W");
        List<MapType> maps = parser.parseMapLine(mapLine, customizer);
        assertEquals(2, maps.size());
        assertEquals("7", maps.get(0).getName());
        assertEquals("8", maps.get(1).getName());
    }
}
