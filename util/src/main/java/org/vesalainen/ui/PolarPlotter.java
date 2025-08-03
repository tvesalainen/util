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
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.stream.Stream;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.scale.AngleScale;
import org.vesalainen.ui.scale.MergeScale;
import org.vesalainen.ui.scale.Scale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarPlotter extends AbstractPlotter
{
    private DoubleBounds bounds = new DoubleBounds();
    private double translateY;

    public PolarPlotter(int width, int height, Color background)
    {
        this(width, height, background, MergeScale.BASIC15);
    }
    public PolarPlotter(int width, int height, Color background, Scale yScale)
    {
        super(width, height, background, true, new AngleScale(), yScale, new PolarTransform());
    }

    @Override
    protected void plot(Drawer drawer)
    {
        for (Drawable drawable : shapes)
        {
            bounds.add(drawable.getBounds());
        }
        if (bounds.getMinY() < bounds.height)
        {
            translateY = bounds.height-bounds.getMinY();
            AffineTransform at = AffineTransform.getTranslateInstance(0, translateY);
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
        for (double a=0;a<360;a+=90)
        {
            updatePoint(a, maxY);
        }
    }

    @Override
    @Deprecated public void drawCoordinateY()
    {
    }

    @Override
    @Deprecated public void drawCoordinateX()
    {
    }

    @Override
    public void drawCoordinates()
    {
        super.drawCoordinates(LEFT, TOP);
    }
    /**
     * @deprecated Use drawCoordinates()
     * @param directions 
     */
    @Override
    public void drawCoordinates(Direction... directions)
    {
        super.drawCoordinates(directions);
    }

    @Override
    public void drawCoordinates(Scale scale, Direction... directions)
    {
        drawCoordinates(new PolarCoordinates(this, bounds, this::getTranslateY), scale, directions);
    }

    public double getTranslateY()
    {
        return translateY;
    }

}
