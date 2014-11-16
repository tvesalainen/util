/*
 * Copyright (C) 2014 Timo Vesalainen
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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Timo Vesalainen
 */
public class Plotter extends AbstractView
{
    private final BufferedImage image;
    private final Graphics2D graphics2D;
    private Color color = Color.BLACK;
    private final List<Drawable> drawables = new ArrayList<>();

    public Plotter(int width, int height)
    {
        this(width, height, new Color(255, 255, 255, 255));
    }
    public Plotter(int width, int height, Color background)
    {
        super.setScreen(width, height);
        image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_4BYTE_ABGR);
        graphics2D = image.createGraphics();
        graphics2D.setBackground(background);
        graphics2D.clearRect(0, 0, (int)width, (int)height);
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public void drawCircle(double x, double y, double r)
    {
        update(x, y, r);
        drawables.add(new Circle(color, x, y, r));
    }
    
    public void drawPoint(double x, double y)
    {
        update(x, y);
        drawables.add(new Point(color, x, y));
    }
    
    @Override
    public void setScreen(double width, double height)
    {
        throw new UnsupportedOperationException("Screen coordinates must be set in constructor");
    }
    
    public void plot(File file) throws IOException
    {
        for (Drawable d : drawables)
        {
            d.draw(graphics2D);
        }
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            ImageIO.write(image, "png", fos);
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }
    private class Drawable
    {
        Color color;
        double x;
        double y;

        public Drawable(Color color, double x, double y)
        {
            this.color = color;
            this.x = x;
            this.y = y;
        }
        
        protected void draw(Graphics2D graphics2D)
        {
            graphics2D.setColor(color);
        }
    }
    private class Point extends Drawable
    {

        public Point(Color color, double x, double y)
        {
            super(color, x, y);
        }
        
        @Override
        protected void draw(Graphics2D graphics2D)
        {
            super.draw(graphics2D);
            int sx = (int) toScreenX(x);
            int sy = (int) toScreenY(y);
            graphics2D.drawOval(sx-2, sy-2, 4, 4);
        }
    }
    private class Circle extends Drawable
    {
        double r;

        public Circle(Color color, double x, double y, double r)
        {
            super(color, x, y);
            this.r = r;
        }
        
        @Override
        protected void draw(Graphics2D graphics2D)
        {
            super.draw(graphics2D);
            int sx = (int) toScreenX(x);
            int sy = (int) toScreenY(y);
            int sr = (int) scale(r);
            graphics2D.drawOval(sx-sr, sy-sr, sr, sr);
        }
    }
}
