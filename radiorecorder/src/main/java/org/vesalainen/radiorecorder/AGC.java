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
package org.vesalainen.radiorecorder;

import java.io.IOException;
import org.vesalainen.ham.DataListener;
import org.vesalainen.nio.IntArray;
import org.vesalainen.nmea.icommanager.IcomManager;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AGC extends JavaLogging implements DataListener
{
    private double highLimit;
    private double lowLimit;
    private IcomManager manager;
    private double amplitude;

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
        double min = array.getMaxPossibleValue();
        double max = array.getMinPossibleValue();
        int len = array.length();
        for (int ii=0;ii<len;ii++)
        {
            int v = array.get(ii);
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        amplitude = (max-min)/(array.getMaxPossibleValue()-array.getMinPossibleValue());
        try
        {
            if (amplitude > highLimit)
            {
                manager.adjustRFGain(-1);
            }
            else
            {
                if (amplitude < lowLimit)
                {
                    manager.adjustRFGain(1);
                }
            }
        }
        catch (IOException | InterruptedException ex)
        {
            warning("%s", ex.getMessage());
        }        
    }
    
}
