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

package org.vesalainen.ui;

import org.vesalainen.math.AbstractCircle;
import org.vesalainen.math.Circles;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableInnerCircle extends MouldableCircle implements MouldableCircle.MouldableCircleObserver
{
    private MouldableSector outer;

    public MouldableInnerCircle(MouldableSector outer, AbstractCircle circle)
    {
        super(circle);
        this.outer = outer;
    }

    @Override
    protected Cursor createRadiusCursor(double x, double y)
    {
        return new InnerRadiusCursor(outer);
    }

    @Override
    public void centerMoved(double x, double y)
    {
        setX(x);
        setY(y);
    }

    @Override
    public void radiusChanged(double r)
    {
    }
    
    protected class InnerRadiusCursor implements Cursor
    {
        private MouldableSector outer;
        private InnerRadiusCursor(MouldableSector outer)
        {
            this.outer = outer;
        }
        @Override
        public Cursor update(double x, double y)
        {
            double distance = Circles.distanceFromCenter(circle, x, y);
            if (distance < outer.getRadius())
            {
                setRadius(distance);
            }
            return this;
        }

        @Override
        public void ready(double x, double y)
        {
            update(x, y);
            if (outer.rawNearCircle(x, y))
            {
                outer.sector.makeCircle();
            }
            fireRadius(getRadius());
        }
    }
}
