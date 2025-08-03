/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleAdjustableClock implements AdjustableClock
{
    private Clock clock;
    private long offset;

    public SimpleAdjustableClock(Clock clock)
    {
        this(clock, 0);
    }
    public SimpleAdjustableClock(Clock clock, long offset)
    {
        this.clock = clock;
        this.offset = offset;
    }

    @Override
    public void adjust(long nanos)
    {
        offset += nanos;
    }

    @Override
    public long millis()
    {
        return clock.millis() + offset/1000000L;
    }

    @Override
    public Instant instant()
    {
        return clock.instant().plusNanos(offset);
    }

    @Override
    public long offset()
    {
        return offset;
    }
}
