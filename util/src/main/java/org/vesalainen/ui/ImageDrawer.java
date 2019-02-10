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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import java.awt.image.BufferedImage;
import org.vesalainen.math.BezierCurve;
import static org.vesalainen.math.BezierCurve.LINE;
import org.vesalainen.util.function.DoubleBiConsumer;
import org.vesalainen.math.BezierOperator;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageDrawer extends AbstractDrawer
{
    private BufferedImage image;
    private Rectangle bounds;
    private DoubleTransformer transformer;
    private DoubleBiConsumer pixelDrawer;

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
    }

    @Override
    public void draw(Shape shape)
    {
        double[] orig = new double[6];
        double[] array = new double[6];
        double fox;
        double foy;
        double ftx;
        double fty;
        double lox = 0;
        double loy = 0;
        double ltx = 0;
        double lty = 0;
        double delta = 0;
        transformer = getTransform();
        pixelDrawer = (x,y)->transformer.transform(x, y, this::drawPoint);
        PathIterator pi = shape.getPathIterator(null);
        while (!pi.isDone())
        {
            switch (pi.currentSegment(orig))
            {
                case SEG_MOVETO:
                    fox = lox = orig[0];
                    foy = loy = orig[1];
                    transformer.transform(tmp, orig, array, 1);
                    ftx = ltx = array[0];
                    fty = lty = array[1];
                    break;
                case SEG_LINETO:
                    lox = orig[0];
                    loy = orig[1];
                    transformer.transform(tmp, orig, array, 1);
                    delta = delta(LINE, ltx, lty, array[0], array[1]);
                    draw(LINE, delta, lox, loy, orig[0], orig[1]);
                    ltx = array[0];
                    lty = array[1];
                    break;
            }
            pi.next();
        }
    }
    private double delta(BezierCurve bc, double... transformed)
    {
        double len = bc.pathLengthEstimate(transformed);
        return 1.0/len;
    }
    private void draw(BezierCurve bc, double delta, double... orig)
    {
        BezierOperator op = bc.operator(orig);
        op.eval(0, pixelDrawer);
        for (double t=delta;t<1.0;t+=delta)
        {
            op.eval(t, pixelDrawer);
        }
        op.eval(1, pixelDrawer);
    }
    
    public void drawPoint(double x, double y)
    {
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
    }

    public BufferedImage getImage()
    {
        return image;
    }
    
}
