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
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.IntBinaryOperator;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;

/**
 * <p>This class is NOT thread-safe!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractDrawer implements Drawer
{
    protected Font font;
    protected Color color;
    protected Paint paint;
    protected IntBinaryOperator pattern;
    protected BasicStroke stroke;
    protected DoubleTransform transform;
    protected DoubleTransform inverse;
    protected Point2D.Double tmp = new Point2D.Double();
    protected DoubleBinaryMatrix gradient;
    protected IntBounds fillBounds = new IntBounds();
    protected double scale;
    protected double delta;
    private double deltax;
    private double deltay;
    protected Shape fillShape;
    
    @Override
    public void setFont(Font font)
    {
        this.font = font;
    }

    @Override
    public void text(double x, double y, TextAlignment alignment, String text)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setColor(Color color)
    {
        this.color = color;
    }

    @Override
    public Color getColor()
    {
        return color;
    }

    @Override
    public void setPaint(Paint paint)
    {
        this.paint = paint;
    }

    @Override
    public void setPattern(IntBinaryOperator pattern)
    {
        this.pattern = pattern;
    }

    @Override
    public void setStroke(BasicStroke stroke)
    {
        this.stroke = stroke;
    }

    @Override
    public void setTransform(DoubleTransform transform, double scale)
    {
        this.transform = transform;
        this.gradient = transform.gradient();
        this.inverse = transform.inverse();
        this.scale = scale;
    }

    @Override
    public void drawMark(Shape mark)
    {
        Rectangle2D b = mark.getBounds2D();
        Point2D p1 = new Point2D.Double();
        Point2D p2 = new Point2D.Double();
        transform.transform(b.getCenterX(), b.getCenterY(), p1::setLocation);
        transform.transform(b.getCenterX()+b.getWidth(), b.getCenterY()+b.getHeight(), p2::setLocation);
        double distance = p1.distance(p2)/1.4;
        float lineWidth = stroke.getLineWidth();
        double s = lineWidth/distance;
        fill(Shapes.scaleInPlace(mark, s, s));
    }

    protected void updateDelta(double dx, double dy)
    {
        if (dx < 0)
        {
            System.err.println("DELTA "+dx+", "+dy);
        }
        delta = Math.hypot(dx, dy);
    }

    public void draw(Shape shape)
    {
        if (stroke != null && stroke.getLineWidth() > 1)
        {
            BasicStroke s = new BasicStroke((float) (stroke.getLineWidth() / scale), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
            fillShape = s.createStrokedShape(shape);
            fill(fillShape);
        }
        else
        {
            drawIt(shape);
        }
    }

    public void drawIt(Shape shape)
    {
        double[] arr = new double[6];
        double fx = 0;
        double fy = 0;
        double lx = 0;
        double ly = 0;
        PathIterator pi = shape.getPathIterator(null);
        beginPath();
        while (!pi.isDone())
        {
            switch (pi.currentSegment(arr))
            {
                case PathIterator.SEG_MOVETO:
                    moveTo(arr[0], arr[1]);
                    //System.err.printf("MOVETO %.1f %.1f\n", arr[0], arr[1]);
                    fx = lx = arr[0];
                    fy = ly = arr[1];
                    break;
                case PathIterator.SEG_LINETO:
                    drawLine(lx, ly, arr[0], arr[1]);
                    //System.err.printf("LINETO %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1]);
                    lx = arr[0];
                    ly = arr[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    drawQuad(lx, ly, arr[0], arr[1], arr[2], arr[3]);
                    //System.err.printf("QUADTO %.1f %.1f %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1], arr[2], arr[3]);
                    lx = arr[2];
                    ly = arr[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    drawCubic(lx, ly, arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                    //System.err.printf("CUBICTO %.1f %.1f %.1f %.1f %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                    lx = arr[4];
                    ly = arr[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    //System.err.printf("CLOSE\n");
                    closePath(lx, ly, fx, fy);
                    lx = -1;
                    ly = -1;
                    break;
            }
            pi.next();
        }
    }

    public void fill(Shape shape)
    {
        fillShape = shape;
        fillBounds.clear();
        drawIt(shape);
        fill();
    }

    protected abstract void fill();

}
