/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class ArrayHelp
{
    public static final Object[] EMPTY = new Object[]{};
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final boolean[] concat(boolean[] array, boolean item)
    {
        boolean[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final byte[] concat(byte[] array, byte item)
    {
        byte[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final short[] concat(short[] array, short item)
    {
        short[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final char[] concat(char[] array, char item)
    {
        char[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final int[] concat(int[] array, int item)
    {
        int[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final long[] concat(long[] array, long item)
    {
        long[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final float[] concat(float[] array, float item)
    {
        float[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param array
     * @param item
     * @return 
     */
    public static final double[] concat(double[] array, double item)
    {
        double[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * Returns new array which contains given array and item.
     * @param <T>
     * @param array
     * @param item
     * @return 
     */
    public static final <T> T[] concat(T[] array, T item)
    {
        T[] copyOf = Arrays.copyOf(array, array.length+1);
        copyOf[array.length] = item;
        return copyOf;
    }
    /**
     * returns true if 2-dimensional points x-coordinates are ordered ascending.
     * @param array
     * @return 
     */
    public static boolean arePointsInXOrder(double[] array)
    {
        return inOrder(array, 0, 2);
    }
    /**
     * Returns true if every rows k's number is not greater than next rows.
     * @param array
     * @param k
     * @param cols
     * @return 
     */
    public static boolean inOrder(double[] array, int k, int cols)
    {
        if (array.length % cols != 0)
        {
            throw new IllegalArgumentException("row length dont match");
        }
        double prev = array[k];
        int len = array.length / cols;
        for (int ii=1;ii<len;ii++)
        {
            double next = array[ii*cols+k];
            if (next < prev)
            {
                return false;
            }
            prev = next;
        }
        return true;
    }
    public static final RowComparator NATURAL_ROW_COMPARATOR = new NaturalRowComparator();
    /**
     * Sorts rows in 1D array in ascending order comparing each rows column.
     * @param data
     * @param rowLength 
     */
    public static void sort(Object data, int rowLength)
    {
        sort(data, 0, Array.getLength(data), rowLength);
    }
    /**
     * Sorts rows in 1D array in ascending order comparing each rows column.
     * @param data
     * @param offset
     * @param length Length of data
     * @param rowLength 
     */
    public static void sort(Object data, int offset, int length, int rowLength)
    {
        if (offset % rowLength != 0)
        {
            throw new IllegalArgumentException("offset not dividable by rowLength");
        }
        if (length % rowLength != 0)
        {
            throw new IllegalArgumentException("length not dividable by rowLength");
        }
        quickSort(data, offset/rowLength, (offset + length - 1)/rowLength, rowLength, NATURAL_ROW_COMPARATOR, new double[rowLength], new double[rowLength]);
    }

    /**
     * Sorts rows in 1D array using given comparator.
     * @param data
     * @param rowLength
     * @param comparator 
     */
    public static void sort(Object data, int rowLength, RowComparator comparator)
    {
        sort(data, 0, Array.getLength(data), rowLength, comparator);
    }
    /**
     * Sorts rows in 1D array using given comparator.
     * @param data
     * @param length Length of data
     * @param rowLength
     * @param comparator 
     */
    public static void sort(Object data, int offset, int length, int rowLength, RowComparator comparator)
    {
        if (offset % rowLength != 0)
        {
            throw new IllegalArgumentException("offset not dividable by rowLength");
        }
        if (length % rowLength != 0)
        {
            throw new IllegalArgumentException("length not dividable by rowLength");
        }
        quickSort(data, offset/rowLength, (offset + length - 1)/rowLength, rowLength, comparator, new double[rowLength], new double[rowLength]);
    }

    public static void quickSort(Object arr, int left, int right, int len, RowComparator c, Object pivot, Object tmp)
    {
        int i = left, j = right;
        System.arraycopy(arr, ((left + right) / 2)*len, pivot, 0, len);

        /* partition */
        while (i <= j)
        {
            while (c.compare(arr, i, pivot, len) < 0)
            {
                i++;
            }
            while (c.compare(arr, j, pivot, len) > 0)
            {
                j--;
            }
            if (i <= j)
            {
                if (i != j)
                {
                    System.arraycopy(arr, i*len, tmp, 0, len);
                    System.arraycopy(arr, j*len, arr, i*len, len);
                    System.arraycopy(tmp, 0, arr, j*len, len);
                }
                i++;
                j--;
            }
        };

        /* recursion */
        if (left < j)
        {
            quickSort(arr, left, j, len, c, pivot, tmp);
        }
        if (i < right)
        {
            quickSort(arr, i, right, len, c, pivot, tmp);
        }
    }
    /**
     * Returns true if rows r1 and r2 equal
     * @param a
     * @param r1
     * @param r2
     * @return 
     */
    public static boolean rowsEqual(Object a, int r1, int r2)
    {
        return compareRows(a, r1, a, r2) == 0;
    }
    /**
     * Returns true if array a1 row r1 equals a2 r2
     * @param a1
     * @param r1
     * @param a2
     * @param r2
     * @return 
     */
    public static boolean rowsEqual(Object a1, int r1, Object a2, int r2)
    {
        return compareRows(a1, r1, a2, r2) == 0;
    }
    /**
     * Compares rows r1 and r2 of array a
     * @param a
     * @param r1
     * @param r2
     * @return 
     */
    public static int compareRows(Object a, int r1, int r2)
    {
        return compareRows(a, r1, a, r2);
    }
    /**
     * Compares array a1 row r1 to a2 r2
     * @param a1
     * @param r1
     * @param a2
     * @param r2
     * @return 
     */
    public static int compareRows(Object a1, int r1, Object a2, int r2)
    {
        Class<?> c1 = a1.getClass();
        if (!c1.isArray())
        {
            throw new IllegalArgumentException(a1+" not array");
        }
        Class<?> c2 = a2.getClass();
        if (c1 != c2)
        {
            throw new IllegalArgumentException("incompatible arrays");
        }
        Class<?> componentType = c1.getComponentType();
        String simpleName = componentType.getSimpleName();
        switch (simpleName)
        {
            case "boolean":
                    return Boolean.compare(Array.getBoolean(a1, r1), Array.getBoolean(a2, r2));
            case "byte":
                    return Byte.compare(Array.getByte(a1, r1), Array.getByte(a2, r2));
            case "short":
                    return Short.compare(Array.getShort(a1, r1), Array.getShort(a2, r2));
            case "char":
                    return Character.compare(Array.getChar(a1, r1), Array.getChar(a2, r2));
            case "int":
                    return Integer.compare(Array.getInt(a1, r1), Array.getInt(a2, r2));
            case "long":
                    return Long.compare(Array.getLong(a1, r1), Array.getLong(a2, r2));
            case "float":
                    return Float.compare(Array.getFloat(a1, r1), Array.getFloat(a2, r2));
            case "double":
                    return Double.compare(Array.getDouble(a1, r1), Array.getDouble(a2, r2));
            default:
                if (componentType.isAssignableFrom(Comparable.class))
                {
                    Comparable co1 = (Comparable)Array.get(a1, r1);
                    Comparable co2 = (Comparable)Array.getDouble(a2, r2);
                    return co1.compareTo(co2);
                }
                else
                {
                    throw new IllegalArgumentException("array types not comparable");
                }
        }
    }
    public static class NaturalRowComparator implements RowComparator
    {

        @Override
        public int compare(Object data, int row, Object pivot, int len)
        {
            for (int ii=0;ii<len;ii++)
            {
                int compare = compareRows(data, len*row+ii, pivot, ii);
                if (compare != 0)
                {
                    return compare;
                }
            }
            return 0;
        }
        
    }
    public interface RowComparator
    {
        /**
         * Compares sub array of data to pivot array.
         * 
         * <p>Returns -1 if data[row*len,...,(row+1)*len-1] &lt; pivot.
         * <p>Returns 1 if data[row*len,...,(row+1)*len-1] &gt; pivot.
         * <p>Returns 0 if data[row*len,...,(row+1)*len-1] == pivot.
         * @param data
         * @param row
         * @param pivot
         * @param len
         * @return 
         */
        int compare(Object data, int row, Object pivot, int len);
    }
    public static final <T> T[] flatten(T[][] m)
    {
        int rows = m.length;
        int cols = m[0].length;
        int len = rows*cols;
        T[] arr = (T[]) Array.newInstance(m[0][0].getClass(), len);
        for (int ii=0;ii<len;ii++)
        {
            arr[ii] = m[ii/cols][ii%cols];
        }
        return arr;
    }
    public static final <T> T[][] unFlatten(int rows, T... arr)
    {
        if (arr.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        int cols = arr.length/rows;
        T[][] m = (T[][]) Array.newInstance(arr[0].getClass(), rows, cols);
        for (int ii=0;ii<rows;ii++)
        {
            m[ii] = Arrays.copyOfRange(arr, ii*cols, (ii+1)*cols);
        }
        return m;
    }
    public static final double[] flatten(double[][] m)
    {
        int rows = m.length;
        int cols = m[0].length;
        int len = rows*cols;
        double[] arr = new double[len];
        for (int ii=0;ii<len;ii++)
        {
            arr[ii] = m[ii/cols][ii%cols];
        }
        return arr;
    }
    public static final double[][] unFlatten(int rows, double... arr)
    {
        if (arr.length % rows != 0)
        {
            throw new IllegalArgumentException("not full rows");
        }
        int cols = arr.length/rows;
        double[][] m = new double[rows][];
        for (int ii=0;ii<rows;ii++)
        {
            m[ii] = Arrays.copyOfRange(arr, ii*cols, (ii+1)*cols);
        }
        return m;
    }
    public static final int indexOf(boolean[] array, boolean item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(byte[] array, byte item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(short[] array, short item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(char[] array, char item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(int[] array, int item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(long[] array, long item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(float[] array, float item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(double[] array, double item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii] == item)
            {
                return ii;
            }
        }
        return -1;
    }
    public static final int indexOf(Object[] array, Object item)
    {
        int len = array.length;
        for (int ii=0;ii<len;ii++)
        {
            if (array[ii].equals(item))
            {
                return ii;
            }
        }
        return -1;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(byte[] array, byte item)
    {
        for (byte b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(char[] array, char item)
    {
        for (char b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(short[] array, short item)
    {
        for (short b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(int[] array, int item)
    {
        for (int b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members is item
     * @param array
     * @param item
     * @return 
     */
    public static final boolean contains(long[] array, long item)
    {
        for (long b : array)
        {
            if (b == item)
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns true if one of arr members equals item
     * @param <T>
     * @param array
     * @param item
     * @return 
     */
    public static final <T> boolean contains(T[] array, T item)
    {
        for (T b : array)
        {
            if (b.equals(item))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(T[] array, T... items)
    {
        for (T b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(byte[] array, T... items)
    {
        for (byte b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(char[] array, T... items)
    {
        for (char b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(short[] array, T... items)
    {
        for (short b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(int[] array, T... items)
    {
        for (int b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
    /**
     * Throws UnsupportedOperationException if one of array members is not one
     * of items
     * @param <T>
     * @param array
     * @param items
     * @return 
     */
    public static final <T> boolean containsOnly(long[] array, T... items)
    {
        for (long b : array)
        {
            if (!contains(items, b))
            {
                return false;
            }
        }
        return true;
    }
}
