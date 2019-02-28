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

import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.vesalainen.ui.scale.Scale;
import org.vesalainen.ui.scale.ScaleLevel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractCoordinate extends SimpleDrawContext
{
    protected Direction direction;
    protected Scaler scaler;
    protected ScaleLevel level;

    public AbstractCoordinate(DrawContext ctx, Scale scale, Direction direction)
    {
        super(ctx);
        this.scaler = new Scaler(scale);
        this.direction = direction;
    }
    
    public void addMargin(AbstractPlotter plotter)
    {
        DoubleBounds userBounds = plotter.userBounds;
        DoubleTransform combinedTransform = plotter.combinedTransform;
        FontRenderContext fontRenderContext = plotter.fontRenderContext;
        Rectangle2D bounds = new Rectangle2D.Double();
        scaler.set(userBounds.getMinX(), userBounds.getMaxX());
        switch (direction)
        {
            case TOP:
                level = scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, userBounds.getMaxY(), bounds);
                plotter.setMargin(bounds, direction);
                break;
            case BOTTOM:
                level = scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, userBounds.getMinY(), bounds);
                plotter.setMargin(bounds, direction);
                break;
            case LEFT:
                level = scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, userBounds.getMinX(), bounds);
                plotter.setMargin(bounds, direction);
                break;
            case RIGHT:
                level = scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, userBounds.getMaxX(), bounds);
                plotter.setMargin(bounds, direction);
                break;
        }
    }
    public void generate(AbstractPlotter plotter, DoubleBounds origUserBounds)
    {
        plotter.copy(this);
        DoubleBounds userBounds = plotter.userBounds;
        switch (direction)
        {
            case TOP:
                scaler.forEach(locale, level, (value,label)->
                {
                    plotter.drawCoordinateLine(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY());
                    plotter.drawScreenText(value, userBounds.getMaxY(), label, TextAlignment.MIDDLE_X, TextAlignment.END_Y);
                });
                break;
            case BOTTOM:
                scaler.forEach(locale, level, (value,label)->
                {
                    plotter.drawCoordinateLine(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY());
                    plotter.drawScreenText(value, userBounds.getMinY(), label, TextAlignment.MIDDLE_X);
                });
                break;
            case LEFT:
                scaler.forEach(locale, level, (value,label)->
                {
                    plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                    plotter.drawScreenText(origUserBounds.getMinX(), value, label, TextAlignment.END_X, TextAlignment.MIDDLE_Y);
                });
                break;
            case RIGHT:
                scaler.forEach(locale, level, (value,label)->
                {
                    plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                    plotter.drawScreenText(origUserBounds.getMaxX(), value, label, TextAlignment.START_X, TextAlignment.MIDDLE_Y);
                });
                break;
        }
    }
}
