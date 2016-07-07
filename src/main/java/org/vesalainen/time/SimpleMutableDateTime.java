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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.EnumMap;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;

/**
 *
 * @author tkv
 */
public class SimpleMutableDateTime implements MutableDateTime, Cloneable
{
    private IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));

    public SimpleMutableDateTime()
    {
    }

    public SimpleMutableDateTime(int year, int month, int day, int hour, int minute, int second, int milliSecond)
    {
        setDate(year, month, day);
        setTime(hour, minute, second, milliSecond);
    }

    @Override
    public SimpleMutableDateTime clone()
    {
        return new SimpleMutableDateTime(getYear(), getMonth(), getDay(), getHour(), getMinute(), getSecond(), getMilliSecond());
    }
    /**
     * Adds delta years. Delta can be negative. Affects only year field.
     * @param delta 
     */
    public void plusYears(int delta)
    {
        if (delta != 0)
        {
            setYear(getYear() + delta);
        }
    }
    /**
     * Adds delta months. Delta can be negative. Month and year fields are 
     * affected.
     * @param delta 
     */
    public void plusMonths(int delta)
    {
        if (delta != 0)
        {
            int result = getMonth() -1 + delta;
            setMonth(Math.floorMod(result, 12) + 1);
            plusYears(Math.floorDiv(result, 12));
        }
    }
    /**
     * Adds delta days. Delta can be negative. Month, year and day fields are 
     * affected.
     * @param delta 
     */
    public void plusDays(long delta)
    {
        if (delta != 0)
        {
            long result = getDay() + delta;
            if (result >= 1 && result <= 28)
            {
                setDay((int) result);
            }
            else
            {
                ZonedDateTime zonedDateTime = zonedDateTime();
                ZonedDateTime plusDays = zonedDateTime.plusDays(delta);
                setZonedDateTime(plusDays);
            }
        }
    }
    /**
     * Adds delta hours. Delta can be negative. Month, year, day and hour fields 
     * are affected.
     * @param delta 
     */
    public void plusHours(long delta)
    {
        if (delta != 0)
        {
            long result = getHour() + delta;
            setHour((int) Math.floorMod(result, 24));
            plusDays(Math.floorDiv(result, 24));
        }
    }
    /**
     * Adds delta minutes. Delta can be negative. Month, year, day, hour and 
     * minute fields are affected.
     * @param delta 
     */
    public void plusMinutes(long delta)
    {
        if (delta != 0)
        {
            long result = getMinute() + delta;
            setMinute((int) Math.floorMod(result, 60));
            plusHours(Math.floorDiv(result, 60));
        }
    }
    /**
     * Adds delta seconds. Delta can be negative. Month, year, day, hour, 
     * minute and seconds fields are affected.
     * @param delta 
     */
    public void plusSeconds(long delta)
    {
        if (delta != 0)
        {
            long result = getSecond() + delta;
            setSecond((int) Math.floorMod(result, 60));
            plusMinutes(Math.floorDiv(result, 60));
        }
    }
    /**
     * Adds delta milliseconds. Delta can be negative. Month, year, day, hour, 
     * minute, seconds and milliSecond fields are affected.
     * @param delta 
     */
    public void plusMilliSeconds(long delta)
    {
        if (delta != 0)
        {
            long result = getMilliSecond() + delta;
            setMilliSecond((int) Math.floorMod(result, 1000));
            plusSeconds(Math.floorDiv(result, 1000));
        }
    }
    /**
     * Creates ZonedDateTime with same fields and UTC ZoneId.
     * @return 
     */
    public ZonedDateTime zonedDateTime()
    {
        return zonedDateTime(ZoneOffset.UTC);
    }
    /**
     * Creates ZonedDateTime with same fields and given ZoneId.
     * @param zoneId
     * @return 
     */
    public ZonedDateTime zonedDateTime(ZoneId zoneId)
    {
        return ZonedDateTime.of(getYear(), getMonth(), getDay(), getHour(), getMinute(), getSecond(), getMilliSecond()*1000000, zoneId);
    }
    /**
     * Returns SimpleMutableDateTime as 1970-01-01 00:00:00Z
     * @return 
     */
    public static final SimpleMutableDateTime epoch()
    {
        return new SimpleMutableDateTime(1970, 1, 1, 0, 0, 0, 0);
    }
    /**
     * Creates SimpleMutableDateTime and initializes it to current time UTC.
     * @return 
     */
    public static final SimpleMutableDateTime now()
    {
        return now(ZoneOffset.UTC);
    }
    /**
     * Creates SimpleMutableDateTime and initializes it using given clock.
     * @param clock
     * @return 
     */
    public static final SimpleMutableDateTime now(Clock clock)
    {
        ZonedDateTime zdt = ZonedDateTime.now(clock);
        SimpleMutableDateTime smt = new SimpleMutableDateTime();
        smt.setZonedDateTime(zdt);
        return smt;
    }
    /**
     * Creates SimpleMutableDateTime and initializes it to given ZoneId.
     * @param zoneId
     * @return 
     */
    public static final SimpleMutableDateTime now(ZoneId zoneId)
    {
        ZonedDateTime zdt = ZonedDateTime.now(zoneId);
        SimpleMutableDateTime smt = new SimpleMutableDateTime();
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
        final SimpleMutableDateTime other = (SimpleMutableDateTime) obj;
        return equals(other);
    }

}
