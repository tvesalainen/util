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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageAreaFillerTest
{
    
    public ImageAreaFillerTest()
    {
    }

    @Test
    public void test0()
    {
        int width = 10;
        int height = 10;
        Rectangle clip = new Rectangle(2, 2, 2, 2);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ImageAreaFiller filler = new ImageAreaFiller(image);
        int rgb = Color.BLUE.getRGB();
        filler.fill(clip, (x,y)->clip.contains(x, y), rgb);
        for (int x=0;x<width;x++)
        {
            for (int y=0;y<height;y++)
            {
                assertEquals("("+x+", "+y+")", clip.contains(x, y), rgb == image.getRGB(x, y));
            }
        }
    }
    
}
