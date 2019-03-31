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

import java.awt.geom.Path2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PlusShape extends Path2D.Double
{

    public PlusShape()
    {
        moveTo(0, 2);
        lineTo(0, 3);
        lineTo(2, 3);
        lineTo(2, 5);
        lineTo(3, 5);
        lineTo(3, 3);
        lineTo(5, 3);
        lineTo(5, 2);
        lineTo(3, 2);
        lineTo(3, 0);
        lineTo(2, 0);
        lineTo(2, 2);
        closePath();
    }
    
}
