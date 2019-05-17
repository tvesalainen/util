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

import java.awt.BasicStroke;
import java.awt.Color;
import static java.awt.Font.BOLD;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.vesalainen.ui.Direction.*;
import org.vesalainen.ui.scale.BasicScale;
import org.vesalainen.ui.scale.LogarithmScale;
import org.vesalainen.ui.scale.MergeScale;

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
            Plotter p = new Plotter(1000, 1000, Color.LIGHT_GRAY);//, true, MergeScale.BASIC15, new LogarithmScale(10, "u"));
            p.setColor(Color.BLACK);
            p.setTraceColor(Color.BLACK);
            //p.setStroke(new BasicStroke(10));
            p.setFont("Arial", BOLD, 20);
            p.drawText(50, 90, "start", TextAlignment.START_X);
            p.drawText(50, 50, "middle", TextAlignment.MIDDLE_X);
            p.drawText(50, 10, "end", TextAlignment.END_X);
            p.setColor(Color.ORANGE);
            p.drawCircle(50, 50, 30);
            p.setColor(Color.RED);
            p.drawCross(47, 30);
            //p.drawPoint(1, 1);
            //p.drawPoint(2, 0);
            BasicCoordinates bc = new BasicCoordinates(p);
            p.setColor(Color.BLUE);
            bc.addCoordinate(LEFT, BasicScale.SCALE10);
            p.setColor(Color.GREEN);
            bc.addCoordinate(BOTTOM, BasicScale.SCALE05);
            p.setColor(Color.PINK);
            bc.addCoordinate(RIGHT, BasicScale.SCALE03);
            p.drawBackground(bc);
            //p.drawCoordinates(LEFT, BOTTOM, RIGHT, TOP);
            p.setColor(Color.RED);
            p.drawTitle(TOP, "Yläotsikko");
            p.drawTitle(LEFT, "Vasenotsikko");
            p.drawTitle(BOTTOM, "Alaotsikko");
            p.drawTitle(RIGHT, "Oikeaotsikko");
            p.plot("plotterTest.png");
        }
        catch (IOException ex)
        {
            Logger.getLogger(PlotterT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
