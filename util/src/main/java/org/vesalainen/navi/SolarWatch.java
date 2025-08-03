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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.LongGetter;
import static org.vesalainen.navi.SolarWatch.DayPhase.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;
/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SolarWatch extends JavaLogging
{
    public enum DayPhase {DAY, NIGHT, TWILIGHT};
    
    private final SolarPosition solarPosition;
    private final Clock clock;
    private final DoubleGetter latitude;
    private final DoubleGetter longitude;
    private DoubleGetter twilightAngle;
    private DayPhase phase = DAY;
    private final CachedScheduledThreadPool executor;
    private final List<Consumer<DayPhase>> observers = new ArrayList<>();

    public SolarWatch(DoubleGetter latitude, DoubleGetter longitude, double twilightAngle)
    {
        this(Clock.systemDefaultZone(), new CachedScheduledThreadPool(), latitude, longitude, twilightAngle);
    }

    public SolarWatch(Clock clock, CachedScheduledThreadPool executor, DoubleGetter latitude, DoubleGetter longitude, double twilightAngle)
    {
        super(SolarWatch.class);
        this.solarPosition = new SolarPosition(twilightAngle, ZonedDateTime.now(clock), longitude.getDouble(), latitude.getDouble());
        this.clock = clock;
        this.executor = executor;
        this.latitude = latitude;
        this.longitude = longitude;
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
        fire();
    }
    public void stop()
    {
    }
    
    private void fire()
    {
        solarPosition.set(ZonedDateTime.now(clock), longitude.getDouble(), latitude.getDouble());
        DayPhase phase = solarPosition.getDayPhase();
        observers.forEach((o)->o.accept(phase));
        switch (phase)
        {
            case DAY:
                ZonedDateTime sunset = solarPosition.nextSunset();
                executor.schedule(this::fire, sunset);
                info("%s next sunset at %s", phase, sunset);
                break;
            case NIGHT:
                ZonedDateTime dawn = solarPosition.nextDawn();
                executor.schedule(this::fire, dawn);
                info("%s next dawn at %s", phase, dawn);
                break;
            case TWILIGHT:
                ZonedDateTime dusk = solarPosition.nextDusk();
                ZonedDateTime sunrise = solarPosition.nextSunrise();
                if (dusk.isBefore(sunrise))
                {
                    executor.schedule(this::fire, dusk);
                    info("%s next dusk at %s", phase, dusk);
                }
                else
                {
                    executor.schedule(this::fire, sunrise);
                    info("%s next sunrise at %s", phase, sunrise);
                }
                break;
        }
    }
    
}
