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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoField.*;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.EnumMap;
import java.util.Objects;
import org.vesalainen.util.LongMap;

/**
 * SimpleMutableDateTime is a mutable Temporal implementation which supports creating
 * date objects from fields. Supported fields are from year to nanos.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleMutableDateTime implements MutableDateTime, Cloneable
{
    private LongMap<ChronoField> fields = new LongMap<>(new EnumMap<>(ChronoField.class));
    private ZoneId zoneId;
    /**
     * Creates uninitialized SimpleMutableDateTime
     */
    public SimpleMutableDateTime()
    {
        this.zoneId = ZoneOffset.UTC;
    }
    /**
     * Creates SimpleMutableDateTime in UTC
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param milliSecond 
     */
    public SimpleMutableDateTime(int year, int month, int day, int hour, int minute, int second, int milliSecond)
    {
        this(year, month, day, hour, minute, second, milliSecond, ZoneId.of("Z"));
    }
    /**
     * Creates SimpleMutableDateTime 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @param milliSecond
     * @param zoneId 
     */
    public SimpleMutableDateTime(int year, int month, int day, int hour, int minute, int second, int milliSecond, ZoneId zoneId)
    {
        Objects.requireNonNull(zoneId, "zoneId");
        setDate(year, month, day);
        setTime(hour, minute, second, milliSecond);
        this.zoneId = zoneId;
    }
    public static final SimpleMutableDateTime from(TemporalAccessor temporal)
    {
        SimpleMutableDateTime smt = SimpleMutableDateTime.epoch();
        smt.setZone(ZoneId.from(temporal));
        if (
                temporal.isSupported(YEAR) && 
                temporal.isSupported(MONTH_OF_YEAR) &&
                temporal.isSupported(DAY_OF_MONTH) &&
                temporal.isSupported(HOUR_OF_DAY) &&
                temporal.isSupported(MINUTE_OF_HOUR) &&
                temporal.isSupported(SECOND_OF_MINUTE) &&
                temporal.isSupported(NANO_OF_SECOND)
                )
        {
            smt.set(YEAR, temporal.get(YEAR));
            smt.set(MONTH_OF_YEAR, temporal.get(MONTH_OF_YEAR));
            smt.set(DAY_OF_MONTH, temporal.get(DAY_OF_MONTH));
            smt.set(HOUR_OF_DAY, temporal.get(HOUR_OF_DAY));
            smt.set(MINUTE_OF_HOUR, temporal.get(MINUTE_OF_HOUR));
            smt.set(SECOND_OF_MINUTE, temporal.get(SECOND_OF_MINUTE));
            smt.set(NANO_OF_SECOND, temporal.get(NANO_OF_SECOND));
            return smt;
        }
        throw new UnsupportedOperationException(temporal+" not supported");
    }

    @Override
    public <R> R query(TemporalQuery<R> query)
    {
        if (query == TemporalQueries.zoneId())
        {
            return (R) zoneId;
        }
        else
        {
            if (query == TemporalQueries.zone())
            {
                return (R) zoneId;
            }
            else
            {
                if (query == TemporalQueries.offset())
                {
                    return null;
                }
                else
                {
                    if (query == TemporalQueries.localDate() && fields.containsKey(YEAR))
                    {
                        return (R) LocalDate.of(getYear(), getMonth(), getDay());
                    }
                    else
                    {
                        if (query == TemporalQueries.localTime() && fields.containsKey(HOUR_OF_DAY))
                        {
                            return (R) LocalTime.of(getHour(), getMinute(), getSecond(), getMilliSecond()*1000000);
                        }
                    }
                }
            }
        }
        return null;    //query.queryFrom(this);
    }
    @Override
    public SimpleMutableDateTime clone()
    {
        return new SimpleMutableDateTime(getYear(), getMonth(), getDay(), getHour(), getMinute(), getSecond(), getMilliSecond(), zoneId);
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
                ZonedDateTime zonedDateTime = ZonedDateTime.from(this);
                ZonedDateTime plusDays = zonedDateTime.plusDays(delta);
                set(plusDays);
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
     * @deprecated Use ZonedDateTime::from
     * Creates ZonedDateTime.
     * @return 
     */
    public ZonedDateTime zonedDateTime()
    {
        return zonedDateTime(ZoneOffset.ofTotalSeconds(get(OFFSET_SECONDS)));
    }
    /**
     * @deprecated Use ZonedDateTime::from
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
        SimpleMutableDateTime smt = SimpleMutableDateTime.from(zdt);
        return smt;
    }
    /**
     * Creates SimpleMutableDateTime and initializes it using milliseconds from 
     * epoch.
     * @param millis
     * @return 
     */
    public static final SimpleMutableDateTime ofEpochMilli(long millis)
    {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
        SimpleMutableDateTime smt = SimpleMutableDateTime.from(zdt);
        return smt;
    }
    public static final SimpleMutableDateTime ofEpoch()
    {
        return new SimpleMutableDateTime(1970, 0, 0, 0, 0, 0, 0, ZoneOffset.UTC);
    }
    /**
     * Creates SimpleMutableDateTime and initializes it to given ZoneId.
     * @param zoneId
     * @return 
     */
    public static final SimpleMutableDateTime now(ZoneId zoneId)
    {
        ZonedDateTime zdt = ZonedDateTime.now(zoneId);
        SimpleMutableDateTime smt = SimpleMutableDateTime.from(zdt);
        return smt;
    }
    /**
     * Returns the inner field map.
     * @return 
     */
    public LongMap<? extends TemporalField> getFields()
    {
        return fields;
    }
    
    @Override
    public long getLong(TemporalField chronoField)
    {
        checkField(chronoField);
        ChronoField cf = (ChronoField)chronoField;
        if (fields.containsKey(cf))
        {
            return fields.getLong(cf);
        }
        else
        {
            return chronoField.range().getMinimum();
        }
    }
    @Override
    public void set(TemporalField chronoField, long amount)
    {
        checkField(chronoField);
        ChronoField cf = (ChronoField) chronoField;
        if (ChronoField.YEAR.equals(chronoField))
        {
            amount = convertTo4DigitYear(amount);
        }
        cf.checkValidValue(amount);
        fields.put(cf, amount);
    }

    @Override
    public ZoneId getZone()
    {
        return zoneId;
    }

    @Override
    public void setZone(ZoneId zoneId)
    {
        Objects.requireNonNull(zoneId, "zoneId");
        this.zoneId = zoneId;
    }

    @Override
    public String toString()
    {
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%s", 
                getYear(),
                getMonth(),
                getDay(),
                getHour(),
                getMinute(),
                getSecond(),
                getMilliSecond(),
                getZone()
                );
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        for (TemporalField cf : SUPPORTED_FIELDS)
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
