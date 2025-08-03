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
package org.vesalainen.math.matrix;

import java.util.HashMap;
import java.util.Map;
import org.vesalainen.math.MoreMath;

/**
 * A permutation matrix. Each row is permutation. Odd rows have + sign. Positions
 * start with 0.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PermutationMatrix extends ByteMatrix
{
    private static final Map<Integer,PermutationMatrix> map = new HashMap<>();
    
    protected PermutationMatrix(int n)
    {
        super(MoreMath.factorial(n), n);
        generate(n);
        this.consumer = null;   // make sure this is immutable
    }

    @Override
    public void swapRows(int r1, int r2, byte[] tmp)
    {
        throw new UnsupportedOperationException("not supported");
    }
    /**
     * Returns immutable PermutationMatrix possibly from cache.
     * @param n
     * @return 
     */
    public static PermutationMatrix getInstance(int n)
    {
        PermutationMatrix pm = map.get(n);
        if (pm == null)
        {
            pm = new PermutationMatrix(n);
            map.put(n, pm);
        }
        return pm;
    }
    private void generate(int n)
    {
        int[] array = new int[n];
        int i = 0;
        int j = 0;
        for (int ii=0;ii<n;ii++)
        {
            array[ii] = ii;
        }
        setRow(j++, array, 0);
        int[] c = new int[n];
        while (i < n)
        {
            if (c[i] < i)
            {
                if (isEven(i))
                {
                    swap(array, 0, i);
                }
                else
                {
                    swap(array, c[i], i);
                }
                setRow(j++, array, 0);
                c[i] += 1;
                i = 0;
            }
            else
            {
                //Calling generate(i+1, array) has ended as the for-loop terminated. Reset the state and simulate popping the stack by incrementing the pointer.
                c[i] = 0;
                i += 1;
            }
        }
    }
    private void swap(int[] array, int i, int j)
    {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private boolean isEven(int a)
    {
        return a % 2 == 0;
    }

}
