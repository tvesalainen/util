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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import javax.imageio.ImageIO;
import org.vesalainen.math.BezierCurve;
import static org.vesalainen.math.BezierCurve.*;
import org.vesalainen.util.function.IntBiPredicate;
import org.vesalainen.math.ParameterizedOperator;
import org.vesalainen.math.matrix.DoubleBinaryMatrix;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageDrawer extends AbstractDrawer
{
    private BufferedImage image;
    private Rectangle bounds;
    private ImageAreaFiller filler;
    private Point2D fillPoint = new Point2D.Double();
    private Shape fillShape;
    private IntBiPredicate isInside;

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
        filler = new ImageAreaFiller(image);
    }

    @Override
    public void setTransform(DoubleTransform transform, double scale)
    {
        super.setTransform(transform, scale);
        isInside = (x,y)->
        {
            inverse.transform(x, y, (xx,yy)->fillPoint.setLocation(xx, yy));
            return fillShape.contains(fillPoint);
        };
    }

    @Override
    public void draw(Shape shape)
    {
        if (stroke != null && stroke.getLineWidth() > 1)
        {
            BasicStroke s = new BasicStroke(
                    (float) (stroke.getLineWidth()/scale), 
                    stroke.getEndCap(), 
                    stroke.getLineJoin(), 
                    stroke.getMiterLimit(), 
                    stroke.getDashArray(), 
                    stroke.getDashPhase()
            );
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
        while (!pi.isDone())
        {
            switch (pi.currentSegment(arr))
            {
                case SEG_MOVETO:
                    moveTo(arr[0], arr[1]);
                    System.err.printf("MOVETO %.1f %.1f\n", arr[0], arr[1]);
                    fx = lx = arr[0];
                    fy = ly = arr[1];
                    break;
                case SEG_LINETO:
                    drawLine(lx, ly, arr[0], arr[1]);
                    System.err.printf("LINETO %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1]);
                    lx = arr[0];
                    ly = arr[1];
                    break;
                case SEG_QUADTO:
                    drawQuad(lx, ly, arr[0], arr[1], arr[2], arr[3]);
                    System.err.printf("QUADTO %.1f %.1f %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1], arr[2], arr[3]);
                    lx = arr[2];
                    ly = arr[3];
                    break;
                case SEG_CUBICTO:
                    drawCubic(lx, ly, arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                    System.err.printf("CUBICTO %.1f %.1f %.1f %.1f %.1f %.1f %.1f %.1f\n", lx, ly, arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                    lx = arr[4];
                    ly = arr[5];
                    break;
                case SEG_CLOSE:
                    System.err.printf("CLOSE\n");
                    closePath(lx, ly, fx, fy);
                    lx = -1;
                    ly = -1;
                    break;
            }
            pi.next();
        }
    }

    @Override
    public void fill(Shape shape)
    {
        fillShape = shape;
        fillBounds.clear();
        drawIt(shape);
        fill();
    }

    @Override
    public void drawLine(double... cp)
    {
        draw(LINE, cp);
    }

    @Override
    public void drawQuad(double... cp)
    {
        draw(QUAD, cp);
    }

    @Override
    public void drawCubic(double... cp)
    {
        draw(CUBIC, cp);
    }

    @Override
    public void closePath(double... cp)
    {
        drawLine(cp);
    }

    private void draw(BezierCurve curve, double... cp)
    {
        ParameterizedOperator op = curve.operator(cp);
        draw(op);
    }
    private void draw(ParameterizedOperator op)
    {
        ParameterizedOperator curve = op.andThen(transform);
        double t = 0;
        DoubleUnaryOperator hypot = curve.hypot();
        curve.calc(t, this::drawPoint);
        t += 1.0/hypot.applyAsDouble(t);
        while (t < 1)
        {
            curve.calc(t, this::drawPoint);
            t += 1.0/hypot.applyAsDouble(t);
        }
        curve.calc(1, this::drawPoint);
    }
    
    public void fill()
    {
        if (!fillBounds.isEmpty())
        {
            if (paint != null)
            {
                PaintPattern paintPattern = new PaintPattern(image, paint);
                filler.fill(fillBounds, isInside, paintPattern.getPattern(fillBounds));
            }
            else
            {
                if (pattern != null)
                {
                    filler.fill(fillBounds, isInside, pattern);
                }
                else
                {
                    filler.fill(fillBounds, isInside, color.getRGB());
                }
            }
        }
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

    public void drawPixel(int x, int y)
    {
        if (bounds.contains(x, y))
        {
            image.setRGB(x, y, color.getRGB());
            fillBounds.add(x, y);
        }
    }
    public BufferedImage getImage()
    {
        return image;
    }

    @Override
    public <T> boolean supports(T target)
    {
        if (target instanceof Graphics2D)
        {
            return true;
        }
        if (target instanceof Path)
        {
            Path path = (Path) target;
            String fs = path.getFileName().toString();
            int idx = fs.lastIndexOf('.');
            if (idx == -1)
            {
                return false;
            }
            String fileSuffix = fs.substring(idx-1);
            for (String suffix : ImageIO.getReaderFileSuffixes())
            {
                if (fileSuffix.equalsIgnoreCase(suffix))
                {
                    return true;
                }
            }
        }
        return false;
    }

    
    @Override
    public <T> void write(T target)
    {
        if (target instanceof Graphics2D)
        {
            Graphics2D g = (Graphics2D) target;
            g.drawImage(image, null, null);
        }
        else
        {
            if (target instanceof Path)
            {
                Path path = (Path) target;
                String fs = path.getFileName().toString();
                int idx = fs.lastIndexOf('.');
                if (idx == -1)
                {
                    throw new IllegalArgumentException("no file suffix");
                }
                String fileFormat = fs.substring(idx+1);
                try (OutputStream os = Files.newOutputStream(path))
                {
                    ImageIO.write(image, fileFormat, os);
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
            else
            {
                throw new UnsupportedOperationException(target+" not supported");
            }
        }
    }
    
}
