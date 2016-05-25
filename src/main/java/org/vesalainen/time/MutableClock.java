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
 *
 * @author tkv
 */
public final class MutableClock extends Clock implements TemporalAccessor, MutableTime
{
    private static final long SecondInMillis = 1000;
    private static final long MinuteInMillis = SecondInMillis*60;
    private static final long HourInMillis = MinuteInMillis*60;
    private IntMap<ChronoField> fields = new IntMap<>(new EnumMap<ChronoField,IntReference>(ChronoField.class));
    private  Clock clock;
    private boolean needCalc;
    private long dateMillis;
    private long updated;

    public MutableClock()
    {
        this(Clock.systemUTC());
    }

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
        millis += clock.millis() - updated;
        millis += dateMillis;
        millis += get(ChronoField.HOUR_OF_DAY)*HourInMillis;
        millis += get(ChronoField.MINUTE_OF_HOUR)*MinuteInMillis;
        millis += get(ChronoField.SECOND_OF_MINUTE)*SecondInMillis;
        millis += get(ChronoField.MILLI_OF_SECOND);
        return millis;
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
    private void setZonedDateTime(ZonedDateTime zdt)
    {
        for (ChronoField cf : SupportedFields)
        {
            set(cf, zdt.get(cf));
        }
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

}
