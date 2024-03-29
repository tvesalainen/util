/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham;

import java.io.IOException;
import org.vesalainen.math.sliding.DoubleTimeoutSlidingAverage;
import org.vesalainen.nio.IntArray;
import org.vesalainen.nmea.icommanager.IcomManager;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AGC extends JavaLogging implements DataListener
{
    private static final long TIME_LIMIT = 3000;
    private double highLimit;
    private double lowLimit;
    private double max;
    private IcomManager manager;
    private long lastAdjust;
    private int rfg;

    public AGC(IcomManager manager, double upLimit, double downLimit)
    {
        super(AGC.class);
        this.manager = manager;
        this.highLimit = upLimit;
        this.lowLimit = downLimit;
        
    }
    
    @Override
    public void update(IntArray array)
    {
        double maxPos = array.getMaxPossibleValue();
        max = 0;
        int len = array.length();
        for (int ii=0;ii<len;ii++)
        {
            int v = array.get(ii);
            max = Math.max(max, Math.abs(v/maxPos));
        }
        long now = System.currentTimeMillis();
        if (now > lastAdjust + TIME_LIMIT)
        {
            try
            {
                if (max > highLimit)
                {
                    rfg = manager.adjustRFGain(-1);
                    lastAdjust = now;
                    fine("lower %.1f", max);
                }
                else
                {
                    if (max < lowLimit)
                    {
                        rfg = manager.adjustRFGain(1);
                        lastAdjust = now;
                        fine("higher %.1f %d", max, rfg);
                    }
                }
            }
            catch (IOException | InterruptedException ex)
            {
                warning("%s", ex.getMessage());
            }
        }
    }

    public int getRfg()
    {
        return rfg;
    }

    @Override
    public String toString()
    {
        return "AGC{" + "max=" + max + ", rfg=" + rfg + '}';
    }
    
}
