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
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;

/**
 *
 * @author Timo Vesalainen
 */
public class Plotter extends AbstractView
{
    private Color color = Color.BLACK;
    protected final List<Drawable> drawables = new ArrayList<>();
    private File dir;
    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    protected final Color background;

    public Plotter(int width, int height)
    {
        this(width, height, new Color(255, 255, 255, 255));
    }
    public Plotter(int width, int height, Color background)
    {
        super.setScreen(width, height);
        this.background = background;
    }

    public void clear()
    {
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
        drawables.add(new Pnt(color, x, y));
    }
    
    public void drawPoint(Point p)
    {
        drawPoint(p.getX(), p.getY());
    }
    
    public void drawPoint(double x, double y)
    {
        updatePoint(x, y);
        drawables.add(new Pnt(color, x, y));
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
        updatePoint(x1, y1);
        updatePoint(x2, y2);
        drawables.add(new Lin(color, x1, y1, x2, y2));
    }
    public void drawCoordinateLine(double x1, double y1, double x2, double y2)
    {
        updatePoint(x1, y1);
        updatePoint(x2, y2);
        drawables.add(0, new Lin(color, x1, y1, x2, y2));
    }
    public void drawLines(double[] data)
    {
        updatePolygon(data, data.length);
        drawables.add(new Lines(color, data));
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
    public void drawCoordinates()
    {
        Color safe = color;
        int minx = (int) (xMin-1);
        int maxx = (int) (xMax+1);
        int miny = (int) (yMin-1);
        int maxy = (int) (yMax+1);
        color = Color.BLACK;
        drawCoordinateLine(minx, 0, maxx, 0);
        drawCoordinateLine(0, miny, 0, maxy);
        color = Color.LIGHT_GRAY;
        for (int x=minx;x<=maxx;x++)
        {
            drawCoordinateLine(x, miny, x, maxy);
        }
        for (int y=miny;y<=maxy;y++)
        {
            drawCoordinateLine(minx, y, maxx, y);
        }
        color = safe;
    }
    @Override
    public void setScreen(double width, double height)
    {
        throw new UnsupportedOperationException("Screen coordinates must be set in constructor");
    }

    public void plotToDocFiles(Class<?> cls, String filename, String ext) throws IOException
    {
        String dirName = String.format("src/main/resources/%s/doc-files", 
                cls.getPackage().getName().replace('.', '/'));
        File dir = new File(dirName);
        dir.mkdirs();
        File file = new File(dir, filename+"."+ext);
        plot(file, ext);
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
        BufferedImage image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setBackground(background);
        graphics2D.clearRect(0, 0, (int)width, (int)height);
        Graphics2DDrawer g2d = new Graphics2DDrawer(graphics2D);
        drawables.stream().forEach((d) ->
        {
            d.draw(g2d);
        });
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            //System.err.println(Arrays.toString(ImageIO.getWriterMIMETypes()));  // [image/vnd.wap.wbmp, image/png, image/x-png, image/jpeg, image/bmp, image/gif]
            ImageIO.write(image, ext, fos);
        }
        catch (IOException ex)
        {
            throw ex;
         }
    }

    protected class Drawable
    {
        Color color;

        public Drawable(Color color)
        {
            this.color = color;
        }
        
        public void draw(Drawer drawer)
        {
            drawer.color(color);
        }
    }
    protected class Pnt extends Drawable
    {
        double x;
        double y;
        public Pnt(Color color, double x, double y)
        {
            super(color);
            this.x = x;
            this.y = y;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int sx = (int) toScreenX(x);
            int sy = (int) toScreenY(y);
            drawer.ellipse(sx-2, sy-2, 2, 2);
        }
    }
    protected class Circl extends Pnt
    {
        double r;

        public Circl(Color color, double x, double y, double r)
        {
            super(color, x, y);
            this.r = r;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int sx = (int) toScreenX(x);
            int sy = (int) toScreenY(y);
            int sr = (int) scaleToScreen(r);
            drawer.ellipse(sx-sr, sy-sr, sr, sr);
        }
    }
    protected class Poly extends Drawable
    {
        double[] data;

        public Poly(Color color, double[] data)
        {
            super(color);
            this.data = data;
        }
        
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
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int len = data.length/2;
            if (len >= 2)
            {
                double x1 = toScreenX(data[2*(len-1)]);
                double y1 = toScreenY(data[2*(len-1)+1]);
                for (int r=0;r<len;r++)
                {
                    double x2 = toScreenX(data[2*r]);
                    double y2 = toScreenY(data[2*r+1]);
                    drawer.line(x1, y1, x2, y2);
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
    }
    protected class Lin extends Drawable
    {
        double x1;
        double y1;
        double x2;
        double y2;

        public Lin(Color color, double x1, double y1, double x2, double y2)
        {
            super(color);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            double sx1 = toScreenX(x1);
            double sy1 = toScreenY(y1); 
            double sx2 = toScreenX(x2); 
            double sy2 = toScreenY(y2);
            drawer.line(sx1, sy1, sx2, sy2);
        }
    }
    protected class Lines extends Drawable
    {
        double[] data;

        public Lines(Color color, double[] data)
        {
            super(color);
            this.data = data;
        }
        
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
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int len = data.length/2;
            if (len >= 2)
            {
                double x[] = new double[len];
                double y[] = new double[len];
                x[0] = toScreenX(data[0]);
                y[0] = toScreenY(data[1]);
                for (int r=1;r<len;r++)
                {
                    x[r] = toScreenX(data[2*r]);
                    y[r] = toScreenY(data[2*r+1]);
                }
                drawer.polyline(x, y);
            }
        }
    }
}
