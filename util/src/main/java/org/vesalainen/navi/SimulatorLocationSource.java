/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimulatorLocationSource extends LocationSource
{
    private AnchorageSimulator simulator;
    private final Timer timer;
    private final long period;
    private boolean isDaemon;

    public SimulatorLocationSource(Timer timer, long period, boolean isDaemon)
    {
        this.timer = timer;
        this.period = period;
        this.isDaemon = isDaemon;
    }

    @Override
    protected void start()
    {
        try
        {
            reset();    // simulator needs to reset
            simulator = new AnchorageSimulator(timer);
            simulator.simulate(this, period, isDaemon);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    protected void stop()
    {
        reset();    // simulator needs to reset
        simulator.cancel();
        simulator = null;
    }

}
