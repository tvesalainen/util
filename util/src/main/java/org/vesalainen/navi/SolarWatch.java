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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarWatch implements Runnable
{
    public enum DayPhase {DAY, NIGHT, TWILIGHT};
    
    private final LongSupplier millis;
    private final long periodMillis;
    private ScheduledFuture<?> future;
    private final DoubleSupplier latitude;
    private final DoubleSupplier longitude;
    private DoubleSupplier twilightAngle;
    private DayPhase phase;
    private final GregorianCalendar cal = new GregorianCalendar();
    private final CachedScheduledThreadPool executor;
    private final List<Consumer<DayPhase>> observers = new ArrayList<>();

    public SolarWatch(DoubleSupplier latitude, DoubleSupplier longitude, DoubleSupplier twilightAngle)
    {
        this(System::currentTimeMillis, new CachedScheduledThreadPool(), 1, TimeUnit.MINUTES, latitude, longitude, twilightAngle);
    }

    public SolarWatch(LongSupplier millis, CachedScheduledThreadPool executor, long period, TimeUnit unit, DoubleSupplier latitude, DoubleSupplier longitude, DoubleSupplier twilightAngle)
    {
        this.millis = millis;
        this.executor = executor;
        this.periodMillis = MILLISECONDS.convert(period, unit);
        this.latitude = latitude;
        this.longitude = longitude;
        this.twilightAngle = twilightAngle;
    }

    public void addObserver(Consumer<DayPhase> observer)
    {
        observers.add(observer);
    }
    public void removeObserver(Consumer<DayPhase> observer)
    {
        observers.remove(observer);
    }

    public DayPhase getPhase()
    {
        return phase;
    }
    
    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException();
        }
        this.phase = phase();
        future = executor.iterateAtFixedRate(periodMillis, periodMillis, MILLISECONDS, this);
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
        DayPhase newPhase = phase();
        if (phase != newPhase)
        {
            phase = newPhase;
            observers.forEach((o)->o.accept(phase));
        }
    }
    
    private DayPhase phase()
    {
        cal.setTimeInMillis(millis.getAsLong());
        AzimuthZenithAngle angle = Grena3.calculateSolarPosition(cal, latitude.getAsDouble(), longitude.getAsDouble(), DeltaT.estimate(cal));
        double zenith = angle.getZenithAngle();
        if (zenith < 90)
        {
            return DAY;
        }
        else
        {
            if (zenith < 90 + twilightAngle.getAsDouble())
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
