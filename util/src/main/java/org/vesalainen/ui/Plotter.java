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
public class Plotter extends AbstractPlotter
{
    private File dir;

    public Plotter(int width, int height)
    {
        this(width, height, new Color(255, 255, 255, 255));
    }
    public Plotter(int width, int height, Color background)
    {
        super(width, height, background);
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
        ImageDrawer drawer = new ImageDrawer((int)screenBounds.width, (int)screenBounds.height);
        plot(drawer);
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            //System.err.println(Arrays.toString(ImageIO.getWriterMIMETypes()));  // [image/vnd.wap.wbmp, image/png, image/x-png, image/jpeg, image/bmp, image/gif]
            ImageIO.write(drawer.getImage(), ext, fos);
        }
        catch (IOException ex)
        {
            throw ex;
         }
    }

}
