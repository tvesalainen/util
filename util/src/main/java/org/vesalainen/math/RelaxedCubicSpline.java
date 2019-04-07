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
public class RelaxedCubicSpline extends AbstractCubicSpline
{

    public RelaxedCubicSpline(double... points)
    {
        super(points);
    }

    @Override
    protected Matrix createMatrix(int n)
    {
        Matrix m = Matrix.getInstance(n, n);
        m.set(0, 0, 6);
        for (int i=1;i<n-1;i++)
        {
            m.set(i, i-1, 1);
            m.set(i, i, 4);
            m.set(i, i+1, 1);
        }
        m.set(n-1, n-1, 6);
        m.scalarMultiply(1.0/6.0);
        return m;
    }
    
}
