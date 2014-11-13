/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.math.Matrices.RowComparator;

/**
 *
 * @author Timo Vesalainen
 */
public class MatricesTest
{

    public MatricesTest()
    {
    }

    /**
     * Test of sort method, of class MatrixSort.
     */
    @Test
    public void testSort1()
    {
        double[] arr1 = new double[]
        {
            1, 12, 5, 26, 7, 14, 3, 7, 2, 23, 56
        };
        double[] arr2 = new double[arr1.length];
        System.arraycopy(arr1, 0, arr2, 0, arr1.length);
        Matrices.sort(arr1, 1, new C());
        Arrays.sort(arr2);
        assertTrue(Arrays.equals(arr2, arr1));
    }

    @Test
    public void testSort2()
    {
        double[] arr1 = new double[1000];
        double[] arr2 = new double[arr1.length];
        Random r = new Random(1234567L);
        for (int ii = 0; ii < arr1.length; ii++)
        {
            arr1[ii] = r.nextDouble();
        }
        System.arraycopy(arr1, 0, arr2, 0, arr1.length);
        Matrices.sort(arr1, 1, new C());
        Arrays.sort(arr2);
        assertTrue(Arrays.equals(arr2, arr1));
    }

    @Test
    public void testSort3()
    {
        double[] arr1 = new double[]
        {
            10, 9, 8, 8, 7, 6, 5, 3, 3, 4, 1
        };
        double[] arr2 = new double[arr1.length];
        System.arraycopy(arr1, 0, arr2, 0, arr1.length);
        Matrices.sort(arr1, 1, new C());
        Arrays.sort(arr2);
        assertTrue(Arrays.equals(arr2, arr1));
    }

    public class C implements RowComparator
    {

        @Override
        public int compare(double[] data, int row, double pivot[], int len)
        {
            if (data[row] < pivot[0])
            {
                return -1;
            }
            else
            {
                if (data[row] == pivot[0])
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        }

    }

}
