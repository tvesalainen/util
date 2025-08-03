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

import static java.lang.Math.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DD1
{
    
    protected double r1; // rudder arm
    protected final double r2; // dd1 arm
    protected final double r2i;
    protected final double alpha; // max rudder angle (half)
    protected final double beta; // max DD1 angle (half)
    protected double gamma; // offset angle
    protected final double a;
    protected final double o;

    public DD1(DD1 oth)
    {
        this(oth.r1, oth.r2, oth.alpha, oth.beta, oth.gamma, oth.a, oth.o);
    }

    public DD1(double r1, double r2, double alpha, double beta, double gamma, double a, double o)
    {
        this.r1 = r1;
        this.r2 = r2;
        this.alpha = toRadians(alpha);
        this.beta = toRadians(beta);
        this.r2i = cos(this.beta)*r2;
        this.gamma = toRadians(gamma);
        this.a = a;
        this.o = o;
    }

    public DD1(double r2, double alpha, double beta, double a, double o)
    {
        this.r2 = r2;
        this.alpha = toRadians(alpha);
        this.beta = toRadians(beta);
        this.a = a;
        this.o = o;
        this.r2i = cos(this.beta)*r2;
    }

    protected final double x1(double r, double oa, double a1)
    {
        return r * sin(oa + a1);
    }

    protected final double y1(double r, double oa, double a1)
    {
        return r * cos(oa + a1);
    }

    protected final double x10(double r, double oa)
    {
        double r1i = cos(alpha)*r;
        return r1i * sin(oa);
    }

    protected final double y10(double r, double oa)
    {
        double r1i = cos(alpha)*r;
        return r1i * cos(oa);
    }

    protected final double x2(double oa, double a2)
    {
        return r2 * sin(oa + a2) + a;
    }

    protected final double y2(double oa, double a2)
    {
        return r2 * cos(oa + a2) + o;
    }
    
    protected final double x20(double oa, double a2)
    {
        return r2i * sin(oa + a2) + a;
    }

    protected final double y20(double oa, double a2)
    {
        return r2i * cos(oa + a2) + o;
    }
    
}
