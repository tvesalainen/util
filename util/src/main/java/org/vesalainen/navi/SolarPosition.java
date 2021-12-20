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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.vesalainen.navi.SolarWatch.DayPhase;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://gml.noaa.gov/grad/solcalc/solareqns.PDF">General Solar Position Calculations</a>
 */
public class SolarPosition
{
    public static final double SUNSET_ANGLE = 90.833;
    private double longitude;
    private double latitude;
    private double sunSetCos;
    private double twilightCos;
    private double γ;
    private double eqtime;
    private double decl;
    private double time_offset;
    private double tst;
    private double hourAngle;
    private ZonedDateTime time;
    private int timezoneMinutes;
    private double Φ;   // zenith angle
    private final double twilightAngle;
    private double twilightHourAngle;
    /**
     * Initialize SolarPosition with time and position 90.833deg as sunset
     * zenith angle and 102.833deg as twilight zenith angle
     * @param zdt
     * @param longitude Degrees
     * @param latitude Degrees 
     */
    public SolarPosition(ZonedDateTime zdt, double longitude, double latitude)
    {
        this(SUNSET_ANGLE, SUNSET_ANGLE+12, zdt, longitude, latitude);
    }
    /**
     * Initialize SolarPosition with time and position and sunet and twilight zenith angles.
     * @param sunSetAngle Degrees
     * @param twilightAngle Degrees
     * @param time
     * @param longitude Degrees
     * @param latitude Degrees 
     */
    public SolarPosition(double sunSetAngle, double twilightAngle, ZonedDateTime time, double longitude, double latitude)
    {
        this.sunSetCos = cos(toRadians(sunSetAngle));
        this.twilightAngle = twilightAngle;
        this.twilightCos = cos(toRadians(twilightAngle));
        set(time, longitude, latitude);
    }
    /**
     * Initialize SolarPosition with time and position
     * @param time
     * @param longitude
     * @param latitude 
     */
    public void set(ZonedDateTime time, double longitude, double latitude)
    {
        this.time = time;
        this.longitude = longitude;
        this.latitude = latitude;
        double lat = toRadians(latitude);
        int dayOfYear = time.getDayOfYear();
        int hr = time.getHour();
        int mn = time.getMinute();
        int sc = time.getSecond();
        ZoneOffset zoneOffset = time.getOffset();
        timezoneMinutes = zoneOffset.getTotalSeconds()/60;
        γ = ((2*PI)/365) * (dayOfYear - 1 + (hr-12)/24);
        eqtime = 229.18*(0.000075 + 0.001868*cos(γ) - 0.032077*sin(γ) - 0.014615*cos(2*γ) - 0.040849*sin(2*γ) );
        decl = 0.006918 - 0.399912*cos(γ) + 0.070257*sin(γ) - 0.006758*cos(2*γ) + 0.000907*sin(2*γ) - 0.002697*cos(3*γ) + 0.00148*sin (3*γ);
        time_offset = (int) (eqtime + 4*longitude - timezoneMinutes);
        tst = hr*60 + mn + sc/60 + time_offset;
        double ha = toRadians((tst / 4) - 180);
        double cosΦ = (sin(lat)*sin(decl) + cos(lat)*cos(decl)*cos(ha));
        Φ = toDegrees(acos(cosΦ));
        twilightHourAngle = toDegrees(acos(twilightCos/(cos(lat)*cos(decl)) - tan(lat)*tan(decl)));
        hourAngle = toDegrees(acos(sunSetCos/(cos(lat)*cos(decl)) - tan(lat)*tan(decl)));
    }
    /**
     * Returns DayPhase
     * Calculated using last initialization.
     * @return 
     */
    public DayPhase getDayPhase()
    {
        if (Φ < SUNSET_ANGLE)
        {
            return DAY;
        }
        else
        {
            if (Φ > twilightAngle)
            {
                return NIGHT;
            }
            else
            {
                return TWILIGHT;
            }
        }
    }
    /**
     * Returns next sunrise
     * Calculated using last initialization.
     * @return 
     */
    public ZonedDateTime nextSunrise()
    {
        int sr = (int) (720 - 4*(longitude + hourAngle) - eqtime);
        return nextZonedDateTime(sr+timezoneMinutes);
    }
    /**
     * Returns next sunset.
     * Calculated using last initialization.
     * @return 
     */
    public ZonedDateTime nextSunset()
    {
        int ss = (int) (720 - 4*(longitude - hourAngle) - eqtime);
        return nextZonedDateTime(ss+timezoneMinutes);
    }
    /**
     * Returns next end-of-dusk
     * Calculated using last initialization.
     * @return 
     */
    public ZonedDateTime nextDusk()
    {
        int ss = (int) (720 - 4*(longitude - twilightHourAngle) - eqtime);
        return nextZonedDateTime(ss+timezoneMinutes);
    }
    /**
     * Returns next dawn
     * Calculated using last initialization.
     * @return 
     */
    public ZonedDateTime nextDawn()
    {
        int ss = (int) (720 - 4*(longitude + twilightHourAngle) - eqtime);
        return nextZonedDateTime(ss+timezoneMinutes);
    }
    private ZonedDateTime nextZonedDateTime(int min)
    {
        int h = min/60;
        int m = min%60;
        ZonedDateTime t = ZonedDateTime.of(time.getYear(), time.getMonthValue(), time.getDayOfMonth(), h, m, 0, 0, time.getZone());
        while (t.isBefore(time))
        {
            t = t.plusDays(1);
        }
        return t;
    }
}
