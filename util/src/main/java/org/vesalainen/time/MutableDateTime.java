/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This interface defines simple mutable access to time fields.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface MutableDateTime extends TemporalAccessor
{
    /**
     * This interface supports only these fields. Using other ChronoFields is 
     * undefined.
     */
    static final TemporalField[] SUPPORTED_FIELDS = new ChronoField[]{
        ChronoField.OFFSET_SECONDS,
        ChronoField.YEAR,
        ChronoField.MONTH_OF_YEAR, 
        ChronoField.DAY_OF_MONTH, 
        ChronoField.HOUR_OF_DAY, 
        ChronoField.MINUTE_OF_HOUR, 
        ChronoField.SECOND_OF_MINUTE,
        ChronoField.MILLI_OF_SECOND,
        ChronoField.DAY_OF_WEEK,
        ChronoField.INSTANT_SECONDS,
        ChronoField.NANO_OF_SECOND
    };
    static final long SECOND_IN_MILLIS = 1000;
    static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS*60;
    static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS*60;
    static final long DAY_IN_MILLIS = HOUR_IN_MILLIS*24;
    static final Map<String,Long> millisMap = new HashMap<>();
    /**
     * Returns milliseconds from 1970-01-01 00:00:00Z
     * @return 
     */
    default long millis()
    {
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
                -SECOND_IN_MILLIS*getOffsetSecond() +
                epochMillis +
                DAY_IN_MILLIS*(getDay()-1) +
                HOUR_IN_MILLIS*getHour() +
                MINUTE_IN_MILLIS*getMinute() +
                SECOND_IN_MILLIS*getSecond() +
                getMilliSecond();
    }
    /**
     * Checks if the instant of this date-time is after that of the specified date-time.
     * @param other
     * @return 
     */
    default boolean isAfter(MutableDateTime other)
    {
        return millis() > other.millis();
    }
    /**
     * Checks if the instant of this date-time is before that of the specified date-time.
     * @param other
     * @return 
     */
    default boolean isBefore(MutableDateTime other)
    {
        return millis() < other.millis();
    }
    /**
     * Returns seconds from 1970-01-01 00:00:00Z
     * @return 
     */
    default long seconds()
    {
        return millis()/1000;
    }

    public default void checkField(TemporalField field)
    {
        if (!isSupported(field))
        {
            throw new UnsupportedOperationException(field+" is not supported");
        }
    }
    @Override
    public default boolean isSupported(TemporalField field)
    {
        for (TemporalField cf : SUPPORTED_FIELDS)
        {
            if (cf.equals(field))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Sets time field value. See set-methods for constraints
     * @param chronoField
     * @param amount 
     */
    void set(TemporalField chronoField, long amount);
    /**
     * @deprecated Use SimpleMutabledateTime::from
     * Copies fields to this from given MutableDateTime.
     * <p>
     * This implementation just calls get and set methods for every supported
     * field. 
     * @param mt 
     */
    default void set(MutableDateTime mt)
    {
        setOffsetSecond(mt.getOffsetSecond());
        setYear(mt.getYear());
        setMonth(mt.getMonth());
        setDay(mt.getDay());
        setHour(mt.getHour());
        setMinute(mt.getMinute());
        setSecond(mt.getSecond());
        setMilliSecond(mt.getMilliSecond());
    }
    /**
     * @deprecated Use set
     * Sets fields from ZonedDateTime.
     * <p>
     * Note! Zone is ignored.
     * @param zonedDateTime 
     */
    default void setZonedDateTime(ZonedDateTime zonedDateTime)
    {
        for (TemporalField cf : SUPPORTED_FIELDS)
        {
            set(cf, zonedDateTime.getLong(cf));
        }
    }
    /**
     * Sets fields from ZonedDateTime.
     * <p>
     * Note! Zone is ignored.
     * @param temporal 
     */
    default void set(TemporalAccessor temporal)
    {
        for (TemporalField cf : SUPPORTED_FIELDS)
        {
            set(cf, temporal.getLong(cf));
        }
    }
    /**
     * Returns true if given MutableDateTime equals to this
     * @param mt
     * @return 
     */
    default boolean equals(MutableDateTime mt)
    {
        for (TemporalField cf : SUPPORTED_FIELDS)
        {
            if (getLong(cf) != mt.getLong(cf))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Return Year (4 digits)
     * @return 
     */
    default int getYear()
    {
        return get(ChronoField.YEAR);
    }
    /**
     * Returns Month of Year (1-12)
     * @return 
     */
    default int getMonth()
    {
        return get(ChronoField.MONTH_OF_YEAR);
    }
    /**
     * Return Day of Month (1-31)
     * @return 
     */
    default int getDay()
    {
        return get(ChronoField.DAY_OF_MONTH);
    }
    /**
     * Return Hour of Day (0-23)
     * @return 
     */
    default int getHour()
    {
        return get(ChronoField.HOUR_OF_DAY);
    }
    /**
     * Return Minute of Hour (0-59)
     * @return 
     */
    default int getMinute()
    {
        return get(ChronoField.MINUTE_OF_HOUR);
    }
    /**
     * Return Second of Minute (0-59)
     * @return 
     */
    default int getSecond()
    {
        return get(ChronoField.SECOND_OF_MINUTE);
    }
    /**
     * Return Milli Second of Second (0-999)
     * @return 
     */
    default int getMilliSecond()
    {
        return get(ChronoField.MILLI_OF_SECOND);
    }
    /**
     * Returns the offset from UTC.
     * @return 
     */
    default int getOffsetSecond()
    {
        return get(ChronoField.OFFSET_SECONDS);
    }
    /**
     * Returns ZoneId
     * @return 
     */
    default ZoneId getZoneId()
    {
        return ZoneOffset.ofTotalSeconds(getOffsetSecond());
    }
    /**
     * Set utc time
     * @param hour 0 - 23
     * @param minute 0 - 59
     * @param second 0 - 59
     * @param milliSecond 0-999
     */
    default void setTime(int hour, int minute, int second, int milliSecond)
    {
        setHour(hour);
        setMinute(minute);
        setSecond(second);
        setMilliSecond(milliSecond);
    }
    /**
     * Set utc date
     * @param year yy
     * @param month mm 1 - 12
     * @param day dd 1 - 31
     */
    default void setDate(int year, int month, int day)
    {
        setYear(year);
        setMonth(month);
        setDay(day);
    }
    /**
     * Update UTC Hour
     * @param hour
     */
    default void setHour(int hour)
    {
        set(ChronoField.HOUR_OF_DAY, hour);
    }
    /**
     * Update utc minute
     * @param minute 
     */
    default void setMinute(int minute)
    {
        set(ChronoField.MINUTE_OF_HOUR, minute);
    }
    /**
     * Update utc second
     * @param second 
     */
    default void setSecond(int second)
    {
        set(ChronoField.SECOND_OF_MINUTE, second);
    }
    /**
     * Update UTC microsecond
     * @param milliSecond 
     */
    default void setMilliSecond(int milliSecond)
    {
        set(ChronoField.MILLI_OF_SECOND, milliSecond);
    }

    /**
     * Day of Month, 01 to 31
     * @param day 
     */
    default void setDay(int day)
    {
        set(ChronoField.DAY_OF_MONTH, day);
    }
    /**
     * Month of Year, 01 to 12
     * @param month 
     */
    default void setMonth(int month)
    {
        set(ChronoField.MONTH_OF_YEAR, month);
    }
    /**
     * Year (4 digits)
     * @param year 
     */
    default void setYear(int year)
    {
        set(ChronoField.YEAR, convertTo4DigitYear(year));
    }
    /**
     * Set offset to UTC.
     * @param offsetSecond 
     */
    default void setOffsetSecond(int offsetSecond)
    {
        set(ChronoField.OFFSET_SECONDS, offsetSecond);
    }
    /**
     * Converts 2 digit year to 4 digit. If year &lt; 70 add 2000. If 
     * year &lt; 100 add 1900.
     * @param year
     * @return 
     */
    default long convertTo4DigitYear(long year)
    {
        if (year < 70)
        {
            return 2000 + year;
        }
        else
        {
            if (year < 100)
            {
                return 1900 + year;
            }
            else
            {
                return year;
            }
        }
    }
    /**
     * Returns Gregorian calendar constructed from MutableDateTime. 
     * <p>
     * Note! Milli seconds are ignored.
     * @return 
     */
    default GregorianCalendar getGregorianCalendar()
    {
        return new GregorianCalendar(getYear(), getMonth()-1, getDay(), getHour(), getMinute(), getSecond());
    }

}
