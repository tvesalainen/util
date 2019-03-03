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

import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicTitle extends BackgroundGenerator
{
    protected Direction direction;
    protected Rectangle2D bounds;
    protected Shape title;

    public BasicTitle(AbstractPlotter plotter, Direction direction, String text)
    {
        super(plotter);
        this.direction = direction;
        GlyphVector glyphVector = font.createGlyphVector(plotter.fontRenderContext, text);
        Shape shape = glyphVector.getOutline();
        switch (direction)
        {
            case TOP:
            case BOTTOM:
                this.title = shape;
                break;
            case LEFT:
                this.title = new Path2D.Double(shape, AffineTransform.getQuadrantRotateInstance(3));
                break;
            case RIGHT:
                this.title = new Path2D.Double(shape, AffineTransform.getQuadrantRotateInstance(1));
                break;
        }
        this.bounds = title.getBounds2D();
    }

    @Override
    public void addMargin()
    {
        plotter.setMargin(bounds, direction);
    }

    @Override
    public void addShapes()
    {
        switch (direction)
        {
            case TOP:
                plotter.drawScreen(origUserBounds.getCenterX(), origUserBounds.getMaxY(), title, TextAlignment.MIDDLE_X, TextAlignment.END_Y);
                break;
            case BOTTOM:
                plotter.drawScreen(origUserBounds.getCenterX(), origUserBounds.getMinY(), title, TextAlignment.MIDDLE_X);
                break;
            case LEFT:
                plotter.drawScreen(origUserBounds.getMinX(), origUserBounds.getCenterY(), title, TextAlignment.END_X, TextAlignment.MIDDLE_Y);
                break;
            case RIGHT:
                plotter.drawScreen(origUserBounds.getMaxX(), origUserBounds.getCenterY(), title, TextAlignment.START_X, TextAlignment.MIDDLE_Y);
                break;
        }
    }
}
