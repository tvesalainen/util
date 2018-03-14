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
import java.nio.file.Path;
import javax.imageio.ImageIO;
import static org.vesalainen.ham.hffax.FaxTone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxRenderer implements FaxListener
{
    private static final String TYPE = "png";
    private FaxStateListener stateListener;
    private BufferedImage image;
    private Path out;
    private final Graphics2D graphics;
    private final PageLocator locator;
    private int line;
    private Rectangle bounds;

    public FaxRenderer(FaxStateListener stateListener, Path out, int resolution, PageLocator locator)
    {
        this.stateListener = stateListener;
        this.out = out;
        this.locator = locator;
        image = new BufferedImage(resolution, 2*resolution, TYPE_BYTE_BINARY);
        graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.setColor(Color.BLACK);
        bounds = graphics.getDeviceConfiguration().getBounds();
        graphics.clearRect(0, 0, bounds.width, bounds.height);
    }

    public FaxRenderer(Graphics2D graphics, PageLocator locator)
    {
        this.graphics = graphics;
        this.locator = locator;
        graphics.setBackground(Color.WHITE);
        graphics.setColor(Color.BLACK);
        //graphics.scale(1, 2);
        bounds = graphics.getDeviceConfiguration().getBounds();
        graphics.clearRect(0, 0, bounds.width, bounds.height);
    }
    
    public boolean render() throws IOException
    {
        if (image != null)
        {
            BufferedImage subimage = image.getSubimage(0, locator.firstLine(), image.getWidth(), locator.lastLine());
            ImageIO.write(subimage, TYPE, out.toFile());
        }
        return true;
    }

    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude, long error)
    {
        int lin2 = locator.line(end);
        if (tone == BLACK)
        {
            int col1 = locator.column(bounds.width, begin);
            int lin1 = locator.line(begin);
            int col2 = locator.column(bounds.width, end);
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
        line = lin2;
        if (line > 1600)
        {
            stateListener.stop("lines > 1600");
        }
    }
}
