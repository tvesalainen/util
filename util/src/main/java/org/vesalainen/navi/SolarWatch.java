/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import java.time.Clock;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.function.DoubleSupplier;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;
import net.e175.klaus.solarpositioning.SPA;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarWatch implements Runnable
{
    private final Clock clock;
    private final long periodMillis;
    private ScheduledFuture<?> future;
    private final DoubleSupplier latitude;
    private final DoubleSupplier longitude;
    private double twilightAngle = 6;

    enum DayPhase {DAY, NIGHT, TWILIGHT};
    private final GregorianCalendar cal = new GregorianCalendar();
    private final CachedScheduledThreadPool executor;

    public SolarWatch(long period, TimeUnit unit, DoubleSupplier latitude, DoubleSupplier longitude)
    {
        this(Clock.systemDefaultZone(), new CachedScheduledThreadPool(), period, unit, latitude, longitude);
    }

    public SolarWatch(Clock clock, CachedScheduledThreadPool executor, long period, TimeUnit unit, DoubleSupplier latitude, DoubleSupplier longitude)
    {
        this.clock = clock;
        this.executor = executor;
        this.periodMillis = MILLISECONDS.convert(period, unit);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException();
        }
        future = executor.iterateAtFixedRate(0, periodMillis, MILLISECONDS, this);
    }
    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException();
        }
        future.cancel(true);
        future = null;
    }
    
    @Override
    public void run()
    {
        cal.setTimeInMillis(clock.millis());
        AzimuthZenithAngle angle = Grena3.calculateSolarPosition(cal, latitude.getAsDouble(), longitude.getAsDouble(), DeltaT.estimate(cal));
    }
    
    DayPhase phase(double zenith)
    {
        if (zenith < 90)
        {
            return DAY;
        }
        else
        {
            if (zenith < 90 + twilightAngle)
            {
                return TWILIGHT;
            }
            else
            {
                return NIGHT;
            }
        }
    }
}
