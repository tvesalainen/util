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
package org.vesalainen.navi.cpa;

import static java.lang.Math.*;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * <a href="doc-files/cpa.html">Collision Point Analyses</a>
 */
public interface Course
{
    default long getCPATime(Course o)
    {
        double x0 = getLatitude()-o.getLatitude();
        double x1 = deltaLatitude()-o.deltaLatitude();
        double x2 = getLongitude()-o.getLongitude();
        double x3 = deltaLongitude()-o.deltaLongitude();
        double x5 = x0*x1;
        double x6 = x1*x0;
        double x7 = x5+x6;
        double x8 = x1*x1;
        double x10 = x2*x3;
        double x11 = x3*x2;
        double x12 = x10+x11;
        double x13 = x3*x3;
        double x15 = x7+x12;
        double x16 = x8+x13;
        double x17 = 1*x15;
        double x18 = 2*x16;

        double t = -x17/x18;

        return (long) t;
    }
    default double getCPADistance(Course o)
    {
        double x0 = getLatitude()-o.getLatitude();
        double x1 = deltaLatitude()-o.deltaLatitude();
        double x2 = getLongitude()-o.getLongitude();
        double x3 = deltaLongitude()-o.deltaLongitude();
        double x4 = x0*x0;
        double x5 = x0*x1;
        double x6 = x1*x0;
        double x7 = x5+x6;
        double x8 = x1*x1;
        double x9 = x2*x2;
        double x10 = x2*x3;
        double x11 = x3*x2;
        double x12 = x10+x11;
        double x13 = x3*x3;
        double x14 = x4+x9;
        double x15 = x7+x12;
        double x16 = x8+x13;
        double x17 = 1*x15;
        double x18 = 2*x16;

        double t = -x17/x18;

        return Math.sqrt((x16*t+x15)*t+x14)*60.0;
    }
    default double getLatitudeAt(long t)
    {
        double x0 = getLatitude();
        double dx = deltaLatitude();
        return x0 + dx*t;
    }
    default double getLongitudeAt(long t)
    {
        double y0 = getLongitude();
        double dy = deltaLongitude();
        return y0 + dy*t;
    }
    default double deltaLatitude()
    {
        return cos(toRadians(getCourse()))*getSpeed()/HOURS.toMillis(60);
    }
    default double deltaLongitude()
    {
        return sin(toRadians(getCourse()))*getSpeed()/HOURS.toMillis(60)/cos(toRadians(getLatitude()));
    }
    double getLatitude();
    double getLongitude();
    double getCourse();
    double getSpeed();
}
