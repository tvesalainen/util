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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.stream.Stream;
import org.vesalainen.ui.scale.AngleScale;
import org.vesalainen.ui.scale.MergeScale;
import org.vesalainen.ui.scale.Scale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarPlotter extends AbstractPlotter
{

    private final boolean useRadians;

    public PolarPlotter(int width, int height, Color background)
    {
        this(width, height, background, MergeScale.BASIC15);
    }
    public PolarPlotter(int width, int height, Color background, Scale yScale)
    {
        this(width, height, background, yScale, false);
    }
    public PolarPlotter(int width, int height, Color background, Scale yScale, boolean useRadians)
    {
        super(width, height, background, true, new AngleScale(), yScale, new PolarTransform(useRadians));
        this.useRadians = useRadians;
    }

    @Override
    protected void plot(Drawer drawer)
    {
        DoubleBounds bounds = new DoubleBounds();
        for (Drawable drawable : shapes)
        {
            bounds.add(drawable.getBounds());
        }
        if (bounds.getMinY() < bounds.height)
        {
            AffineTransform at = AffineTransform.getTranslateInstance(0, bounds.height-bounds.getMinY());
            for (Drawable drawable : shapes)
            {
                drawable.transform(at);
            }
        }
        super.plot(drawer);
    }

    @Override
    public void update(Stream<Rectangle2D> shapes)
    {
        super.update(shapes);
        double maxY = userBounds.getMaxY();
        double full = useRadians ? 2*Math.PI : 360;
        for (double a=0;a<full;a+=full/4)
        {
            updatePoint(a, maxY);
        }
    }
    
}
