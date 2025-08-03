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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.vesalainen.math.DoubleTransform;
import org.vesalainen.ui.scale.Scale;

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
    public Plotter(int width, int height, Color background, boolean keepAspectRatio)
    {
        super(width, height, background, keepAspectRatio);
    }
    public Plotter(int width, int height, Color background, boolean keepAspectRatio, Scale xScale, Scale yScale)
    {
        super(width, height, background, keepAspectRatio, xScale, yScale);
    }

    public Plotter(int width, int height, Color background, boolean keepAspectRatio, Scale xScale, Scale yScale, DoubleTransform transform)
    {
        super(width, height, background, keepAspectRatio, xScale, yScale, transform);
    }
    
    /**
     * Set default directory for plotting. Affects only plot methods with String
     * filename.
     * @param dir 
     */
    @Deprecated public void setDir(File dir)
    {
        this.dir = dir;
    }

    @Deprecated public void plotToDocFiles(Class<?> cls, String filename, String ext) throws IOException
    {
        String dirName = String.format("src/main/resources/%s/doc-files", 
                cls.getPackage().getName().replace('.', '/'));
        File dir = new File(dirName);
        dir.mkdirs();
        File file = new File(dir, filename+"."+ext);
        plot(file.toPath());
    }
    @Deprecated public void plot(String filename, String ext) throws IOException
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
        plot(file.toPath());
    }

}
