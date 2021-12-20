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
package org.vesalainen.navi;

import java.time.Clock;
import java.time.ZonedDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarPositionTest
{
    
    public SolarPositionTest()
    {
    }

    @Test
    public void test1()
    {
        ZonedDateTime now = ZonedDateTime.now();
        SolarPosition sp = new SolarPosition(now, 176.1776266, -37.6704733);
        ZonedDateTime sunset = sp.nextSunset();
        ZonedDateTime dusk = sp.nextDusk();
        ZonedDateTime dawn = sp.nextDawn();
        ZonedDateTime sunrise = sp.nextSunrise();
        switch (sp.getDayPhase())
        {
            case DAY:
                assertTrue(now.isBefore(sunset));
                assertTrue(sunset.isBefore(dusk));
                assertTrue(dusk.isBefore(dawn));
                assertTrue(dawn.isBefore(sunrise));
                break;
        }
    }
    
}
