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

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;

/**
 *
 * @author tkv
 */
public class SimpleMutableDate implements MutableDate
{
    private IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));

    public SimpleMutableDate()
    {
    }

    public SimpleMutableDate(int year, int month, int day, int hour, int minute, int second, int milliSecond)
    {
        setDate(year, month, day);
        setTime(hour, minute, second, milliSecond);
    }
    
    /**
     * Creates SimpleMutableDate and initializes it to current time UTC.
     * @return 
     */
    public static final SimpleMutableDate now()
    {
        return now(ZoneOffset.UTC);
    }
    /**
     * Creates SimpleMutableDate and initializes it using given clock.
     * @param clock
     * @return 
     */
    public static final SimpleMutableDate now(Clock clock)
    {
        ZonedDateTime zdt = ZonedDateTime.now(clock);
        SimpleMutableDate smt = new SimpleMutableDate();
        smt.setZonedDateTime(zdt);
        return smt;
    }
    /**
     * Creates SimpleMutableDate and initializes it to given time-zone.
     * @param zoneOffset
     * @return 
     */
    public static final SimpleMutableDate now(ZoneOffset zoneOffset)
    {
        ZonedDateTime zdt = ZonedDateTime.now(zoneOffset);
        SimpleMutableDate smt = new SimpleMutableDate();
        smt.setZonedDateTime(zdt);
        return smt;
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

    @Override
    public int hashCode()
    {
        int hash = 0;
        for (ChronoField cf : SupportedFields)
        {
            hash += get(cf);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final SimpleMutableDate other = (SimpleMutableDate) obj;
        return equals(other);
    }

}
