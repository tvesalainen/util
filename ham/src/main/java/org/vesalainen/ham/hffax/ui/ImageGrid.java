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
package org.vesalainen.ham.hffax.ui;

import static java.awt.Color.WHITE;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.awt.image.WritableRaster;
import java.util.Objects;
import org.vesalainen.util.AbstractArrayGrid;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageGrid extends AbstractArrayGrid<Boolean>
{
    private BufferedImage image;
    private WritableRaster raster;
    
    public ImageGrid(int width, int height)
    {
        super(width, height, 0, width*height, false);
        this.image = new BufferedImage(width, height, TYPE_BYTE_BINARY);
        this.raster = image.getRaster();
    }

    public BufferedImage getImage()
    {
        return image;
    }
    
    @Override
    public int width()
    {
        return raster.getWidth();
    }

    @Override
    public int height()
    {
        return raster.getHeight();
    }

    @Override
    public boolean hit(int x, int y, Boolean color)
    {
        if (x < 0 || y < 0 || x >= width() || y >= height())
        {
            return false;
        }
        return Objects.equals(color, getColor(x, y));
    }

    @Override
    public void setColor(int x, int y, Boolean color)
    {
        raster.setSample(x, y, 0, color ? 0 : WHITE.getRGB());
    }

    @Override
    public Boolean getColor(int x, int y)
    {
        return raster.getSample(x, y, 0) == 0;
    }

    @Override
    public AbstractArrayGrid view(int offset, int width)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setColor(int position, Boolean color)
    {
        setColor(column(position), line(position), color);
    }

    @Override
    protected Boolean getColor(int position)
    {
        return getColor(column(position), line(position));
    }
    
}
