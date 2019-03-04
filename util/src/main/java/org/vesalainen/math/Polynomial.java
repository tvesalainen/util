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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Polynomial implements MathFunction
{
    private double[] coef;
    private int length;
    /**
     * Creates new Polynomial with coefficients a, b, c, ...
     * <p>Example: cx\u00B2+bx+a 
     * @param coef 
     */
    public Polynomial(double... coef)
    {
        this.coef = coef;
        this.length = length(coef);
    }
    private static int length(double[] coef)
    {
        for (int ii=coef.length-1;ii>=0;ii--)
        {
            if (Math.abs(coef[ii]) > Double.MIN_VALUE )
            {
                return ii+1;
            }
        }
        return 0;
    }
    public int degree()
    {
        return length-1;
    }
    @Override
    public double applyAsDouble(double x)
    {
        if (length == 0)
        {
            return 0;
        }
        double sum = coef[length-1]*x;
        for (int ii=length-2;ii>0;ii--)
        {
            sum += coef[ii];
            sum *= x;
        }
        sum += coef[0];
        return sum;
    }

    @Override
    public MathFunction derivate()
    {
        double[] c = new double[length-1];
        for (int ii=1;ii<length;ii++)
        {
            c[ii-1] = coef[ii]*ii;
        }
        return new Polynomial(c);
    }

    @Override
    public MathFunction integral()
    {
        double[] c = new double[length+1];
        for (int ii=0;ii<length;ii++)
        {
            c[ii+1] = coef[ii]/(ii+1);
        }
        return new Polynomial(c);
    }
    
}
