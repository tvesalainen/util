/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.awt.Color;
import java.awt.Rectangle;
import static java.lang.Math.*;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.math.MathFunction;
import org.vesalainen.ui.scale.CoordinateScale.LatitudeScale;
import org.vesalainen.ui.scale.CoordinateScale.LongitudeScale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChartPlotter extends AbstractPlotter
{
    
    public ChartPlotter(int width, int height)
    {
        this(width, height, Color.WHITE);
    }
    public ChartPlotter(int width, int height, Color background)
    {
        super(width, height, background, true, new LongitudeScale(), new LatitudeScale(), null);
    }

    @Override
    protected DoubleTransform createTransform()
    {
        DoubleBounds rect = new DoubleBounds();
        shapes.forEach((r)->rect.add(r.getBounds()));
        double centerY = rect.getCenterY();
        return DoubleTransform.composite(MathFunction.preMultiplier(MathFunction.IDENTITY, cos(toRadians(centerY))), MathFunction.IDENTITY);
    }
    
}
