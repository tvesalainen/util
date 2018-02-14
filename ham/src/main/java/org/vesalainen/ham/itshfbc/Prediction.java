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
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Prediction
{
    private YearMonth month;
    private Map<Integer,HourPrediction> hours = new HashMap<>();

    public Prediction(int year, String mon)
    {
        this(year, detect(mon));
    }
    public Prediction(int year, Month mon)
    {
        month = YearMonth.of(year, mon);
    }
    public void addHour(HourPrediction hourly)
    {
        hours.put(hourly.getHour(), hourly);
    }
    private static Month detect(String mon)
    {
        switch (mon)
        {
            case "Jan":
                return JANUARY;
            case "Feb":
                return FEBRUARY;
            case "Mar":
                return MARCH;
            case "Apr":
                return APRIL;
            case "Jun":
                return JUNE;
            case "Jul":
                return JULY;
            case "Aug":
                return AUGUST;
            case "Sep":
                return SEPTEMBER;
            case "Oct":
                return OCTOBER;
            case "Nov":
                return NOVEMBER;
            case "Dec":
                return DECEMBER;
            default:
                throw new UnsupportedOperationException(mon);
        }
    }
}
