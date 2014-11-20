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

package org.vesalainen.navi;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejml.data.DenseMatrix64F;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen
 */
public class AnchorWatchTest
{
    public static final File dir = new File("target/surefire-reports");
    public AnchorWatchTest()
    {
    }

    /**
     * Test of update method, of class AnchorWatch.
     */
    @Test
    public void testUpdate()
    {
        URL url = AnchorWatchTest.class.getResource("/20141117091525.nmea");
        CoordinateParser parser = CoordinateParser.newInstance();
        Plotter plotter = new Plotter(1000, 1000);
        plotter.setDir(dir);
        AnchorWatch aw = new AnchorWatch(plotter);
        
        try
        {
            parser.parse(url, aw);
        }
        catch (IOException ex)
        {
            fail(ex.getMessage());
        }
    }
}
