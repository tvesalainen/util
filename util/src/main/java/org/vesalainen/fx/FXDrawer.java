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

import javafx.scene.canvas.GraphicsContext;
import org.vesalainen.ui.AbstractDrawer;

/**
 * @deprecated Not ready!
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FXDrawer extends AbstractDrawer
{
    private GraphicsContext gc;

    public FXDrawer(GraphicsContext gc)
    {
        this.gc = gc;
    }
    
    @Override
    protected void fill()
    {
        gc.fill();
    }

    @Override
    public void beginPath()
    {
        gc.beginPath();
    }

    @Override
    public void moveTo(double... cp)
    {
        gc.moveTo(cp[0], cp[1]);
    }
    
    @Override
    public void drawLine(double... cp)
    {
        gc.lineTo(cp[2], cp[3]);
    }

    @Override
    public void drawQuad(double... cp)
    {
        gc.quadraticCurveTo(cp[2], cp[3], cp[4], cp[5]);
    }

    @Override
    public void drawCubic(double... cp)
    {
        gc.bezierCurveTo(cp[2], cp[3], cp[4], cp[5], cp[6], cp[7]);
    }

    @Override
    public void closePath(double... cp)
    {
        gc.closePath();
    }

    @Override
    public <T> boolean supports(T target)
    {
        return (target instanceof GraphicsContext);
    }

    @Override
    public <T> void write(T target)
    {
    }

}
