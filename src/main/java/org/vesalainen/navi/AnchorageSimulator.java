/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorageSimulator extends TimerTask
{
    private final URL url;
    private Timer timer;
    private AnchorWatch anchorWatch;
    private DataInputStream dis;

    public AnchorageSimulator()
    {
        this(null);
    }

    public AnchorageSimulator(Timer timer)
    {
        this.timer = timer;
        this.url = AnchorageSimulator.class.getResource("/simulation.ser");
        if (url == null)
        {
            throw new IllegalArgumentException("resource /simulation.ser not found");
        }
    }
    /**
     * Starts simulating anchorige.
     * @param anchorWatch
     * @param period Update rate in millis.
     * @param isDaemon Sets timer thread. In single thread this should be false. 
     * This parament is used only if external Timer was not provided!
     * @throws IOException 
     */
    public void simulate(AnchorWatch anchorWatch, long period, boolean isDaemon) throws IOException
    {
        this.anchorWatch = anchorWatch;
        dis = new DataInputStream(new BufferedInputStream(url.openStream()));
        if (timer == null)
        {
            timer = new Timer("AnchorageSimulator", isDaemon);
        }
        timer.scheduleAtFixedRate(this, 0, period);
    }

    @Override
    public void run()
    {
        try
        {
            float lon = dis.readFloat();
            float lat = dis.readFloat();
            anchorWatch.update(lon, lat, System.currentTimeMillis(), 1);
        }
        catch (EOFException ex)
        {
            cancel();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
