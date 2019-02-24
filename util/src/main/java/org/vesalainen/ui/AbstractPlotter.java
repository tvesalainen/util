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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.stream.Stream;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Rect;
import static org.vesalainen.ui.Direction.*;

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
    private final List<Drawable> backgroundShapes = new ArrayList<>();
    private final List<Drawable> fixedShapes = new ArrayList<>();
    private final List<Drawable> shapes = new ArrayList<>();
    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    protected final Color background;
    protected BasicStroke stroke = new BasicStroke();
    protected IntBinaryOperator pattern;
    protected Paint paint;
    protected Map<Direction,AbstractScaler> scalerMap = new EnumMap(Direction.class);
    protected Map<Direction,Double> scalerLevelMap = new EnumMap(Direction.class);

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
        calculate();
        Rectangle2D origUserBounds = new Rectangle2D.Double();
        origUserBounds.setRect(userBounds);
        Rectangle2D bounds = new Rectangle2D.Double();
        if (!scalerMap.isEmpty())
        {
            scalerMap.forEach((dir, scaler)->
            {
                switch (dir)
                {
                    case TOP:
                        scaler.set(userBounds.getMinX(), userBounds.getMaxX());
                        scalerLevelMap.put(dir, scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, userBounds.getMaxY(), bounds));
                        setMargin(bounds, dir);
                        break;
                    case BOTTOM:
                        scaler.set(userBounds.getMinX(), userBounds.getMaxX());
                        scalerLevelMap.put(dir, scaler.getLevelFor(font, fontRenderContext, combinedTransform, true, userBounds.getMinY(), bounds));
                        setMargin(bounds, dir);
                        break;
                    case LEFT:
                        scaler.set(userBounds.getMinY(), userBounds.getMaxY());
                        scalerLevelMap.put(dir, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, userBounds.getMinX(), bounds));
                        setMargin(bounds, dir);
                        break;
                    case RIGHT:
                        scaler.set(userBounds.getMinY(), userBounds.getMaxY());
                        scalerLevelMap.put(dir, scaler.getLevelFor(font, fontRenderContext, combinedTransform, false, userBounds.getMaxX(), bounds));
                        setMargin(bounds, dir);
                        break;
                }
            });
            calculate();
        }
        drawer.setTransform(combinedTransform, inverse, derivates, scale);
        scalerMap.forEach((dir, scaler)->
        {
            switch (dir)
            {
                case TOP:
                    scaler.forEach(locale, scalerLevelMap.get(dir), (value,label)->
                    {
                        backgroundShapes.add(new Drawable(new Line2D.Double(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY())));
                        drawScreenText(value, userBounds.getMaxY(), label, TextAlignment.MIDDLE_X, TextAlignment.END_Y);
                    });
                    break;
                case BOTTOM:
                    scaler.forEach(locale, scalerLevelMap.get(dir), (value,label)->
                    {
                        backgroundShapes.add(new Drawable(new Line2D.Double(value, origUserBounds.getMinY(), value, origUserBounds.getMaxY())));
                        drawScreenText(value, userBounds.getMinY(), label, TextAlignment.MIDDLE_X);
                    });
                    break;
                case LEFT:
                    scaler.forEach(locale, scalerLevelMap.get(dir), (value,label)->
                    {
                        backgroundShapes.add(new Drawable(new Line2D.Double(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value)));
                        drawScreenText(userBounds.getMinX(), value, label, TextAlignment.START_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
                case RIGHT:
                    scaler.forEach(locale, scalerLevelMap.get(dir), (value,label)->
                    {
                        backgroundShapes.add(new Drawable(new Line2D.Double(origUserBounds.getMinX(), value, origUserBounds.getMaxX(), value)));
                        drawScreenText(userBounds.getMaxX(), value, label, TextAlignment.END_X, TextAlignment.MIDDLE_Y);
                    });
                    break;
            }
        });
        backgroundShapes.forEach((d) ->d.draw(drawer));
        drawer.setTransform(DoubleTransform.identity(), DoubleTransform.identity(), new DoubleTransform[]{}, 1.0);
        fixedShapes.forEach((d) ->d.draw(drawer));
        drawer.setTransform(combinedTransform, inverse, derivates, scale);
        shapes.forEach((d) ->d.draw(drawer));
    }
    /**
     * Clears the screen
     */
    public void clear()
    {
        backgroundShapes.clear();
        fixedShapes.clear();
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

    public AbstractPlotter setHorizontalScaler(AbstractScaler horizontalScaler)
    {
        this.scalerMap.put(BOTTOM, horizontalScaler);
        return this;
    }

    public AbstractPlotter setVerticalScaler(AbstractScaler verticalScaler)
    {
        this.scalerMap.put(LEFT, verticalScaler);
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
        return setFont(new Font(fontName, fontStyle, fontSize));
    }

    public AbstractPlotter setFont(Font font)
    {
        Objects.requireNonNull(font);
        this.font = font;
        return this;
    }

    public AbstractPlotter setPaint(Paint paint)
    {
        this.paint = paint;
        return this;
    }

    public AbstractPlotter setStroke(BasicStroke stroke)
    {
        Objects.requireNonNull(stroke);
        this.stroke = stroke;
        return this;
    }

    public AbstractPlotter setPattern(IntBinaryOperator pattern)
    {
        this.pattern = pattern;
        return this;
    }
    
    public void drawText(double x, double y, String text)
    {
        drawText(x, y, text, TextAlignment.START_X);
    }
    /**
     * @deprecated Only one x/y alignment possible
     * @param x
     * @param y
     * @param alignment
     * @param text 
     */
    public void drawText(double x, double y, TextAlignment alignment, String text)
    {
        drawText(x, y, alignment, text);
    }
    public void drawText(double x, double y, String text, TextAlignment... alignments)
    {
        shapes.add(new Drawable(true, text2Shape(x, y, text, alignments)));
    }

    public void drawScreenText(double x, double y, String text)
    {
        drawScreenText(x, y, text, TextAlignment.START_X);
    }
    /**
     * @deprecated Only x/y alignment is possible
     * @param x
     * @param y
     * @param alignment
     * @param text 
     */
    public void drawScreenText(double x, double y, TextAlignment alignment, String text)
    {
        drawScreenText(x, y, text, alignment);
    }
    public void drawScreenText(double x, double y, String text, TextAlignment... alignments)
    {
        fixedShapes.add(new Fixed(x, y, true, text2Shape(0, 0, text, alignments)));
    }

    public Shape text2Shape(double x, double y, String text, TextAlignment... alignments)
    {
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, text);
        Rectangle2D lb = glyphVector.getLogicalBounds();
        Shape outline = glyphVector.getOutline();
        LineMetrics lm = font.getLineMetrics(text, fontRenderContext);
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        y -= lm.getDescent();
        for (TextAlignment alignment : alignments)
        {
            switch (alignment)
            {
                case START_X:
                case START_Y:
                    break;
                case MIDDLE_X:
                    x -= lb.getWidth()/2;
                    break;
                case END_X:
                    x -= lb.getWidth();
                    break;
                case MIDDLE_Y:
                    y += lm.getHeight()/2;
                    break;
                case END_Y:
                    y += lm.getHeight();
                    break;
                default:
                    throw new UnsupportedOperationException(alignments+" not supported");
            }
        }
        at.translate(x, y);
        return new Path2D.Double(outline, at);
    }
    public void drawCircle(Circle circle)
    {
        drawCircle(circle.getX(), circle.getY(), circle.getRadius());
    }

    public void drawCircle(double x, double y, double r)
    {
        shapes.add(new Drawable(new Ellipse2D.Double(x+r, y+r, r/2, r/2)));
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
        shapes.add(new Drawable(new DoublePolygon(polygon)));
    }

    public void drawPolygon(DenseMatrix64F polygon)
    {
        shapes.add(new Drawable(new DoublePolygon(polygon)));
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
        shapes.add(new Drawable(new Line2D.Double(x1, y1, x2, y2)));
    }

    public void drawPolyline(Polyline polyline)
    {
        shapes.add(polyline);
    }

    public void drawCoordinateLine(double x1, double y1, double x2, double y2)
    {
        shapes.add(0, new Drawable(new Line2D.Double(x1, y1, x2, y2)));
    }

    public void drawLines(double[] data)
    {
        shapes.add(new Drawable(new DoublePolygon(data)));
    }

    public void drawLines(Polygon polygon)
    {
        drawLines(polygon.points);
    }

    public void drawLines(DenseMatrix64F polygon)
    {
        shapes.add(new Drawable(new DoublePolygon(polygon)));
    }
    /**
     * Draws coordinates using given LinearScaler
     * @param directions 
     */
    public void drawCoordinates(Direction... directions)
    {
        drawCoordinates(new LinearScaler(), directions);
    }
    /**
     * Draws coordinates using given scaler
     * @param directions
     * @param scaler 
     */
    public void drawCoordinates(AbstractScaler scaler, Direction... directions)
    {
        for (Direction direction : directions)
        {
            scalerMap.put(direction, scaler);
        }
    }
    /**
     * Draws coordinates using LinearScaler to LEFT and BOTTOM
     */
    public void drawCoordinates()
    {
        drawCoordinateX();
        drawCoordinateY();
    }
    /**
     * Draws coordinates using LinearScaler to BOTTOM
     */
    public void drawCoordinateX()
    {
        drawCoordinates(BOTTOM);
    }
    /**
     * Draws coordinates using LinearScaler to LEFT
     */
    public void drawCoordinateY()
    {
        drawCoordinates(LEFT);
    }

    public void drawCoordinates0()
    {
    }
    public void drawScreen(double x, double y, Shape shape)
    {
        drawScreen(x, y, false, shape);
    }
    public void drawScreen(double x, double y, boolean fill, Shape shape)
    {
        fixedShapes.add(new Fixed(x, y, fill, shape));
    }
    public void drawHorizontalLine(double x)
    {
        backgroundShapes.add(new HorizontalLine(x));
    }
    public void drawVerticalLine(double x)
    {
        backgroundShapes.add(new VerticalLine(x));
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
    @Deprecated public Polyline polyline(Color color, double lineWidth)
    {
        return polyline(color, new BasicStroke((float)lineWidth));
    }
    public Polyline polyline(Color color, BasicStroke stroke)
    {
        return new Polyline(color, stroke);
    }
 
    private class DrawContext
    {
        protected Color color;
        protected Font font;
        protected BasicStroke stroke;
        protected Paint paint;
        protected IntBinaryOperator pattern;

        public DrawContext()
        {
            this.color = AbstractPlotter.this.color;
            this.font = AbstractPlotter.this.font;
            this.stroke = AbstractPlotter.this.stroke;
            this.paint = AbstractPlotter.this.paint;
            this.pattern = AbstractPlotter.this.pattern;
        }

    }
    private class Drawable<S extends Shape> extends DrawContext
    {
        protected boolean fill;
        protected S shape;

        public Drawable(S shape)
        {
            this.shape = shape;
        }

        public Drawable(boolean fill, S shape)
        {
            this.shape = shape;
            this.fill = fill;
        }

        public void draw(Drawer drawer)
        {
            drawer.setColor(color);
            drawer.setFont(font);
            drawer.setStroke(stroke);
            drawer.setPattern(pattern);
            drawer.setPaint(paint);
            if (fill)
            {
                drawer.fill(shape);
            }
            else
            {
                drawer.draw(shape);
            }
        }

        public S getShape()
        {
            return shape;
        }
        
    }
    public class Fixed extends Drawable
    {
        private double x;
        private double y;

        public Fixed(double x, double y, Shape shape)
        {
            super(shape);
            this.x = x;
            this.y = y;
        }

        public Fixed(double x, double y, boolean fill, Shape shape)
        {
            super(fill, shape);
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Drawer drawer)
        {
            AffineTransform at = AffineTransform.getScaleInstance(1, -1);
            combinedTransform.transform(x, y, (xx,yy)->
            {
                at.translate(xx-x, -(yy-y));
            });
            Shape safe = shape;
            shape = new Path2D.Double(safe, at);
            super.draw(drawer);
            shape = safe;
        }
        
    }
    public class HorizontalLine extends Drawable<Line2D>
    {
        private double y;
        
        public HorizontalLine(double y)
        {
            super(new Line2D.Double());
            this.y = y;
        }

        @Override
        public void draw(Drawer drawer)
        {
            shape.setLine(transformedUserBounds.getMinX(), y, transformedUserBounds.getMaxX(), y);
            super.draw(drawer);
        }
        
    }
    public class VerticalLine extends Drawable<Line2D>
    {
        private double x;
        
        public VerticalLine(double x)
        {
            super(new Line2D.Double());
            this.x = x;
        }

        @Override
        public void draw(Drawer drawer)
        {
            shape.setLine(x, transformedUserBounds.getMinY(), x, transformedUserBounds.getMaxY());
            super.draw(drawer);
        }
        
    }
    public class Polyline extends Drawable<DoublePolygon>
    {

        public Polyline(Color color, BasicStroke stroke)
        {
            super(new DoublePolygon());
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