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
package org.vesalainen.navi;

import static org.vesalainen.navi.AnchorWatch.toMeters;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractLocationObserver implements LocationObserver
{
    protected double lastLongitude = Double.NaN;
    protected double lastLatitude = Double.NaN;
    protected long lastTime = -1;
    @Override
    public final void update(double longitude, double latitude, long time)
    {
        update(longitude, latitude, time, Double.NaN);
    }
    @Override
    public final void update(double longitude, double latitude, long time, double accuracy)
    {
        if (Double.isNaN(lastLongitude))
        {
            update(longitude, latitude, time, accuracy, 0.0);
        }
        else
        {
            double speed = Navis.speed(lastTime, lastLatitude, lastLongitude, time, latitude, longitude);
            update(longitude, latitude, time, accuracy, speed);
        }
        lastLongitude = longitude;
        lastLatitude = latitude;
        lastTime = time;
    }
}
