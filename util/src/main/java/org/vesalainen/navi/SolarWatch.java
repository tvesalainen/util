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
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.LongGetter;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarWatch implements Runnable
{
    public enum DayPhase {DAY, NIGHT, TWILIGHT};
    
    private final LongGetter millis;
    private boolean running;
    private LongGetter updateSeconds;
    private final DoubleGetter latitude;
    private final DoubleGetter longitude;
    private DoubleGetter twilightAngle;
    private DayPhase phase = DAY;
    private final GregorianCalendar cal = new GregorianCalendar();
    private final CachedScheduledThreadPool executor;
    private final List<Consumer<DayPhase>> observers = new ArrayList<>();

    public SolarWatch(DoubleGetter latitude, DoubleGetter longitude, DoubleGetter twilightAngle)
    {
        this(System::currentTimeMillis, new CachedScheduledThreadPool(), ()->60, latitude, longitude, twilightAngle);
    }

    public SolarWatch(LongGetter millis, CachedScheduledThreadPool executor, LongGetter updateSeconds, DoubleGetter latitude, DoubleGetter longitude, DoubleGetter twilightAngle)
    {
        this.millis = millis;
        this.executor = executor;
        this.updateSeconds = updateSeconds;
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
        if (running)
        {
            throw new IllegalStateException();
        }
        running = true;
        run();
    }
    public void stop()
    {
        if (!running)
        {
            throw new IllegalStateException();
        }
        running = false;
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
        executor.schedule(this::run, updateSeconds.getLong(), SECONDS);
    }
    
    private DayPhase phase()
    {
        cal.setTimeInMillis(millis.getLong());
        AzimuthZenithAngle angle = Grena3.calculateSolarPosition(cal, latitude.getDouble(), longitude.getDouble(), DeltaT.estimate(cal));
        double zenith = angle.getZenithAngle();
        if (zenith < 90)
        {
            return DAY;
        }
        else
        {
            if (zenith < 90 + twilightAngle.getDouble())
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
