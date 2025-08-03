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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.EnumMap;
import org.vesalainen.util.LongMap;

/**
 * A mutable clock implementation. Clock fields can be changed. Depending on
 * base clock the clock is running or fixed.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MutableClock extends Clock implements MutableDateTime
{
    protected LongMap<ChronoField> fields = new LongMap<>(new EnumMap<>(ChronoField.class));
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

    public LongMap<ChronoField> getFields()
    {
        return fields;
    }

    @Override
    public ZoneId getZone()
    {
        return clock.getZone();
    }

    @Override
    public void setZone(ZoneId zoneId)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        millis += get(ChronoField.HOUR_OF_DAY)*HOUR_IN_MILLIS;
        millis += get(ChronoField.MINUTE_OF_HOUR)*MINUTE_IN_MILLIS;
        millis += get(ChronoField.SECOND_OF_MINUTE)*SECOND_IN_MILLIS;
        millis += get(ChronoField.NANO_OF_SECOND)/1000000;
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
            get(ChronoField.NANO_OF_SECOND),
            clock.getZone());
    }
    /**
     * Sets time by using milli seconds from epoch.
     * @param millis 
     */
    public void setMillis(long millis)
    {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
        set(zonedDateTime);
    }

    @Override
    public long getLong(TemporalField chronoField)
    {
        checkField(chronoField);
        ChronoField cf = (ChronoField) chronoField;
        if (fields.containsKey(cf))
        {
            return fields.getLong(cf);
        }
        else
        {
            return (int) chronoField.range().getMinimum();
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
        long value = -1;
        if (fields.containsKey(cf))
        {
            value = fields.getLong(cf);
        }
        if (value != amount)
        {
            updated = clock.millis();
            fields.put(cf, amount);
            if (isDateField(cf))
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
