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
import org.vesalainen.math.AbstractSector;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Circles;

/**
 *
 * @author Timo Vesalainen
 */
public class MouldableSectorWithInnerCircle extends MouldableSector
{
    private MouldableCircle innerCircle;
    
    public MouldableSectorWithInnerCircle(Circle circle)
    {
        this(new AbstractSector(circle));
    }

    public MouldableSectorWithInnerCircle(AbstractSector sector)
    {
        super(sector);
        innerCircle = new MouldableCircle(new AbstractCircle(sector, sector.getRadius()/2.0));
    }

    @Override
    public boolean isInside(double x, double y)
    {
        return super.isInside(x, y) || innerCircle.isInside(x, y);
    }

    @Override
    public Cursor getCursor(double x, double y)
    {
        Cursor cursor = super.getCursor(x, y);
        if (cursor != null)
        {
            return cursor;
        }
        else
        {
            if (!isInside(x, y) && !innerCircle.isNearCenter(x, y))
            {
                return innerCircle.getCursor(x, y);
            }
        }
        return null;
    }

    
    public MouldableCircle getInnerCircle()
    {
        return innerCircle;
    }
    
}
