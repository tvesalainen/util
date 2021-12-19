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

import static java.lang.Math.*;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarPosition
{
    private Clock clock;
    private double longitude;
    private double latitude;
    private double γ;
    private double eqtime;
    private double decl;
    private double time_offset;
    private double tst;
    private double hourAngle;
    private ZonedDateTime zdt;
    private int timezoneMinutes;

    public SolarPosition(Clock clock)
    {
        this.clock = clock;
    }
    
    public void set(double longitude, double latitude)
    {
        this.clock = clock;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    private void calc()
    {
        zdt = ZonedDateTime.now(clock);
        double lon = toRadians(longitude);
        double lat = toRadians(latitude);
        int dayOfYear = zdt.getDayOfYear();
        int hr = zdt.getHour();
        int mn = zdt.getMinute();
        int sc = zdt.getSecond();
        ZoneOffset zoneOffset = zdt.getOffset();
        timezoneMinutes = zoneOffset.getTotalSeconds()/60;
        γ = ((2*PI)/365) * (dayOfYear - 1 + (hr-12)/24);
        eqtime = 229.18*(0.000075 + 0.001868*cos(γ) - 0.032077*sin(γ) - 0.014615*cos(2*γ) - 0.040849*sin(2*γ) );
        decl = 0.006918 - 0.399912*cos(γ) + 0.070257*sin(γ) - 0.006758*cos(2*γ) + 0.000907*sin(2*γ) - 0.002697*cos(3*γ) + 0.00148*sin (3*γ);
        time_offset = (int) (eqtime + 4*longitude - timezoneMinutes);
        tst = hr*60 + mn + sc/60 + time_offset;
        //double ha = (tst / 4) - 180;
        //double Φ = acos(sin(latitude)*sin(decl) + cos(latitude)*cos(decl)*cos(ha));
        hourAngle = toDegrees(acos(cos(toRadians(90.833))/(cos(lat)*cos(decl)) - tan(lat)*tan(decl)));
    }
    public ZonedDateTime sunrise()
    {
        calc();
        int sr = (int) (720 - 4*(longitude + hourAngle) - eqtime);
        return toZonedDateTime(sr+timezoneMinutes);
    }
    public ZonedDateTime sunset()
    {
        calc();
        int ss = (int) (720 - 4*(longitude - hourAngle) - eqtime);
        return toZonedDateTime(ss+timezoneMinutes);
    }
    private ZonedDateTime toZonedDateTime(int min)
    {
        int h = min/60;
        int m = min%60;
        return ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), h, m, 0, 0, zdt.getZone());
    }
}
