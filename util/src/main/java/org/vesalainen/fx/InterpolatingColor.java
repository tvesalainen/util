/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import javafx.scene.paint.Color;
import org.vesalainen.math.AbstractLine;
import org.vesalainen.math.Line;
import org.vesalainen.math.RelaxedCubicSpline;
import org.vesalainen.navi.Navis;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class InterpolatingColor
{
    private RelaxedCubicSpline hue;
    private Line saturation;

    public InterpolatingColor(double... points)
    {
        hue = new RelaxedCubicSpline(points);
        saturation = new AbstractLine(points[0], 0.1, points[points.length-2], 1.0);
    }
    
    public Color color(double x)
    {
        return Color.hsb(Navis.normalizeAngle(hue.applyAsDouble(x)), saturation.getY(x), 1.0);
    }
}
