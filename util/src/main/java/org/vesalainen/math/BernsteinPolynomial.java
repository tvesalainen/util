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

import java.util.function.DoubleUnaryOperator;
import static org.vesalainen.math.MoreMath.factorial;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BernsteinPolynomial
{
    /**
     * Returns Bernstein polynomial
     * @param n
     * @param m
     * @return 
     */
    public static DoubleUnaryOperator b(int n, int m)
    {
        return (t)->c(n,m)*Math.pow(t, m)*Math.pow(1-t, n-m);
    }
    
    static int c(int n, int m)
    {
        return factorial(n)/(factorial(m)*factorial(n-m));
    }
}
