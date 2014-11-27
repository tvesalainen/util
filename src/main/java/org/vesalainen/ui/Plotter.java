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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Polygon;

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
    private File dir;

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

    public void clear()
    {
        graphics2D.clearRect(0, 0, (int)width, (int)height);
        drawables.clear();
    }
    
    public void setDir(File dir)
    {
        this.dir = dir;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public void drawCircle(Circle circle)
    {
        drawCircle(circle.getX(), circle.getY(), circle.getRadius());
    }
    
    public void drawCircle(double x, double y, double r)
    {
        updateCircle(x, y, r);
        drawables.add(new Circl(color, x, y, r));
    }
    
    public void drawPoint(DenseMatrix64F point)
    {
        assert point.numCols == 1;
        assert point.numRows == 2;
        double[] d = point.data;
        double x = d[0];
        double y = d[1];
        updatePoint(x, y);
        drawables.add(new Point(color, x, y));
    }
    
    public void drawPoint(double x, double y)
    {
        updatePoint(x, y);
        drawables.add(new Point(color, x, y));
    }
    
    public void drawPolygon(Polygon polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Poly(color, polygon));
    }
    
    public void drawPolygon(DenseMatrix64F polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Poly(color, polygon));
    }
    
    public void drawLines(Polygon polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Lines(color, polygon));
    }
    
    public void drawLines(DenseMatrix64F polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Lines(color, polygon));
    }
    
    @Override
    public void setScreen(double width, double height)
    {
        throw new UnsupportedOperationException("Screen coordinates must be set in constructor");
    }
    
    public void plot(String filename, String ext) throws IOException
    {
        File file;
        if (dir != null)
        {
            file = new File(dir, filename+'.'+ext);
        }
        else
        {
            file = new File(filename+'.'+ext);
        }
        plot(file, ext);
    }
    public void plot(File file, String ext) throws IOException
    {
        for (Drawable d : drawables)
        {
            d.draw(graphics2D);
        }
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            ImageIO.write(image, ext, fos);
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }

    private class Drawable
    {
        Color color;

        public Drawable(Color color)
        {
            this.color = color;
        }
        
        protected void draw(Graphics2D graphics2D)
        {
            graphics2D.setColor(color);
        }
    }
    private class Point extends Drawable
    {
        double x;
        double y;
        public Point(Color color, double x, double y)
        {
            super(color);
            this.x = x;
            this.y = y;
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
    private class Circl extends Point
    {
        double r;

        public Circl(Color color, double x, double y, double r)
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
            int sr2 = 2*sr;
            graphics2D.drawOval(sx-sr, sy-sr, sr2, sr2);
        }
    }
    private class Poly extends Drawable
    {
        double[] data;
        public Poly(Color color, Polygon polygon)
        {
            super(color);
            DenseMatrix64F m = polygon.points;
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }

        private Poly(Color color, DenseMatrix64F m)
        {
            super(color);
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }
        
        @Override
        protected void draw(Graphics2D graphics2D)
        {
            super.draw(graphics2D);
            int len = data.length/2;
            if (len >= 2)
            {
                int x1 = (int) toScreenX(data[2*(len-1)]);
                int y1 = (int) toScreenY(data[2*(len-1)+1]);
                for (int r=0;r<len;r++)
                {
                    int x2 = (int) toScreenX(data[2*r]);
                    int y2 = (int) toScreenY(data[2*r+1]);
                    graphics2D.drawLine(x1, y1, x2, y2);
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
    }
    private class Lines extends Drawable
    {
        double[] data;
        public Lines(Color color, Polygon polygon)
        {
            super(color);
            DenseMatrix64F m = polygon.points;
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }

        private Lines(Color color, DenseMatrix64F m)
        {
            super(color);
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }
        
        @Override
        protected void draw(Graphics2D graphics2D)
        {
            super.draw(graphics2D);
            int len = data.length/2;
            if (len >= 2)
            {
                int x1 = (int) toScreenX(data[0]);
                int y1 = (int) toScreenY(data[1]);
                for (int r=1;r<len;r++)
                {
                    int x2 = (int) toScreenX(data[2*r]);
                    int y2 = (int) toScreenY(data[2*r+1]);
                    graphics2D.drawLine(x1, y1, x2, y2);
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
    }
}
