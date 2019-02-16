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

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Collections;
import java.util.function.IntBinaryOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PaintPattern
{
    private PaintContext paintContext;

    public PaintPattern(BufferedImage image, Paint paint)
    {
        Rectangle bounds = new Rectangle(image.getWidth(), image.getHeight());
        this.paintContext = paint.createContext(image.getColorModel(), bounds, bounds, new AffineTransform(), new RenderingHints(Collections.EMPTY_MAP));
    }

    public IntBinaryOperator getPattern(Rectangle bounds)
    {
        return getPattern(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    public IntBinaryOperator getPattern(int x, int y, int w, int h)
    {
        return new Pattern(paintContext.getRaster(x, y, w, h));
    }
    private static class Pattern implements IntBinaryOperator
    {
        private Raster raster;

        public Pattern(Raster raster)
        {
            this.raster = raster;
        }
        
        @Override
        public int applyAsInt(int left, int right)
        {
            return raster.getSample(left, right, 0);
        }
        
    }
}
