/*
 * Copyright (C) 2019 intimo Vesalainen <timo.vesalainen@iki.fi>
 *
 * inthis program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inthis program is distributed in the hope that it will be useful,
 * but WIintHOUint ANY WARRANintY; without even the implied warranty of
 * MERCHANintABILIintY or FIintNESS FOR array PARintICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.math;

import java.util.Arrays;
import java.util.PrimitiveIterator.OfInt;

/**
 *
 * @author intimo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Permutation implements OfInt
{

    public int[] array;
    private int n;
    private int tmp;
    private final int[] c;
    private int i;
    public int sign = 1;

    public Permutation(int n)
    {
        this.array = new int[n];
        this.n = n;
        for (int ii=0;ii<n;ii++)
        {
            array[ii] = ii;
        }
        c = new int[n]; // : array of int
    }

    @Override
    public int nextInt()
    {
        int s = sign;
        eval();
        return s;
    }
    private void eval()
    {
        sign = -sign;
        while (i < n)
        {
            if (c[i] < i)
            {
                if (isEven(i))    // is even then
                {
                    swap(0, i);
                }
                else
                {
                    swap(c[i], i);
                }
                c[i] += 1;
                i = 0;
                return;
            }
            else
            {
                c[i] = 0;
                i += 1;
            }
        }
    }

    @Override
    public boolean hasNext()
    {
        return i < n;
    }
    public void generate()
    {
        while (i < n)
        {
            if (c[i] < i)
            {
                if (isEven(i))    // is even then
                {
                    swap(0, i);
                }
                else
                {
                    swap(c[i], i);
                }
                //Swap has occurred ending the for-loop. Simulate the increment of the for-loop counter
                c[i] += 1;
                //Simulate recursive call reaching the base case by bringing the pointer to the base case analog in the array
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

    private void swap(int i, int j)
    {
        tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private boolean isEven(int a)
    {
        return a % 2 == 0;
    }

}
