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

import java.time.Month;
import static java.time.Month.*;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import static org.vesalainen.ham.TimeUtils.MONTH_PARSER;
import org.vesalainen.util.navi.Location;
import org.vesalainen.util.navi.NauticalMile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Prediction
{
    private static final NauticalMile DISTANCE_LIMIT = new NauticalMile(100);
    private Location myLocation;
    private Month month;
    private Map<Integer,HourPrediction> hours = new HashMap<>();

    public Prediction(Location myLocation, String mon)
    {
        this(myLocation, MONTH_PARSER.find(mon));
    }
    public Prediction(Location myLocation, Month month)
    {
        this.myLocation = myLocation;
        this.month = month;
    }
    public void addHour(HourPrediction hourly)
    {
        hours.put(hourly.getHour(), hourly);
    }
    public HourPrediction getHourPrediction(int hour)
    {
        return hours.get(hour);
    }
    public boolean isValid(Location location, OffsetDateTime date)
    {
        return date.getMonth() == month && myLocation.distance(location).lt(DISTANCE_LIMIT);
    }
}
