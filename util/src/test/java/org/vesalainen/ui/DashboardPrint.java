/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DashboardPrint
{
    @Test
    public void print() throws IOException
    {
        int size = 1000;
        Plotter p = new Plotter(size, size, Color.WHITE);
        p.setColor(Color.BLACK);
        int height = 200;
        int width = 174;
        p.drawLine(0, 0, 0, height);
        p.drawLine(0, 0.01, width, 0.01);
        p.drawLine(width, 0, width, height);
        p.drawLine(0, height, width, height);
        for (int i=0;i<4;i++)
        {
            int x = i*40+22;
            for (int j=0;j<5;j++)
            {
                int y=j*36+28;
                circle(p, x, y, 12);
                if ((j==0 && i < 3) || (j==4 && i==3))
                {
                    circle(p, x+16, y+5, 6.35);
                    circle(p, x+16, y-5, 6.35);
                }
                else
                {
                    circle(p, x+16, y, 6.35);
                }
            }
        }
        p.plot("dashboard.png");

    }

    private void circle(Plotter p, double x, double y, double dia)
    {
        p.drawCross(x, y);
        p.drawCircle(x, y, dia/2.0);
    }

}
