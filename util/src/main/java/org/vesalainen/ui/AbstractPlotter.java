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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.stream.Stream;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.MathFunction;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.scale.MergeScale;
import org.vesalainen.ui.scale.Scale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractPlotter extends AbstractView implements DrawContext
{
    protected FontRenderContext fontRenderContext = new FontRenderContext(null, false, true);
    protected final List<Drawable> backgroundShapes = new ArrayList<>();
    protected final List<Drawable> fixedShapes = new ArrayList<>();
    protected final List<Drawable> shapes = new ArrayList<>();
    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    protected Locale locale = Locale.getDefault();
    protected Color color = Color.BLACK;
    protected Font font = new Font("ariel", 0, 12);
    protected final Color background;
    protected BasicStroke stroke = new BasicStroke();
    protected IntBinaryOperator pattern;
    protected Paint paint;
    protected List<BackgroundGenerator> backgroundGenerators = new ArrayList<>();
    private final Scale xScale;
    private final Scale yScale;
    private Color traceColor;

    public AbstractPlotter(int width, int height, Color background)
    {
        this(width, height, background, true);
    }

    public AbstractPlotter(int width, int height, Color background, boolean keepAspectRatio)
    {
        this(width, height, background, keepAspectRatio, MergeScale.BASIC15, MergeScale.BASIC15);
    }
    public AbstractPlotter(int width, int height, Color background, boolean keepAspectRatio, Scale xScale, Scale yScale)
    {
        this(width, height, background, keepAspectRatio, xScale, yScale, DoubleTransform.composite(xScale.function(), yScale.function()));
    }
    public AbstractPlotter(int width, int height, Color background, boolean keepAspectRatio, Scale xScale, Scale yScale, DoubleTransform transform)
    {
        super(width, height, keepAspectRatio, transform);
        this.background = background;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public Color getColor()
    {
        return color;
    }

    @Override
    public Font getFont()
    {
        return font;
    }

    public Color getBackground()
    {
        return background;
    }

    @Override
    public BasicStroke getStroke()
    {
        return stroke;
    }

    @Override
    public IntBinaryOperator getPattern()
    {
        return pattern;
    }

    @Override
    public Paint getPaint()
    {
        return paint;
    }

    public void plot(String path) throws IOException
    {
        plot(Paths.get(path));
    }
    public void plot(Path path) throws IOException
    {

        ImageDrawer drawer = new ImageDrawer(screenBounds.width, screenBounds.height, background);
        plot(drawer);
        drawer.write(path);
    }
    public void plot(Graphics2D g) throws IOException
    {
        ImageDrawer drawer = new ImageDrawer(screenBounds.width, screenBounds.height, background);
        plot(drawer);
        drawer.write(g);
    }
    protected void plot(Drawer drawer)
    {
        update(shapes.stream().map(Drawable::getBounds));
        calculate();
        trace();
        DoubleBounds origUserBounds = new DoubleBounds();
        origUserBounds.setRect(userBounds);
        backgroundGenerators.forEach((c)->
        {
            c.ensureSpace();
            calculate();
            trace();
        });
        drawer.setTransform(combinedTransform, scale);
        backgroundGenerators.forEach((c)->c.generate());
        backgroundShapes.forEach((d) ->d.draw(drawer));
        drawer.setTransform(DoubleTransform.identity(), 1.0);
        fixedShapes.forEach((d) ->d.draw(drawer));
        drawer.setTransform(combinedTransform, scale);
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
    @Override
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
    @Override
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

    @Override
    public AbstractPlotter setFont(Font font)
    {
        Objects.requireNonNull(font);
        this.font = font;
        return this;
    }

    @Override
    public AbstractPlotter setPaint(Paint paint)
    {
        this.paint = paint;
        return this;
    }

    @Override
    public AbstractPlotter setStroke(BasicStroke stroke)
    {
        Objects.requireNonNull(stroke);
        this.stroke = stroke;
        return this;
    }

    @Override
    public AbstractPlotter setPattern(IntBinaryOperator pattern)
    {
        this.pattern = pattern;
        return this;
    }

    public Scale getxScale()
    {
        return xScale;
    }

    public Scale getyScale()
    {
        return yScale;
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
        drawText(x, y, text, alignment);
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
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, text);
        Shape outline = glyphVector.getOutline();
        drawScreen(x, y, outline, alignments);
    }
    public void drawScreen(double x, double y, Shape shape, TextAlignment... alignments)
    {
        fixedShapes.add(new Fixed(x, y, true, shape, alignments));
    }

    public Shape text2Shape(double x, double y, String text, TextAlignment... alignments)
    {
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, text);
        Shape shape = glyphVector.getOutline();
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        Shape as = alignShape(x, y, margin, shape, at, alignments);
        return as;
    }
    public static Shape alignShape(double x, double y, int margin, Shape shape, TextAlignment... alignments)
    {
        return alignShape(x, y, margin, shape, null, alignments);
    }
    public static Shape alignShape(double x, double y, int margin, Shape shape, AffineTransform at, TextAlignment... alignments)
    {
        Rectangle2D b = shape.getBounds2D();
        AffineTransform t = AffineTransform.getTranslateInstance(x, y);
        for (TextAlignment alignment : alignments)
        {
            switch (alignment)
            {
                case START_X:
                    t.translate(margin, 0);
                    break;
                case START_Y:
                    t.translate(0, margin);
                    break;
                case MIDDLE_X:
                    t.translate(-b.getWidth()/2, 0);
                    break;
                case END_X:
                    t.translate(-b.getWidth()-margin, 0);
                    break;
                case MIDDLE_Y:
                    t.translate(0, -b.getHeight()/2);
                    break;
                case END_Y:
                    t.translate(0, -b.getHeight()-margin);
                    break;
                default:
                    throw new UnsupportedOperationException(alignment+" not supported");
            }
        }
        if (at != null)
        {
            t.translate(b.getWidth()/2.0, b.getHeight()/2.0);
            t.concatenate(at);
            t.translate(-b.getWidth()/2.0, -b.getHeight()/2.0);
        }
        t.translate(-b.getX(), -b.getY());
        return t.createTransformedShape(shape);
    }
    public void drawCircle(Circle circle)
    {
        drawCircle(circle.getX(), circle.getY(), circle.getRadius());
    }

    public void drawCircle(double x, double y, double r)
    {
        shapes.add(new Drawable(new Ellipse2D.Double(x-r, y-r, r*2, r*2)));
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
        drawMark(x, y, new Ellipse2D.Double(x, y, 1, 1));
    }

    public void drawPlus(double x, double y)
    {
        drawMark(x, y, new PlusShape());
    }

    public void drawCross(Point2D p)
    {
        drawMark(p.getX(), p.getY(), 45, new PlusShape());
    }

    public void drawCross(double x, double y)
    {
        drawMark(x, y, 45, new PlusShape());
    }

    public void drawMark(double x, double y, Shape mark)
    {
        drawMark(x, y, 0, mark);
    }
    public void drawMark(double x, double y, double deg, Shape mark)
    {
        Rectangle2D bounds = mark.getBounds2D();
        AffineTransform at = new AffineTransform();
        if (deg != 0)
        {
            at.rotate(Math.toRadians(deg), x, y);
        }
        if (x != bounds.getCenterX() || y != bounds.getCenterY())
        {
            at.translate(x-bounds.getCenterX(), y-bounds.getCenterY());
            mark = at.createTransformedShape(mark);
        }
        shapes.add(new DrawableMark(mark));
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
    public void draw(Shape shape)
    {
        shapes.add(new Drawable(shape));
    }
    public void draw(MathFunction func, Rectangle2D bounds)
    {
        shapes.add(new DrawableFunction(this, func, bounds));
    }
    public void draw(MathFunction func, double minX, double minY, double maxX, double maxY)
    {
        draw(func, new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY));
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
     * Draws backgroundGenerators using given LinearScaler
     * @param directions 
     */
    public void drawCoordinates(Direction... directions)
    {
        drawCoordinates(MergeScale.BASIC15, directions);
    }
    /**
     * Draws backgroundGenerators using given scaler
     * @param scale
     * @param directions 
     */
    public void drawCoordinates(Scale scale, Direction... directions)
    {
        drawCoordinates(new BasicCoordinates(this), scale, directions);
    }
    public void drawCoordinates(CoordinatesGenerator generator, Scale scale, Direction... directions)
    {
        for (Direction direction : directions)
        {
            generator.addCoordinate(direction, scale);
        }
        drawBackground(generator);
    }
    public void drawTitle(Direction direction, String title)
    {
        drawBackground(new BasicTitle(this, direction, title));
    }
    protected void drawBackground(BackgroundGenerator generator)
    {
        backgroundGenerators.add(generator);
    }
    /**
     * Draws backgroundGenerators using LinearScaler to LEFT and BOTTOM
     */
    public void drawCoordinates()
    {
        drawCoordinateX();
        drawCoordinateY();
    }
    /**
     * Draws backgroundGenerators using LinearScaler to BOTTOM
     */
    public void drawCoordinateX()
    {
        drawCoordinates(BOTTOM);
    }
    /**
     * Draws backgroundGenerators using LinearScaler to LEFT
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
    public void drawCoordinateLine(double x1, double y1, double x2, double y2)
    {
        backgroundShapes.add( new Drawable(new Line2D.Double(x1, y1, x2, y2)));
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

    private void trace()
    {
        if (traceColor != null)
        {
            Color safe = color;
            color = traceColor;
            draw((Shape)userBounds.clone());
            color = safe;
        }
    }

    public void setTraceColor(Color traceColor)
    {
        this.traceColor = traceColor;
    }
 
    public class Drawable<S extends Shape> extends SimpleDrawContext
    {
        protected boolean fill;
        protected S shape;

        public Drawable(S shape)
        {
            super(AbstractPlotter.this);
            this.shape = shape;
        }

        public Drawable(boolean fill, S shape)
        {
            super(AbstractPlotter.this);
            this.shape = shape;
            this.fill = fill;
        }
        protected void setContext(Drawer drawer)
        {
            drawer.setColor(color);
            drawer.setFont(font);
            drawer.setStroke(stroke);
            drawer.setPattern(pattern);
            drawer.setPaint(paint);
        }
        public void draw(Drawer drawer)
        {
            setContext(drawer);
            if (fill)
            {
                drawer.fill(shape);
            }
            else
            {
                drawer.draw(shape);
            }
        }

        public Rectangle2D getBounds()
        {
            return shape.getBounds2D();
        }

        public void transform(AffineTransform at)
        {
            shape = (S) at.createTransformedShape(shape);
        }
    }
    private class Fixed extends Drawable
    {
        private double x;
        private double y;
        private TextAlignment[] alignments;

        public Fixed(double x, double y, Shape shape, TextAlignment... alignments)
        {
            super(shape);
            this.x = x;
            this.y = y;
            this.alignments = alignments;
        }

        public Fixed(double x, double y, boolean fill, Shape shape, TextAlignment... alignments)
        {
            super(fill, shape);
            this.x = x;
            this.y = y;
            this.alignments = alignments;
        }

        @Override
        public void draw(Drawer drawer)
        {
            Point2D pnt = new Point2D.Double();
            combinedTransform.transform(x, y, pnt::setLocation);
            Shape safe = shape;
            shape = alignShape(pnt.getX(), pnt.getY(), margin, safe, alignments);
            Rectangle2D b = shape.getBounds2D();
            super.draw(drawer);
            shape = safe;
        }
        
    }
    public class DrawableMark extends Drawable
    {
        
        public DrawableMark(Shape shape)
        {
            super(shape);
        }

        @Override
        public void draw(Drawer drawer)
        {
            setContext(drawer);
            drawer.drawMark(shape);
        }
        
    }
    public class DrawableFunction extends Drawable<MathFunctionShape>
    {
        
        public DrawableFunction(DrawContext ctx, MathFunction f, Rectangle2D bounds)
        {
            super(new MathFunctionShape(f, bounds, screenBounds.width));
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
    }
    
}
