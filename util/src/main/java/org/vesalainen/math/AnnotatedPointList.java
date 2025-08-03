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
package org.vesalainen.math;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * AnnotatedPointList is a PointList with annotations associated with points
 * and not with positions.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPointList<T> extends PointList
{
    private Map<Point2D,T> map = new HashMap<>();
    
    public AnnotatedPointList()
    {
    }

    public AnnotatedPointList(int initialSize)
    {
        super(initialSize);
    }

    public T getAnnotation(int index)
    {
        return map.get(get(index));
    }

    @Override
    public void clear()
    {
        super.clear();
        map.clear();
    }
    
    public void remove(int index, T annotation)
    {
        map.remove(get(index));
        super.remove(index);
    }

    public void set(int index, double x, double y, T annotation)
    {
        map.remove(get(index));
        super.set(index, x, y);
        map.put(new Point2D.Double(x, y), annotation);
    }

    public void set(int index, Point2D p, T annotation)
    {
        map.remove(get(index));
        super.set(index, p);
        map.put(new Point2D.Double(p.getX(), p.getY()), annotation);
    }

    public void add(int index, double x, double y, T annotation)
    {
        super.add(index, x, y);
        map.put(new Point2D.Double(x, y), annotation);
    }

    public void add(double x, double y, T annotation)
    {
        super.add(x, y);
        map.put(new Point2D.Double(x, y), annotation);
    }

    public void add(int index, Point2D p, T annotation)
    {
        super.add(index, p);
        map.put(new Point2D.Double(p.getX(), p.getY()), annotation);
    }

    public void add(Point2D p, T annotation)
    {
        super.add(p);
        map.put(new Point2D.Double(p.getX(), p.getY()), annotation);
    }
    
}
