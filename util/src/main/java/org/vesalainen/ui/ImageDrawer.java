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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import org.vesalainen.math.BezierCurve;
import static org.vesalainen.math.BezierCurve.*;
import org.vesalainen.math.BezierOperator;
import org.vesalainen.util.concurrent.ThreadTemporal;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageDrawer extends AbstractDrawer
{
    private BufferedImage image;
    private Rectangle bounds;
    private ImageScanlineFiller filler;

    public ImageDrawer(int width, int height)
    {
        this(width, height, Color.WHITE);
    }
    public ImageDrawer(int width, int height, Color background)
    {
        this(new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR), background);
    }

    public ImageDrawer(BufferedImage image, Color background)
    {
        this.image = image;
        this.bounds = new Rectangle(image.getWidth(), image.getHeight());
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setBackground(background);
        graphics2D.clearRect(0, 0, image.getWidth(), image.getHeight());
        filler = new ImageScanlineFiller(image);
    }

    @Override
    public void setTransform(DoubleTransform t, AffineTransform at)
    {
        super.setTransform(t, at);
    }

    @Override
    public void draw(Shape shape)
    {
        double[] arr = new double[6];
        double fx = 0;
        double fy = 0;
        double lx = 0;
        double ly = 0;
        scaledLineWidth = (float) (lineWidth/scale);
        BasicStroke stroke = new BasicStroke(scaledLineWidth);
        Shape strokedShape = stroke.createStrokedShape(shape);
        PathIterator pi = strokedShape.getPathIterator(null);
        while (!pi.isDone())
        {
            switch (pi.currentSegment(arr))
            {
                case SEG_MOVETO:
                    fx = lx = arr[0];
                    fy = ly = arr[1];
                    break;
                case SEG_LINETO:
                    draw(LINE, lx, ly, arr[0], arr[1]);
                    lx = arr[0];
                    ly = arr[1];
                    break;
                case SEG_QUADTO:
                    draw(QUAD, lx, ly, arr[0], arr[1], arr[2], arr[3]);
                    lx = arr[0];
                    ly = arr[1];
                    break;
                case SEG_CUBICTO:
                    draw(CUBIC, lx, ly, arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                    lx = arr[0];
                    ly = arr[1];
                    break;
                case SEG_CLOSE:
                    draw(LINE, lx, ly, fx, fy);
                    lx = -1;
                    ly = -1;
                    break;
            }
            pi.next();
        }
    }
    private void draw(BezierCurve curve, double... cp)
    {
        BezierOperator op = curve.operator(cp);
        BezierOperator der = curve.derivate(cp);
        draw(op.andThen(transform), der);
    }
    private void draw(BezierOperator curve, BezierOperator curveDerivate)
    {
        double t = 0;
        curve.eval(0, this::drawPoint);
        curveDerivate.eval(t, this::updateDelta);
        t += 1.0/delta;
        while (t < 1)
        {
            curveDerivate.eval(t, this::updateDelta);
            curve.eval(t, this::drawPoint);
            t += 1.0/delta;
        }
        curve.eval(1, this::drawPoint);
    }
    
    public void drawPoint(double x, double y)
    {
        drawPixel((int)x, (int)y);
        /*
        int minX = (int) Math.floor(x-lineWidth);
        int maxX = (int) Math.ceil(x+lineWidth);
        int minY = (int) Math.floor(y-lineWidth);
        int maxY = (int) Math.ceil(y+lineWidth);
        for (int i=minX;i<maxX;i++)
        {
            for (int j=minY;j<maxY;j++)
            {
                if (Math.hypot(x-i, y-j) <= lineWidth)
                {
                    if (bounds.contains(i, j))
                    {
                        image.setRGB(i, j, color.getRGB());
                    }
                }
            }
        }
        */
    }

    public void drawWidth(double x, double y, double dx, double dy)
    {
        LineDrawer.fillWidth(x, y, dx, dy, lineWidth, this::drawPixel);
    }
    public void drawPixel(int x, int y)
    {
        if (bounds.contains(x, y))
        {
            image.setRGB(x, y, color.getRGB());
        }
    }
    public BufferedImage getImage()
    {
        return image;
    }
    
}
