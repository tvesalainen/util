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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Bounds extends Rectangle
{

    public Bounds()
    {
        clear();
    }

    public Bounds(Rectangle r)
    {
        super(r);
    }

    public Bounds(int x, int y, int width, int height)
    {
        super(x, y, width, height);
    }

    public Bounds(int width, int height)
    {
        super(width, height);
    }

    public Bounds(Point p, Dimension d)
    {
        super(p, d);
    }

    public Bounds(Point p)
    {
        super(p);
    }

    public Bounds(Dimension d)
    {
        super(d);
    }
    
    public final void clear()
    {
        setRect(Integer.MAX_VALUE/2, Integer.MAX_VALUE/2, -Integer.MAX_VALUE/2, -Integer.MAX_VALUE/2);
    }
}
