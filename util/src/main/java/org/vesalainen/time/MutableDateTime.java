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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoField.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

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
    static final Set<ChronoField> SUPPORTED_FIELDS = EnumSet.of(
        YEAR,
        MONTH_OF_YEAR, 
        DAY_OF_MONTH, 
        HOUR_OF_DAY, 
        MINUTE_OF_HOUR, 
        SECOND_OF_MINUTE,
        NANO_OF_SECOND,
        DAY_OF_WEEK);

    static final long SECOND_IN_MILLIS = 1000;
    static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS*60;
    static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS*60;
    static final long DAY_IN_MILLIS = HOUR_IN_MILLIS*24;
    
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
        if (field instanceof ChronoField)
        {
            ChronoField cf = (ChronoField) field;
            return SUPPORTED_FIELDS.contains(cf);
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
        return Objects.equals(getZone(), mt.getZone());
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
        return get(ChronoField.NANO_OF_SECOND)/1000000;
    }
    /**
     * 
     * @return Nano Second of Second (0-999999999)
     */
    default int getNanoSecond()
    {
        return get(ChronoField.NANO_OF_SECOND);
    }
    /**
     * Returns ZoneId
     * @return 
     */
    ZoneId getZone();
    /**
     * Sets ZoneId
     * @param zoneId 
     */
    void setZone(ZoneId zoneId);
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
        set(ChronoField.NANO_OF_SECOND, milliSecond*1000000);
    }
    /**
     * Set nano of second
     * @param nanoSecond 
     */
    default void setNanoSecond(int nanoSecond)
    {
        set(ChronoField.NANO_OF_SECOND, nanoSecond);
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

}
