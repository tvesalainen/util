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
package org.vesalainen.navi;

import org.vesalainen.ui.path.FunctionalPathMaker;
import org.vesalainen.ui.path.PathMaker;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WindArrow extends FunctionalPathMaker
{

    public WindArrow(PathMaker oth)
    {
        super(oth);
    }

    public void draw(double knots)
    {
        if (knots > 0)
        {
            if (knots > 50)
            {
                setFillColor("BLACK");
            }
            else if (knots > 40)
            {
                setFillColor("MAGENTA");
            }
            else if (knots > 20)
            {
                setFillColor("RED");
            }
            else if (knots > 15)
            {
                setFillColor("ORANGE");
            }
            else if (knots > 10)
            {
                setFillColor("GREEN");
            }
            else
            {
                setFillColor("GRAY");
            }

            int extension = 34;
            int ww = 1;
            beginPath();
            moveTo(-ww, 0);
            lineTo(-ww, -extension);
            lineTo(ww, -extension);
            lineTo(ww, 0);
            closePath();

            if (knots < 10)
            {
                extension -=7;
            }
            boolean ready = false;
            while (!ready)
            {
                if (knots >= 50)
                {
                    beginPath();
                    moveTo(-ww, -extension);
                    lineTo(-ww, -extension-10);
                    lineTo(17, -extension-10);
                    lineTo(ww, -extension+ww);
                    closePath();
                    knots -= 50;
                    extension -=7;
                }
                else
                {
                    if (knots >= 10)
                    {
                        beginPath();
                        moveTo(ww, -extension);
                        lineTo(ww+17, -extension-10);
                        lineTo(ww+17, -extension-10+ww);
                        lineTo(ww, -extension+ww);
                        closePath();
                        knots -= 10;
                        extension -=7;
                    }
                    else
                    {
                        if (knots >= 5)
                        {
                            beginPath();
                            moveTo(ww, -extension);
                            lineTo(ww+10, -extension-5);
                            lineTo(ww+10, -extension-5+ww);
                            lineTo(ww, -extension+ww);
                            closePath();
                            ready = true;
                            break;
                        }
                        else
                        {
                            ready = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setFillColor(String color)
    {
        fillColor(color);
    }
}
