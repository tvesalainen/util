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

package org.vesalainen.code;

import java.util.Arrays;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public abstract class AbstractDispatcher implements Transactional
{
    protected final int Size = JavaType.values().length;
    protected Object[] arr = new Object[Size];
    protected int[][] ord = new int[Size][];
    protected int[] ind = new int[Size];

    protected AbstractDispatcher(int[] sizes)
    {
        if (sizes.length != Size)
        {
            throw new IllegalArgumentException("sizes illegal length");
        }
        for (int ii=0;ii<Size;ii++)
        {
            if (sizes[ii] > 0)
            {
                ord[ii] = new int[sizes[ii]];
                JavaType jt = JavaType.values()[ii];
                switch (jt)
                {
                    case BOOLEAN:
                        arr[ii]= (Object)new boolean[sizes[ii]];
                        break;
                    case BYTE:
                        arr[ii]= (Object)new byte[sizes[ii]];
                        break;
                    case CHAR:
                        arr[ii]= (Object)new char[sizes[ii]];
                        break;
                    case SHORT:
                        arr[ii]= (Object)new short[sizes[ii]];
                        break;
                    case INT:
                        arr[ii]= (Object)new int[sizes[ii]];
                        break;
                    case LONG:
                        arr[ii]= (Object)new long[sizes[ii]];
                        break;
                    case FLOAT:
                        arr[ii]= (Object)new float[sizes[ii]];
                        break;
                    case DOUBLE:
                        arr[ii]= (Object)new double[sizes[ii]];
                        break;
                    case DECLARED:
                        arr[ii]= new Object[sizes[ii]];
                        break;
                }
            }
        }
    }

    protected void clear()
    {
        Arrays.fill(ind, 0);
    }

    protected void set(int ordinal, boolean arg)
    {
        int o = JavaType.BOOLEAN.ordinal();
        ord[o][ind[o]] = ordinal;
        ((boolean[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, byte arg)
    {
        int o = JavaType.BYTE.ordinal();
        ord[o][ind[o]] = ordinal;
        ((byte[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, char arg)
    {
        int o = JavaType.CHAR.ordinal();
        ord[o][ind[o]] = ordinal;
        ((char[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, short arg)
    {
        int o = JavaType.SHORT.ordinal();
        ord[o][ind[o]] = ordinal;
        ((short[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, int arg)
    {
        int o = JavaType.INT.ordinal();
        ord[o][ind[o]] = ordinal;
        ((int[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, long arg)
    {
        int o = JavaType.LONG.ordinal();
        ord[o][ind[o]] = ordinal;
        ((long[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, float arg)
    {
        int o = JavaType.FLOAT.ordinal();
        ord[o][ind[o]] = ordinal;
        ((float[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, double arg)
    {
        int o = JavaType.DOUBLE.ordinal();
        ord[o][ind[o]] = ordinal;
        ((double[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }

    protected void set(int ordinal, Object arg)
    {
        int o = JavaType.DECLARED.ordinal();
        ord[o][ind[o]] = ordinal;
        ((Object[]) arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    
}
