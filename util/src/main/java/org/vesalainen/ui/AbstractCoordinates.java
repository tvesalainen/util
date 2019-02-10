/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractCoordinates extends Path2D.Double
{
    private Rectangle2D.Double screenBounds;
    private AffineTransform transform;
    private Point2D.Double srcPnt = new Point2D.Double();
    private Point2D.Double dstPnt = new Point2D.Double();

    public AbstractCoordinates(Rectangle2D.Double screenBounds, AffineTransform transform)
    {
        this.screenBounds = screenBounds;
        this.transform = transform;
    }
    
    public final void update()
    {
        reset();
        build();
    }
    protected abstract void build();
    
    protected void horizontalLine(double y)
    {
        srcPnt.setLocation(0, y);
        transform.transform(srcPnt, dstPnt);
        dstPnt.x = screenBounds.getMinX();
        moveTo(dstPnt);
        dstPnt.x = screenBounds.getMaxX();
        lineTo(dstPnt);
    }
    protected void verticalLine(double x)
    {
        srcPnt.setLocation(x, 0);
        transform.transform(srcPnt, dstPnt);
        dstPnt.y = screenBounds.getMinY();
        moveTo(dstPnt);
        dstPnt.y = screenBounds.getMaxY();
        lineTo(dstPnt);
    }
    
    public void moveTo(Point2D.Double p)
    {
        moveTo(p.x, p.y);
    }
    public void lineTo(Point2D.Double p)
    {
        moveTo(p.x, p.y);
    }
}
