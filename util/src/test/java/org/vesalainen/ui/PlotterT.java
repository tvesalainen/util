/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PlotterT
{
    
    public PlotterT()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            Plotter p = new Plotter(1000, 1000);
            p.drawPoint(0, 0);
            p.drawPoint(1, 1);
            p.drawPoint(2, 0);
            p.drawCoordinates();
            p.plot("plotterTest", "png");
        }
        catch (IOException ex)
        {
            Logger.getLogger(PlotterT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
