/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.navi.Navis;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ChartPlotterTest
{
    
    public ChartPlotterTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        ChartPlotter cp = new ChartPlotter(1000, 1000);
        String lat = Navis.latitudeString(60.1);
        String lon = Navis.longitudeString(25.1);
        cp.drawPlus(141.102155, -36.609808);
        cp.drawCross(141.101232, -36.609503);
        cp.drawCross(141.101224, -36.609561);
        cp.drawCross(141.101221, -36.609586);
        cp.drawCross(141.101218, -36.609624);
        cp.drawCross(141.101218, -36.609625);
        cp.drawCoordinates();
        Path path = Paths.get("c:\\temp\\charPlotter.png");
        cp.plot(path);
    }
    
}
