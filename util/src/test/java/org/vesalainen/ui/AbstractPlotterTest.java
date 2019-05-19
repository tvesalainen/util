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

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ui.TextAlignment.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractPlotterTest
{

    private final FontRenderContext frc;
    
    public AbstractPlotterTest()
    {
        frc = new FontRenderContext(null, true, true);
    }

    @Test
    public void testFontBounds()
    {
        String txt = "20";
        FontRenderContext frc = new FontRenderContext(null, false, true);
        Font font = new Font("arial", 0, 10);
        Rectangle2D stringBounds = font.getStringBounds(txt, frc);
        GlyphVector gv = font.createGlyphVector(frc, txt);
        Rectangle2D logicalBounds = gv.getLogicalBounds();
        Rectangle2D visualBounds = gv.getVisualBounds();
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
    }
    @Test
    public void testAlignShape()
    {
        Font font = new Font("arial", 0, 10);
        GlyphVector gv = font.createGlyphVector(frc, "qwerty");
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
        Shape as = AbstractPlotter.alignShape(5, 6, 0, s);
        Rectangle2D b2 = as.getBounds2D();
        assertEquals(5, b2.getMinX(), 1e-10);
        assertEquals(6, b2.getMinY(), 1e-10);
        assertEquals(b1.getWidth(), b2.getWidth(), 1e-10);
        assertEquals(b1.getHeight(), b2.getHeight(), 1e-10);
    }
    @Test
    public void testAlignShapeTransform()
    {
        Font font = new Font("arial", 0, 10);
        GlyphVector gv = font.createGlyphVector(frc, "qwerty");
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        Shape as = AbstractPlotter.alignShape(5, 6, 0, s, at);
        Rectangle2D b2 = as.getBounds2D();
        assertEquals(5, b2.getMinX(), 1e-10);
        assertEquals(6, b2.getMinY(), 1e-10);
        assertEquals(b1.getWidth(), b2.getWidth(), 1e-10);
        assertEquals(b1.getHeight(), b2.getHeight(), 1e-10);
    }
    @Test
    public void testAlignShapeStart()
    {
        Font font = new Font("arial", 0, 10);
        GlyphVector gv = font.createGlyphVector(frc, "qwerty");
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        Shape as = AbstractPlotter.alignShape(0, 0, 0, s, at, START_X, START_Y);
        Rectangle2D b2 = as.getBounds2D();
        assertEquals(0, b2.getMinX(), 1e-10);
        assertEquals(0, b2.getMinY(), 1e-10);
        assertEquals(b1.getWidth(), b2.getWidth(), 1e-10);
        assertEquals(b1.getHeight(), b2.getHeight(), 1e-10);
    }
    @Test
    public void testAlignShapeMiddle()
    {
        Font font = new Font("arial", 0, 10);
        GlyphVector gv = font.createGlyphVector(frc, "qwerty");
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
        Shape as = AbstractPlotter.alignShape(0, 0, 0, s, MIDDLE_X, MIDDLE_Y);
        Rectangle2D b2 = as.getBounds2D();
        assertEquals(0, b2.getCenterX(), 1e-10);
        assertEquals(0, b2.getCenterY(), 1e-10);
        assertEquals(b1.getWidth(), b2.getWidth(), 1e-10);
        assertEquals(b1.getHeight(), b2.getHeight(), 1e-10);
    }
    @Test
    public void testAlignShapeEnd()
    {
        Font font = new Font("arial", 0, 10);
        GlyphVector gv = font.createGlyphVector(frc, "qwerty");
        Shape s = gv.getOutline();
        Rectangle2D b1 = s.getBounds2D();
        Shape as = AbstractPlotter.alignShape(0, 0, 0, s, END_X, END_Y);
        Rectangle2D b2 = as.getBounds2D();
        assertEquals(0, b2.getMaxX(), 1e-10);
        assertEquals(0, b2.getMaxY(), 1e-10);
        assertEquals(b1.getWidth(), b2.getWidth(), 1e-10);
        assertEquals(b1.getHeight(), b2.getHeight(), 1e-10);
    }
    
}
