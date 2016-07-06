/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.time;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.LongMap;

/**
 *
 * @author tkv
 */
public class SimpleMutableTime implements MutableTime
{
    private static final long SecondInMillis = 1000;
    private static final long MinuteInMillis = SecondInMillis*60;
    private static final long HourInMillis = MinuteInMillis*60;
    private static final long DayInMillis = HourInMillis*24;
    private static final Map<String,Long> millisMap = new HashMap<>();
    private IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    /**
     * Returns milliseconds from 1970-01-01 00:00:00Z
     * @return 
     */
    public long millis()
    {
        return millis(ZoneOffset.UTC);
    }
    /**
     * Returns milliseconds from 1970-01-01 00:00:00 in given time-zone.
     * @param zoneOffset
     * @return 
     */
    public long millis(ZoneOffset zoneOffset)
    {
        int offset = - zoneOffset.getTotalSeconds()*1000;
        String yearMonth = String.format("%d-%d", getYear(), getMonth());
        long epochMillis;
        if (!millisMap.containsKey(yearMonth))
        {
            ZonedDateTime zdt = ZonedDateTime.of(getYear(), getMonth(), 1, 0, 0, 0, 0, ZoneOffset.UTC);
            epochMillis = zdt.toEpochSecond()*1000;
            millisMap.put(yearMonth, epochMillis);
        }
        else
        {
            epochMillis = millisMap.get(yearMonth);
        }
        return
                offset +
                epochMillis +
                DayInMillis*(getDay()-1) +
                HourInMillis*getHour() +
                MinuteInMillis*getMinute() +
                SecondInMillis*getSecond() +
                getMilliSecond();
    }
    /**
     * Returns the inner field map.
     * @return 
     */
    public IntMap<ChronoField> getFields()
    {
        return fields;
    }
    
    @Override
    public int get(ChronoField chronoField)
    {
        checkField(chronoField);
        if (fields.containsKey(chronoField))
        {
            return fields.getInt(chronoField);
        }
        else
        {
            return (int) chronoField.range().getMinimum();
        }
    }
    @Override
    public void set(ChronoField chronoField, int amount)
    {
        checkField(chronoField);
        if (ChronoField.YEAR.equals(chronoField))
        {
            amount = convertTo4DigitYear(amount);
        }
        chronoField.checkValidIntValue(amount);
        fields.put(chronoField, amount);
    }

    @Override
    public String toString()
    {
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d", 
                getYear(),
                getMonth(),
                getDay(),
                getHour(),
                getMinute(),
                getSecond(),
                getMilliSecond()
                );
    }

}
