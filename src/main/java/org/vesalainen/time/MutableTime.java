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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.GregorianCalendar;

/**
 * This interface defines simple mutable access to time fields.
 * @author tkv
 */
public interface MutableTime
{
    /**
     * This interface supports only these fields. Using other ChronoFields is 
     * undefined.
     */
    static final ChronoField[] SupportedFields = new ChronoField[]{
        ChronoField.YEAR,
        ChronoField.MONTH_OF_YEAR, 
        ChronoField.DAY_OF_MONTH, 
        ChronoField.HOUR_OF_DAY, 
        ChronoField.MINUTE_OF_HOUR, 
        ChronoField.SECOND_OF_MINUTE,
        ChronoField.MILLI_OF_SECOND
    };
    /**
     * Checks if chronoField is supported. Throws IllegalArgumentException if not.
     * @param chronoField 
     * @throws IllegalArgumentException
     */
    default void checkField(ChronoField chronoField)
    {
        for (ChronoField cf : SupportedFields)
        {
            if (cf.equals(chronoField))
            {
                return;
            }
        }
        throw new IllegalArgumentException(chronoField+" not supported");
    }
    /**
     * Returns time field value. See get-methods for constraints
     * @param chronoField
     * @return 
     */
    int get(ChronoField chronoField);
    /**
     * Sets time field value. See set-methods for constraints
     * @param chronoField
     * @param amount 
     */
    void set(ChronoField chronoField, int amount);
    /**
     * Copies fields to this from given MutableTime.
     * <p>
     * This implementation just calls get and set methods for every supported
     * field. 
     * @param mt 
     */
    default void set(MutableTime mt)
    {
        setYear(mt.getYear());
        setMonth(mt.getMonth());
        setDay(mt.getDay());
        setHour(mt.getHour());
        setMinute(mt.getMinute());
        setSecond(mt.getSecond());
        setMilliSecond(mt.getMilliSecond());
    }
    /**
     * Sets fields from ZonedDateTime.
     * <p>
     * Note! Zone is ignored.
     * @param zonedDateTime 
     */
    default void setZonedDateTime(ZonedDateTime zonedDateTime)
    {
        for (ChronoField cf : SupportedFields)
        {
            set(cf, zonedDateTime.get(cf));
        }
    }
    /**
     * Returns true if given MutableTime equals to this
     * @param mt
     * @return 
     */
    default boolean equals(MutableTime mt)
    {
        return 
            getYear() == mt.getYear() &&
            getMonth() == mt.getMonth() &&
            getDay() == mt.getDay() &&
            getHour() == mt.getHour() &&
            getMinute() == mt.getMinute() &&
            getSecond() == mt.getSecond() &
            getMilliSecond() == mt.getMilliSecond();
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
     * Converts 2 digit year to 4 digit. If year &lt; 70 add 2000. If 
     * year &lt; 100 add 1900.
     * @param year
     * @return 
     */
    default int convertTo4DigitYear(int year)
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
     * Returns Gregorian calendar constructed from MutableTime. 
     * <p>
     * Note! Milli seconds are ignored.
     * @return 
     */
    default GregorianCalendar getGregorianCalendar()
    {
        return new GregorianCalendar(getYear(), getMonth()+1, getDay(), getHour(), getMinute(), getSecond());
    }

}
