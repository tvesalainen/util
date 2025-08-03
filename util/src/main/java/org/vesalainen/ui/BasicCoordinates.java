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

import org.vesalainen.math.DoubleTransform;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import org.vesalainen.ui.scale.Scale;
import org.vesalainen.ui.scale.ScaleLevel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicCoordinates extends CoordinatesGenerator
{
    protected Map<Direction,Scaler> map = new EnumMap(Direction.class);
    protected Map<Direction,ScaleLevel> levelMap = new EnumMap(Direction.class);
    protected Map<Direction,DrawContext> ctxMap = new EnumMap(Direction.class);

    public BasicCoordinates(AbstractPlotter plotter)
    {
        super(plotter);
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
        DoubleTransform combinedTransform = plotter.combinedTransform;
        FontRenderContext fontRenderContext = plotter.fontRenderContext;
        Rectangle2D bounds = new Rectangle2D.Double();
        map.forEach((direction, scaler)->
        {
            plotter.copy(ctxMap.get(direction));
            switch (direction)
            {
                case TOP:
                    scaler.set(origUserBounds.getMinX(), origUserBounds.getMaxX());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, origUserBounds.getMaxY(), bounds));
                    plotter.setMargin(bounds, direction);
                    break;
                case BOTTOM:
                    scaler.set(origUserBounds.getMinX(), origUserBounds.getMaxX());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, origUserBounds.getMinY(), bounds));
                    plotter.setMargin(bounds, direction);
                    break;
                case LEFT:
                    scaler.set(origUserBounds.getMinY(), origUserBounds.getMaxY());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, origUserBounds.getMinX(), bounds));
                    plotter.setMargin(bounds, direction);
                    break;
                case RIGHT:
                    scaler.set(origUserBounds.getMinY(), origUserBounds.getMaxY());
                    levelMap.put(direction, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, origUserBounds.getMaxX(), bounds));
                    plotter.setMargin(bounds, direction);
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
                        plotter.drawScreenText(value, origUserBounds.getMinY(), label, TextAlignment.MIDDLE_X, TextAlignment.START_Y);
                    });
                    break;
                case LEFT:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                        plotter.drawScreenText(origUserBounds.getMinX(), value, label, TextAlignment.END_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
                case RIGHT:
                    scaler.forEach(locale, levelMap.get(direction), (value,label)->
                    {
                        plotter.drawCoordinateLine(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value);
                        plotter.drawScreenText(origUserBounds.getMaxX(), value, label, TextAlignment.START_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
            }
        });
    }
}
