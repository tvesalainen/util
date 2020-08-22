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
package org.vesalainen.navi.dd1;

import java.awt.Color;
import java.io.IOException;
import static java.lang.Math.*;
import java.nio.file.Path;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DD1Calc extends DD1
{
    private final double delta;    // a-o angle
    private double ax;
    private final double h;
    private double offset;   // offset
    private double linkR;    // drag link
    private double link0;    // drag link at 0 angle
    private double linkL;

    public DD1Calc(double r1, double r2, double alpha, double beta, double a, double o)
    {
        this.r1 = r1;
        this.r2 = r2;
        this.alpha = alpha;
        this.beta = beta;
        this.a = a;
        this.ax = a;
        this.o = o;
        this.h = hypot(a, o);
        this.delta = toDegrees(atan2(o, a));
        calc();
    }
    private void calc()
    {
        this.offset = r1*cos(toRadians(alpha))-r2*cos(toRadians(beta));
        this.ax = sqrt(square(h)-square(offset));
        this.linkR = hypot(r1*sin(toRadians(alpha))-(ax+r2*sin(toRadians(beta))), r1*cos(toRadians(alpha))-(r2*cos(toRadians(beta)-offset)));
        this.linkL = hypot(r1*sin(toRadians(-alpha))-(ax+r2*sin(toRadians(-beta))), r1*cos(toRadians(-alpha))-(r2*cos(toRadians(-beta)-offset)));
                //ax - r1*sin(toRadians(alpha))+r2*sin(toRadians(beta));
        this.link0 = hypot(ax, r1-(r2+offset));
    }
    public void optR1()
    {
        double delta = abs(link0-linkR);
        double step = 1;
        while (step != 0)
        {
            r1 += step;
            calc();
            double d = abs(link0-linkR);
            if (d < delta)
            {
                delta = d;
            }
            else
            {
                r1 -= step;
                step *= -0.5;
            }
        }
    }
    public double alpha(double b)
    {
        double gamma = gamma();
        double a2 = toRadians(gamma+b);
        double betaX = a + r2*sin(a2);
        double betaY = o + r2*cos(a2);
        double a1 = 0;
        if (beta != 0)
        {
            a1 = alpha*a2/beta;
        }
        double delta = linkR;
        double dp = delta;
        double step = toRadians(1);
        while (step != 0 && delta > 1e-11)
        {
            double alphaX = r1*sin(a1);
            double alphaY = r1*cos(a1);
            double l = hypot(betaX-alphaX, betaY-alphaY);
            double d = abs(l-linkR);
            if (d < delta)
            {
                delta = d;
                //System.err.println("+"+toDegrees(a1)+" "+l);
            }
            else
            {
                if (d > dp)
                {
                    //System.err.println("-"+toDegrees(a1)+" "+l);
                    a1 -= step;
                    step *= -0.5;
                }
            }
            dp = d;
            a1 += step;
        }
        return toDegrees(a1)-gamma;
    }
    private double square(double x)
    {
        return x*x;
    }
    public double gamma()
    {
        return toDegrees(atan2(offset, ax))-delta;
    }
    public double offset()
    {
        return offset;
    }
    public double link()
    {
        return linkR;
    }
    public double link0()
    {
        return link0;
    }
    public void plot(Path path) throws IOException 
    {
        Plotter p = new Plotter(2000, 2000);
        
        p.setColor(Color.BLUE);
        p.drawLineTo(0, 0, 90, r1);
        p.drawLineTo(0, 0, 90-alpha, r1);
        p.drawLineTo(0, 0, 90+alpha, r1);
        
        p.setColor(Color.GREEN);
        p.drawLineTo(a, offset, 90, r2);
        p.drawLineTo(a, offset, 90-beta, r2);
        p.drawLineTo(a, offset, 90+beta, r2);
        
        p.setColor(Color.LIGHT_GRAY);
        p.drawCoordinates();
        p.plot(path);
    }
}
