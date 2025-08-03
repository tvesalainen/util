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

import java.awt.Rectangle;
import org.vesalainen.math.DoubleTransform;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.scale.Scale;
import org.vesalainen.ui.scale.ScaleLevel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolarCoordinates extends CoordinatesGenerator
{
    protected Map<Direction,Scaler> map = new EnumMap(Direction.class);
    protected Map<Direction,ScaleLevel> levelMap = new EnumMap(Direction.class);
    protected Map<Direction,DrawContext> ctxMap = new EnumMap(Direction.class);
    private final DoubleBounds origPolarBounds;
    private final DoubleSupplier transY;

    public PolarCoordinates(AbstractPlotter plotter, DoubleBounds origPolarBounds, DoubleSupplier transY)
    {
        super(plotter);
        this.origPolarBounds = origPolarBounds;
        this.transY = transY;
    }
    
    @Override
    public void addCoordinate(Direction direction, Scale scale)
    {
        switch (direction)
        {
            case LEFT:
                map.put(direction, new Scaler(yScale));
                break;
            case BOTTOM:
                map.put(direction, new Scaler(xScale));
                break;
            case TOP:
            case RIGHT:
                map.put(direction, new Scaler(scale));
                break;
        }
        ctxMap.put(direction, new SimpleDrawContext(plotter));
    }
    @Override
    protected void addMargin()
    {
        DoubleTransform combinedTransform = (x,y,c)->plotter.combinedTransform.transform(x, y+transY.getAsDouble(), c);
        FontRenderContext fontRenderContext = plotter.fontRenderContext;
        Rectangle screenBounds = plotter.screenBounds;
        Rectangle2D bounds = new Rectangle2D.Double();
        map.forEach((direction, scaler)->
        {
            plotter.copy(ctxMap.get(direction));
            switch (direction)
            {
                case TOP:
                    scaler.set(origPolarBounds.getMinX(), origPolarBounds.getMaxX());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, origPolarBounds.getMaxY(), bounds));
                    double dx = bounds.getWidth() - screenBounds.width+transY.getAsDouble();
                    double dy = bounds.getHeight() - screenBounds.height+transY.getAsDouble();
                    double max = Math.max(dx, dy);
                    if (max > 0)
                    {
                        plotter.setMargin(-max, TOP);
                    }
                    break;
                case LEFT:
                    scaler.set(origPolarBounds.getMinY(), origPolarBounds.getMaxY());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, origPolarBounds.getMinX(), bounds));
                    break;
                case RIGHT:
                    scaler.set(origPolarBounds.getMinY(), origPolarBounds.getMaxY());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, origPolarBounds.getMaxX(), bounds));
                    break;
            }
        });
    }
    @Override
    protected void addShapes()
    {
        map.forEach((direction, scaler)->
        {
            plotter.copy(ctxMap.get(direction));
            switch (direction)
            {
                case TOP:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        plotter.drawCoordinateLine(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY());
                        plotter.drawScreenText(value, origUserBounds.getMaxY(), label, TextAlignment.MIDDLE_X, TextAlignment.END_Y);
                    });
                    break;
                case BOTTOM:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        plotter.drawCoordinateLine(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY());
                        plotter.drawScreenText(value, origUserBounds.getMinY(), label, TextAlignment.MIDDLE_X);
                    });
                    break;
                case LEFT:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        value += transY.getAsDouble();
                        plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                        plotter.drawScreenText(origUserBounds.getMinX(), value, label, TextAlignment.END_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
                case RIGHT:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        value += transY.getAsDouble();
                        plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                        plotter.drawScreenText(origUserBounds.getMaxX(), value, label, TextAlignment.START_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
            }
        });
    }
}
