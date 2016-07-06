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
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.EnumMap;
import org.vesalainen.util.IntMap;
import org.vesalainen.util.IntReference;

/**
 * A mutable clock implementation. Clock fields can be changed. Depending on
 * base clock the clock is running or fixed.
 * @author tkv
 */
public class MutableClock extends Clock implements TemporalAccessor, MutableDate
{
    private static final long SecondInMillis = 1000;
    private static final long MinuteInMillis = SecondInMillis*60;
    private static final long HourInMillis = MinuteInMillis*60;
    protected IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    protected Clock clock;
    protected boolean needCalc;
    protected long dateMillis;
    protected long updated;
    /**
     * Creates a MutableClock with systemUTC clock
     */
    public MutableClock()
    {
        this(Clock.systemUTC());
    }
    /**
     * Creates a MutableClock with given clock.
     * @param clock 
     */
    public MutableClock(Clock clock)
    {
        this.clock = clock;
    }

    void setClock(Clock clock)
    {
        this.clock = clock;
    }

    @Override
    public ZoneId getZone()
    {
        return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone)
    {
        return new MutableClock(clock.withZone(zone));
    }

    @Override
    public Instant instant()
    {
        return Instant.ofEpochMilli(millis());
    }
    
    @Override
    public long millis()
    {
        if (needCalc)
        {
            ZonedDateTime zonedDate = ZonedDateTime.of(
                    get(ChronoField.YEAR),
                    get(ChronoField.MONTH_OF_YEAR),
                    get(ChronoField.DAY_OF_MONTH),
                    0,
                    0,
                    0,
                    0,
                    clock.getZone());
            dateMillis = zonedDate.toInstant().toEpochMilli();
            needCalc = false;
        }
        long millis = 0;
        millis += clock.millis() - getUpdated();
        millis += dateMillis;
        millis += get(ChronoField.HOUR_OF_DAY)*HourInMillis;
        millis += get(ChronoField.MINUTE_OF_HOUR)*MinuteInMillis;
        millis += get(ChronoField.SECOND_OF_MINUTE)*SecondInMillis;
        millis += get(ChronoField.MILLI_OF_SECOND);
        return millis;
    }

    protected long getUpdated()
    {
        return updated;
    }

    /**
     * Returns ZonedDateTime created from latest update.
     * @return 
     */
    public ZonedDateTime getZonedDateTime()
    {
        return ZonedDateTime.of(
            get(ChronoField.YEAR),
            get(ChronoField.MONTH_OF_YEAR), 
            get(ChronoField.DAY_OF_MONTH), 
            get(ChronoField.HOUR_OF_DAY), 
            get(ChronoField.MINUTE_OF_HOUR), 
            get(ChronoField.SECOND_OF_MINUTE),
            get(ChronoField.MILLI_OF_SECOND)*1000000,
            clock.getZone());
    }
    @Override
    public boolean isSupported(TemporalField field)
    {
        if (field instanceof ChronoField)
        {
            ChronoField chronoField = (ChronoField) field;
            switch (chronoField)
            {
                case YEAR:
                case MONTH_OF_YEAR:
                case DAY_OF_MONTH:
                case HOUR_OF_DAY:
                case MINUTE_OF_HOUR:
                case SECOND_OF_MINUTE:
                case MILLI_OF_SECOND:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public long getLong(TemporalField field)
    {
        if (field instanceof ChronoField)
        {
            ChronoField chronoField = (ChronoField) field;
            if (fields.containsKey(chronoField))
            {
                return fields.getInt(chronoField);
            }
            else
            {
                throw new DateTimeException("no value for "+field);
            }
        }
        throw new UnsupportedTemporalTypeException(field.toString());
    }
    /**
     * Sets time by using milli seconds from epoch.
     * @param millis 
     */
    public void setMillis(long millis)
    {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
        setZonedDateTime(zonedDateTime);
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
        int value = -1;
        if (fields.containsKey(chronoField))
        {
            value = fields.getInt(chronoField);
        }
        if (value != amount)
        {
            updated = clock.millis();
            fields.put(chronoField, amount);
            if (isDateField(chronoField))
            {
                needCalc = true;
            }
        }
    }
    private boolean isDateField(ChronoField chronoField)
    {
        switch (chronoField)
        {
            case YEAR:
            case MONTH_OF_YEAR:
            case DAY_OF_MONTH:
                return true;
            default:
                return false;
        }
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
