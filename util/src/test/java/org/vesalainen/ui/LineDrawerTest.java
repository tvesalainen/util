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

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineDrawerTest
{
    public LineDrawerTest()
    {
    }

    @Test
    public void testFillWidth0()
    {
        Point start = new Point();
        Point der = new Point(1, 1);
        List<Point> list = new ArrayList<>();
        LineDrawer.fillWidth(start, der, 6, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(5, list.size());
    }
    @Test
    public void testFillWidthHorizontal()
    {
        Point start = new Point();
        Point der = new Point(1, 0);
        List<Point> list = new ArrayList<>();
        LineDrawer.fillWidth(start, der, 6, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
    }
    @Test
    public void testFillWidthVertical()
    {
        Point start = new Point();
        Point der = new Point(0, 1);
        List<Point> list = new ArrayList<>();
        LineDrawer.fillWidth(start, der, 6, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
    }
    @Test
    public void testHorizontal1()
    {
        Point start = new Point(-3, 1);
        Point end = new Point(3, 1);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(6));
    }
    @Test
    public void testHorizontal2()
    {
        Point start = new Point(3, 1);
        Point end = new Point(-3, 1);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
        assertEquals(end, list.get(0));
        assertEquals(start, list.get(6));
    }
    @Test
    public void testVertical1()
    {
        Point start = new Point(1, -3);
        Point end = new Point(1, 3);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(6));
    }
    @Test
    public void testVertical2()
    {
        Point start = new Point(1, 3);
        Point end = new Point(1, -3);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(6));
    }
    @Test
    public void testDiagonal0()
    {
        Point start = new Point(-3, -3);
        Point end = new Point(3, 3);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(7, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(6));
    }
    @Test
    public void testDiagonal1()
    {
        Point start = new Point(-2, -1);
        Point end = new Point(6, 3);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(9, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(8));
    }
    @Test
    public void testDiagonal2()
    {
        Point start = new Point(-2, -1);
        Point end = new Point(3, 6);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(8, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(7));
    }
    @Test
    public void testDiagonal3()
    {
        Point start = new Point(-1, -1);
        Point end = new Point(6, -3);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(8, list.size());
        assertEquals(start, list.get(0));
        assertEquals(end, list.get(7));
    }
    @Test
    public void testDiagonal4()
    {
        Point start = new Point(1, 1);
        Point end = new Point(-3, 6);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(6, list.size());
        assertEquals(end, list.get(0));
        assertEquals(start, list.get(5));
    }
    @Test
    public void testDiagonal5()
    {
        Point start = new Point(1, 1);
        Point end = new Point(-3, -6);
        List<Point> list = new ArrayList<>();
        LineDrawer.drawLine(start, end, (int x, int y)->list.add(new Point(x,y)));
        assertEquals(8, list.size());
        assertEquals(end, list.get(0));
        assertEquals(start, list.get(7));
    }
}
