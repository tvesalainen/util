/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.ui.path.PathMaker;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class WindArrowTest
{
    
    public WindArrowTest()
    {
    }

    @Test
    public void test1()
    {
        WindArrow wa = new WindArrow(new PM(new AppendablePrinter(System.err)));
        for (int w=0;w<100;w+=5)
        {
            System.err.print("\"");
            wa.draw(w);
            System.err.println("\"");
        }
    }
 
    private class PM implements PathMaker
    {
        AppendablePrinter o;

        public PM(AppendablePrinter o)
        {
            this.o = o;
        }
        
        @Override
        public void beginPath()
        {
        }

        @Override
        public void moveTo(double x, double y)
        {
            o.format("M%.0f %.0f", x, y);
        }

        @Override
        public void lineTo(double x, double y)
        {
            o.format("L%.0f %.0f", x, y);
        }

        @Override
        public void quadTo(double x1, double y1, double x, double y)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void cubicTo(double x1, double y1, double x2, double y2, double x, double y)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void closePath()
        {
            o.append("Z");
        }

        @Override
        public void fillColor(Object color)
        {
        }

        @Override
        public Object getColor(String color)
        {
            return null;
        }
        
    }
}
