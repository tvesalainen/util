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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ImageRectifierTest
{
    
    public ImageRectifierTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINEST);
    }

    //@Test
    public void test1() throws IOException
    {
        File file = new File("c:\\temp\\fax2018-02-08T160936.267Z.png");
        BufferedImage image = ImageIO.read(file);
        ImageRectifier r = new ImageRectifier(image);
        r.rectify();
        ImageIO.write(image, "png", new File("corrected.png"));
    }
    
}
