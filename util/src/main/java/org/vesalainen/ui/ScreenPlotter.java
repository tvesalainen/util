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
package org.vesalainen.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ScreenPlotter extends Plotter
{

    public ScreenPlotter(Component component)
    {
        this(component, Color.BLACK, false);
    }
    public ScreenPlotter(Component component, Color background, boolean keepAspectRatio)
    {
        super(component, background, keepAspectRatio);
    }
    
    public void plot(Graphics2D graphics2D)
    {
        graphics2D.setBackground(background);
        graphics2D.clearRect(0, 0, (int)width, (int)height);
        Graphics2DDrawer g2d = new Graphics2DDrawer(graphics2D);
        drawables.forEach((d) ->
        {
            d.draw(g2d);
        });
    }
}