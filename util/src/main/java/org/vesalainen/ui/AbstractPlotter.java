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
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Rect;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractPlotter extends AbstractView
{
    private Locale locale = Locale.getDefault();
    private Color color = Color.BLACK;
    private Font font;
    private FontRenderContext fontRenderContext = new FontRenderContext(null, false, true);
    private final List<Drawable> shapes = new ArrayList<>();
    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    protected final Color background;
    private double lineWidth = 1.0;

    public AbstractPlotter(int width, int height, Color background)
    {
        this(width, height, background, true);
    }

    public AbstractPlotter(int width, int height, Color background, boolean keepAspectRatio)
    {
        super(keepAspectRatio);
        super.setScreen(width, height);
        this.background = background;
    }

    protected void plot(Drawer drawer)
    {
        update(shapes.stream().map(Drawable::getShape));
        drawer.setTransform(Transforms.affineTransformer(transform));
        shapes.forEach((d) ->
        {
            d.draw(drawer);
        });
    }
    /**
     * Clears the screen
     */
    public void clear()
    {
        shapes.clear();
    }

    /**
     * Set locale
     * @param locale
     * @return
     */
    public AbstractPlotter setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    /**
     * Set color for following drawings
     * @param color
     * @return
     */
    public AbstractPlotter setColor(Color color)
    {
        this.color = color;
        return this;
    }

    /**
     * Set font
     * @param fontName
     * @param fontStyle
     * @param fontSize
     * @return
     * @see java.awt.Font
     */
    public AbstractPlotter setFont(String fontName, int fontStyle, double fontSize)
    {
        Font f = new Font(fontName, fontStyle, fontStyle);
        return setFont(f.deriveFont((float)fontSize));
    }
    public AbstractPlotter setFont(String fontName, int fontStyle, int fontSize)
    {
        return setFont(new Font(fontName, fontStyle, fontStyle));
    }

    public AbstractPlotter setFont(Font font)
    {
        this.font = font;
        return this;
    }

    public AbstractPlotter setLineWidth(double width)
    {
        this.lineWidth = width;
        return this;
    }
    public void drawText(double x, double y, String text)
    {
        drawText(x, y, TextAlignment.START_X, text);
    }

    public void drawText(double x, double y, TextAlignment alignment, String text)
    {
        shapes.add(new Drawable(color, lineWidth, text2Shape(x, y, alignment, text)));
    }

    public Shape text2Shape(double x, double y, TextAlignment alignment, String text)
    {
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, text);
        return glyphVector.getOutline((float)x, (float)y);
    }
    public void drawCircle(Circle circle)
    {
        drawCircle(circle.getX(), circle.getY(), circle.getRadius());
    }

    public void drawCircle(double x, double y, double r)
    {
        shapes.add(new Drawable(color, lineWidth, new Ellipse2D.Double(x, y, r, r)));
    }

    public void drawPoint(DenseMatrix64F point)
    {
        assert point.numCols == 1;
        assert point.numRows == 2;
        double[] d = point.data;
        double x = d[0];
        double y = d[1];
        drawPoint(x, y);
    }

    public void drawPoint(Point p)
    {
        drawPoint(p.getX(), p.getY());
    }

    public void drawPoint(double x, double y)
    {
        drawLine(x, y, x, y);
    }

    public void drawPolygon(Polygon polygon)
    {
        shapes.add(new Drawable(color, lineWidth, new DoublePolygon(polygon)));
    }

    public void drawPolygon(DenseMatrix64F polygon)
    {
        shapes.add(new Drawable(color, lineWidth, new DoublePolygon(polygon)));
    }

    public void drawPoints(double[] arr)
    {
        for (int ii = 0; ii < arr.length; ii++)
        {
            drawPoint(ii, arr[ii]);
        }
    }

    public void lineTo(double x, double y)
    {
        if (Double.isNaN(lastX))
        {
            moveTo(x, y);
        }
        else
        {
            drawLine(lastX, lastY, x, y);
            moveTo(x, y);
        }
    }

    public void moveTo(double x, double y)
    {
        lastX = x;
        lastY = y;
    }

    public void drawLine(Point p1, Point p2)
    {
        drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void drawLine(double x1, double y1, double x2, double y2)
    {
        shapes.add(new Drawable(color, lineWidth, new Line2D.Double(x1, y1, x2, y2)));
    }

    public void drawPolyline(Polyline polyline)
    {
        shapes.add(polyline);
    }

    public void drawCoordinateLine(double x1, double y1, double x2, double y2)
    {
        shapes.add(0, new Drawable(color, lineWidth, new Line2D.Double(x1, y1, x2, y2)));
    }

    public void drawLines(double[] data)
    {
        shapes.add(new Drawable(color, lineWidth, new DoublePolygon(data)));
    }

    public void drawLines(Polygon polygon)
    {
        drawLines(polygon.points);
    }

    public void drawLines(DenseMatrix64F polygon)
    {
        shapes.add(new Drawable(color, lineWidth, new DoublePolygon(polygon)));
    }

    public void drawCoordinates()
    {
        drawCoordinateX();
        drawCoordinateY();
    }

    public void drawCoordinateX()
    {
    }

    public void drawCoordinateY()
    {
    }

    public void drawCoordinates0()
    {
    }

    @Override
    public void setScreen(double width, double height)
    {
        throw new UnsupportedOperationException("Screen coordinates must be set in constructor");
    }

    /**
     * Creates a Polyline. Use drawPolyline to add it to plotter.
     * @param color
     * @return
     */
    public Polyline polyline(Color color)
    {
        return polyline(color, 1.0);
    }
    public Polyline polyline(Color color, double lineWidth)
    {
        return new Polyline(color, lineWidth);
    }
 
    private static class DrawContext
    {
        protected Color color;
        protected Font font;
        protected double lineWidth;

        public DrawContext(Color color, Font font, double lineWidth)
        {
            this.color = color;
            this.font = font;
            this.lineWidth = lineWidth;
        }
        
    }
    private static class Drawable<S extends Shape> extends DrawContext
    {
        protected S shape;

        public Drawable(Color color, double lineWidth, S shape)
        {
            this(color, null, lineWidth, shape);
        }
        public Drawable(Color color, Font font, double lineWidth, S shape)
        {
            super(color, font, lineWidth);
            this.shape = shape;
        }

        public void draw(Drawer drawer)
        {
            drawer.setColor(color);
            drawer.setFont(font);
            drawer.setLineWidth(lineWidth);
            drawer.draw(shape);
        }

        public S getShape()
        {
            return shape;
        }
        
    }
    public static class Polyline extends Drawable<DoublePolygon>
    {

        public Polyline(Color color, double lineWidth)
        {
            super(color, lineWidth, new DoublePolygon());
        }
        public void lineTo(Point p)
        {
            lineTo(p.getX(), p.getY());
        }
        public void lineTo(double x, double y)
        {
            shape.add(x, y);
        }

        public void lineTo(Stream<Point> stream)
        {
            stream.forEach(this::lineTo);
        }
        
        public Rect getBounds()
        {
            Rectangle2D b = shape.getBounds2D();
            return new Rect(b.getMinX(), b.getMaxX(), b.getMinY(), b.getMaxY());
        }
        
    }
    
}
