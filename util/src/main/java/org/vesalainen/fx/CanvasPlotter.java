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
package org.vesalainen.fx;

import static java.awt.Color.WHITE;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.vesalainen.ui.AbstractPlotter;

/**
 * @deprecated Not ready
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CanvasPlotter extends AbstractPlotter
{

    private final Canvas canvas;
    
    public CanvasPlotter(Canvas canvas)
    {
        super((int)canvas.getWidth(), (int)canvas.getHeight(), WHITE);
        this.canvas = canvas;
    }
    public void plot()
    {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        FXDrawer drawer = new FXDrawer(gc);
        plot(drawer);
    }
    
}
