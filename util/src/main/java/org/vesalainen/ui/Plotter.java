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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.ejml.data.DenseMatrix64F;
import org.vesalainen.math.Circle;
import org.vesalainen.math.Point;
import org.vesalainen.math.Polygon;
import org.vesalainen.math.Rect;

/**
 *
 * @author Timo Vesalainen
 */
public class Plotter extends AbstractView
{
    private Locale locale = Locale.getDefault();
    private Color color = Color.BLACK;
    private String fontName;
    private int fontStyle;
    private double fontSize;
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
        this.keepAspectRatio = true;
    }
    public Plotter(Component component, Color background, boolean keepAspectRatio)
    {
        Rectangle bounds = component.getBounds();
        super.setScreen(bounds.width, bounds.height);
        this.background = background;
        this.keepAspectRatio = keepAspectRatio;
    }
    /**
     * Clears the screen
     */
    public void clear()
    {
        drawables.clear();
    }
    /**
     * Set default directory for plotting. Affects only plot methods with String
     * filename.
     * @param dir 
     */
    public void setDir(File dir)
    {
        this.dir = dir;
    }
    /**
     * Set locale
     * @param locale 
     * @return  
     */
    public Plotter setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }
    /**
     * Set color for following drawings
     * @param color
     * @return 
     */
    public Plotter setColor(Color color)
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
    public Plotter setFont(String fontName, int fontStyle, double fontSize)
    {
        this.fontName = fontName;
        this.fontStyle = fontStyle;
        this.fontSize = fontSize;
        return this;
    }

    public void drawText(double x, double y, String text)
    {
        drawText(x, y, TextAlignment.START_X, text);
    }
    public void drawText(double x, double y, TextAlignment alignment, String text)
    {
        updatePoint(x, y);
        drawables.add(new Text(this, color, fontName, fontStyle, fontSize, x, y, text, alignment));
    }
    public void drawCircle(Circle circle)
    {
        drawCircle(circle.getX(), circle.getY(), circle.getRadius());
    }
    
    public void drawCircle(double x, double y, double r)
    {
        updateCircle(x, y, r);
        drawables.add(new Circl(this, color, x, y, r));
    }
    
    public void drawPoint(DenseMatrix64F point)
    {
        assert point.numCols == 1;
        assert point.numRows == 2;
        double[] d = point.data;
        double x = d[0];
        double y = d[1];
        updatePoint(x, y);
        drawables.add(new Pnt(this, color, x, y));
    }
    
    public void drawPoint(Point p)
    {
        drawPoint(p.getX(), p.getY());
    }
    
    public void drawPoint(double x, double y)
    {
        updatePoint(x, y);
        drawables.add(new Pnt(this, color, x, y));
    }
    
    public void drawPolygon(Polygon polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Poly(this, color, polygon));
    }
    
    public void drawPolygon(DenseMatrix64F polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Poly(this, color, polygon));
    }
    public void drawPoints(double[] arr)
    {
        for (int ii=0;ii<arr.length;ii++)
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
        updatePoint(x1, y1);
        updatePoint(x2, y2);
        drawables.add(new Lin(this, color, x1, y1, x2, y2));
    }
    public void drawPolyline(Polyline polyline)
    {
        updateRect(polyline.getBounds());
        drawables.add(polyline);
    }
    public void drawCoordinateLine(double x1, double y1, double x2, double y2)
    {
        updatePoint(x1, y1);
        updatePoint(x2, y2);
        drawables.add(0, new Lin(this, color, x1, y1, x2, y2));
    }
    public void drawLines(double[] data)
    {
        updatePolygon(data, data.length);
        drawables.add(new Lines(this, color, data));
    }
    
    public void drawLines(Polygon polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Lines(this, color, polygon));
    }
    
    public void drawLines(DenseMatrix64F polygon)
    {
        updatePolygon(polygon);
        drawables.add(new Lines(this, color, polygon));
    }
    public void drawCoordinates()
    {
        drawCoordinateX();
        drawCoordinateY();
    }
    public void drawCoordinateX()
    {
        fontSize = fontSize == 0 ? (yMax-yMin)/10 : fontSize;
        // horizontal
        Scaler scaler = new Scaler(xMin, xMax);
        double sum = 0;
        double level=0;
        double w = xMax-xMin;
        for (;sum < w/2;level += 0.5)
        {
            sum = 0;
            for (String label : scaler.getLabels(locale, level))
            {
                sum += label.length()*fontSize*0.6;
            }
        }
        level--;
        updateY(yMin - fontSize);
        drawables.add(new CoordinateX(this, color, fontName, fontStyle, fontSize, scaler, level));
    }
    public void drawCoordinateY()
    {
        fontSize = fontSize == 0 ? (yMax-yMin)/10 : fontSize;
        // vertical
        Scaler scaler = new Scaler(yMin, yMax);
        double sum = 0;
        double level=0;
        double h = yMax-yMin;
        for (;sum < h/4;level += 0.5)
        {
            int size = scaler.getLabels(locale, level).size();
            sum = size*fontSize;
        }
        level--;
        double max = 0;
        for (String label : scaler.getLabels(locale, level))
        {
            max = Math.max(max, label.length()*fontSize);
        }
        updateX(xMin - max);
        drawables.add(new CoordinateY(this, color, fontName, fontStyle, fontSize, scaler, level));
    }
    public void drawCoordinates0()
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
        drawables.forEach((d) ->
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

    public static class Drawable
    {
        protected Plotter plotter;
        protected Color color;
        protected String fontName;
        protected int fontStyle;
        protected double fontSize;

        public Drawable(Plotter plotter, Color color)
        {
            this.plotter = plotter;
            this.color = color;
        }

        public Drawable(Plotter plotter, Color color, String fontName, int fontStyle, double fontSize)
        {
            this.plotter = plotter;
            this.color = color;
            this.fontName = fontName;
            this.fontStyle = fontStyle;
            this.fontSize = fontSize;
        }

        public void draw(Drawer drawer)
        {
            drawer.color(color);
            drawer.font(fontName, fontStyle, (int) plotter.scaleToScreenY(fontSize));
        }
    }
    protected static class Pnt extends Drawable
    {
        double x;
        double y;
        public Pnt(Plotter plotter, Color color, double x, double y)
        {
            super(plotter, color);
            this.x = x;
            this.y = y;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int sx = (int) plotter.toScreenX(x);
            int sy = (int) plotter.toScreenY(y);
            drawer.ellipse(sx-2, sy-2, 2, 2);
        }
    }
    protected static class Circl extends Pnt
    {
        double r;

        public Circl(Plotter plotter, Color color, double x, double y, double r)
        {
            super(plotter, color, x, y);
            this.r = r;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int sx = (int) plotter.toScreenX(x);
            int sy = (int) plotter.toScreenY(y);
            int sr = (int) plotter.scaleToScreen(r);
            drawer.ellipse(sx-sr, sy-sr, sr, sr);
        }
    }
    protected static class Poly extends Drawable
    {
        double[] data;

        public Poly(Plotter plotter, Color color, double[] data)
        {
            super(plotter, color);
            this.data = data;
        }
        
        public Poly(Plotter plotter, Color color, Polygon polygon)
        {
            super(plotter, color);
            DenseMatrix64F m = polygon.points;
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }

        private Poly(Plotter plotter, Color color, DenseMatrix64F m)
        {
            super(plotter, color);
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            int len = data.length/2;
            if (len >= 2)
            {
                double x1 = plotter.toScreenX(data[2*(len-1)]);
                double y1 = plotter.toScreenY(data[2*(len-1)+1]);
                for (int r=0;r<len;r++)
                {
                    double x2 = plotter.toScreenX(data[2*r]);
                    double y2 = plotter.toScreenY(data[2*r+1]);
                    drawer.line(x1, y1, x2, y2);
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
    }
    protected static class Lin extends Drawable
    {
        double x1;
        double y1;
        double x2;
        double y2;

        public Lin(Plotter plotter, Color color, double x1, double y1, double x2, double y2)
        {
            super(plotter, color);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            double sx1 = plotter.toScreenX(x1);
            double sy1 = plotter.toScreenY(y1); 
            double sx2 = plotter.toScreenX(x2); 
            double sy2 = plotter.toScreenY(y2);
            drawer.line(sx1, sy1, sx2, sy2);
        }
    }
    protected static class Lines extends Drawable
    {
        double[] data;

        public Lines(Plotter plotter, Color color, double[] data)
        {
            super(plotter, color);
            this.data = data;
        }
        
        public Lines(Plotter plotter, Color color, Polygon polygon)
        {
            super(plotter, color);
            DenseMatrix64F m = polygon.points;
            this.data = Arrays.copyOf(m.data, m.getNumElements());
        }

        private Lines(Plotter plotter, Color color, DenseMatrix64F m)
        {
            super(plotter, color);
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
                x[0] = plotter.toScreenX(data[0]);
                y[0] = plotter.toScreenY(data[1]);
                for (int r=1;r<len;r++)
                {
                    x[r] = plotter.toScreenX(data[2*r]);
                    y[r] = plotter.toScreenY(data[2*r+1]);
                }
                drawer.polyline(x, y);
            }
        }
    }
    public Polyline polyline(Color color)
    {
        return new Polyline(this, color);
    }
    public static class Polyline extends Drawable
    {
        private List<Double> xData = new ArrayList<>();
        private List<Double> yData = new ArrayList<>();
        private Rect bounds = new Rect();
        
        protected Polyline(Plotter plotter, Color color)
        {
            super(plotter, color);
        }
        
        public void lineTo(Point p)
        {
            lineTo(p.getX(), p.getY());
        }
        public void lineTo(double x, double y)
        {
            xData.add(x);
            yData.add(y);
            bounds.update(x, y);
        }

        public void lineTo(Stream<Point> stream)
        {
            stream.forEach(this::lineTo);
        }
        
        public Rect getBounds()
        {
            return bounds;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            double[] xArr = new double[xData.size()];
            double[] yArr = new double[yData.size()];
            int len = xArr.length;
            for (int ii=0;ii<len;ii++)
            {
                xArr[ii] = plotter.toScreenX(xData.get(ii));
                yArr[ii] = plotter.toScreenY(yData.get(ii));
            }
            drawer.polyline(xArr, yArr);
        }        
    }
    public static class Text extends Drawable
    {
        private double x;
        private double y;
        private String text;
        private TextAlignment alignment;

        public Text(Plotter plotter, Color color, String fontName, int fontStyle, double fontSize, double x, double y, String text, TextAlignment alignment)
        {
            super(plotter, color, fontName, fontStyle, fontSize);
            this.x = x;
            this.y = y;
            this.text = text;
            this.alignment = alignment;
        }


        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            drawer.text(plotter.toScreenX(x), plotter.toScreenY(y), alignment, text);
        }
        
    }
    public static class CoordinateX extends Drawable
    {
        private Scaler scaler;
        private double level;

        public CoordinateX(Plotter plotter, Color color, String fontName, int fontStyle, double fontSize, Scaler scaler, double level)
        {
            super(plotter, color, fontName, fontStyle, fontSize);
            this.scaler = scaler;
            this.level = level;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            if (level >= 0)
            {
                String format = scaler.getFormat(level);
                scaler.stream(level+0.5)
                        .forEach((double x)->
                        {
                            double sx = plotter.toScreenX(x);
                            drawer.line(sx, 0, sx, plotter.height);
                        });
                scaler.stream(level)
                        .forEach((double x)->
                        {
                            String label = String.format(plotter.locale, format, x);
                            Rectangle2D bounds = drawer.bounds(label);
                            double width = bounds.getWidth();
                            double sx = plotter.toScreenX(x);
                            if (sx-width/2 >= 0 && sx+width/2 <= plotter.width)
                            {
                                drawer.line(sx, 0, sx, plotter.height-bounds.getHeight());
                                drawer.text(sx, plotter.height, TextAlignment.MIDDLE_X, label);
                            }
                        });
            }
        }
    }
    public static class CoordinateY extends Drawable
    {
        private Scaler scaler;
        private double level;

        public CoordinateY(Plotter plotter, Color color, String fontName, int fontStyle, double fontSize, Scaler scaler, double level)
        {
            super(plotter, color, fontName, fontStyle, fontSize);
            this.scaler = scaler;
            this.level = level;
        }
        
        @Override
        public void draw(Drawer drawer)
        {
            super.draw(drawer);
            if (level >= 0)
            {
                String format = scaler.getFormat(level);
                scaler.stream(level+0.5)
                        .forEach((double y)->
                        {
                            double sy = plotter.toScreenY(y);
                            drawer.line(0, sy, plotter.width, sy);
                        });
                scaler.stream(level)
                        .forEach((double y)->
                        {
                            String label = String.format(plotter.locale, format, y);
                            Rectangle2D bounds = drawer.bounds(label);
                            double height = bounds.getHeight();
                            double sy = plotter.toScreenY(y);
                            if (sy-height/4 >= 0 && sy+height/4 <= plotter.height)
                            {
                                drawer.text(0, sy, TextAlignment.MIDDLE_Y, label);
                            }
                        });
            }
        }
    }
}
