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
package org.vesalainen.ui;

import java.awt.geom.Rectangle2D;

/**
 * DoubleBounds is a Rectangle2D.Double which initializes to java.lang.Double.MAX_VALUE/2, 
 * java.lang.Double.MAX_VALUE/2, -java.lang.Double.MAX_VALUE, -java.lang.Double.MAX_VALUE.
 * Bound accumulating is practically always automatic.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleBounds extends Rectangle2D.Double
{
    private boolean initialized;
    
    public DoubleBounds()
    {
        clear();
    }

    public DoubleBounds(double x, double y, double w, double h)
    {
        super(x, y, w, h);
    }
    
    public final void clear()
    {
        setRect(java.lang.Double.MAX_VALUE/2, java.lang.Double.MAX_VALUE/2, -java.lang.Double.MAX_VALUE, -java.lang.Double.MAX_VALUE);
    }
}
