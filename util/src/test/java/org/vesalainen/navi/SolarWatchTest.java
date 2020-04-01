/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;
import net.e175.klaus.solarpositioning.PSA;
import net.e175.klaus.solarpositioning.SPA;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarWatchTest
{

    public SolarWatchTest()
    {
    }

    @Test
    public void test1()
    {
        SolarWatch sw = new SolarWatch(1, TimeUnit.MINUTES, ()->-35, ()->174);
        ZonedDateTime zdt = ZonedDateTime.now();
        GregorianCalendar cal = new GregorianCalendar();
        for (int ii=0;ii<1500;ii++)
        {
            cal.setTimeInMillis(zdt.toEpochSecond()*1000);
            AzimuthZenithAngle angle = SPA.calculateSolarPosition(cal, -35.76458, 174.36069, 0, DeltaT.estimate(cal));
            System.err.println(zdt+" "+sw.phase(angle.getZenithAngle()));
            zdt = zdt.plusMinutes(1);
        }
    }

}
