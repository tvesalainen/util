/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.hffax;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import static org.vesalainen.ham.hffax.FaxTone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxRenderer implements FaxListener
{
    private static final String TYPE = "png";
    private BufferedImage image;
    private String filename;
    private final Graphics2D graphics;
    private final long startOfLine;
    private int line;
    private final long lineLength;
    private Rectangle bounds;

    public FaxRenderer(String filename, int resolution, long startOfLine, long lineLength)
    {
        this.filename = filename;
        this.startOfLine = startOfLine;
        this.lineLength = lineLength;
        image = new BufferedImage(resolution, 2*resolution, TYPE_BYTE_BINARY);
        graphics = image.createGraphics();
        graphics.setBackground(Color.BLACK);
        graphics.setColor(Color.white);
        bounds = graphics.getDeviceConfiguration().getBounds();
    }

    public FaxRenderer(Graphics2D graphics, long startOfLine, long lineLength)
    {
        this.graphics = graphics;
        this.startOfLine = startOfLine;
        this.lineLength = lineLength;
        graphics.setBackground(Color.BLACK);
        graphics.setColor(Color.white);
        bounds = graphics.getDeviceConfiguration().getBounds();
    }
    
    public void render() throws IOException
    {
        if (image != null)
        {
            BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), line);
            ImageIO.write(subimage, TYPE, new File(filename+"."+TYPE));
        }
    }

    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude)
    {
        int lin2 = line(end);
        if (tone == WHITE)
        {
            int col1 = column(begin);
            int lin1 = line(begin);
            int col2 = column(end);
            if (lin1 == lin2)
            {
                graphics.drawLine(col1, lin1, col2, lin2);
            }
            else
            {
                graphics.drawLine(col1, lin1, bounds.width, lin1);
                for (int ll=lin1+1;ll<lin2-1;ll++)
                {
                    graphics.drawLine(0, ll, bounds.width, ll);
                }
                graphics.drawLine(0, lin2, col2, lin2);
            }
        }
        if (line != lin2)
        {
            System.err.println("LINE "+line);
        }
        line = lin2;
    }
    private int column(long begin)
    {
        return (int) (bounds.width*((begin-startOfLine) % lineLength)/lineLength);
    }
    private int line(long begin)
    {
        return (int) ((begin-startOfLine) /lineLength);
    }
}
