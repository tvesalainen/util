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

import java.util.EnumSet;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public abstract class TransactionalSetter implements Transactional
{
    protected enum Prim {Z, B, C, S, I, J, F, D, R};
    protected Object[] arr = new Object[9];
    protected int[][] ord = new int[9][];
    protected int[] ind = new int[9];

    public TransactionalSetter()
    {
        this(5, EnumSet.allOf(Prim.class));
    }

    public TransactionalSetter(int size, EnumSet<Prim> types)
    {
        for (Prim p : types)
        {
            ord[p.ordinal()] = new int[size];
            switch (p)
            {
                case Z:
                    arr[p.ordinal()]= (Object)new boolean[size];
                    break;
                case B:
                    arr[p.ordinal()]= (Object)new byte[size];
                    break;
                case C:
                    arr[p.ordinal()]= (Object)new char[size];
                    break;
                case S:
                    arr[p.ordinal()]= (Object)new short[size];
                    break;
                case I:
                    arr[p.ordinal()]= (Object)new int[size];
                    break;
                case J:
                    arr[p.ordinal()]= (Object)new long[size];
                    break;
                case F:
                    arr[p.ordinal()]= (Object)new float[size];
                    break;
                case D:
                    arr[p.ordinal()]= (Object)new double[size];
                    break;
                case R:
                    arr[p.ordinal()]= new Object[size];
                    break;
            }
        }
    }
    
    public static <T extends TransactionalSetter> T getInstance(Class<T> cls)
    {
        try
        {
            TransactionalSetterClass annotation = cls.getAnnotation(TransactionalSetterClass.class);
            if (annotation == null)
            {
                throw new IllegalArgumentException("@"+TransactionalSetterClass.class.getSimpleName()+" missing in cls");
            }
            Class<?> c = Class.forName(annotation.value());
            return (T) c.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    protected void set(int ordinal, boolean arg)
    {
        int o = Prim.Z.ordinal();
        ord[o][ind[o]] = ordinal;
        ((boolean[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, byte arg)
    {
        int o = Prim.B.ordinal();
        ord[o][ind[o]] = ordinal;
        ((byte[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, char arg)
    {
        int o = Prim.C.ordinal();
        ord[o][ind[o]] = ordinal;
        ((char[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, short arg)
    {
        int o = Prim.S.ordinal();
        ord[o][ind[o]] = ordinal;
        ((short[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, int arg)
    {
        int o = Prim.I.ordinal();
        ord[o][ind[o]] = ordinal;
        ((int[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, long arg)
    {
        int o = Prim.J.ordinal();
        ord[o][ind[o]] = ordinal;
        ((long[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, float arg)
    {
        int o = Prim.F.ordinal();
        ord[o][ind[o]] = ordinal;
        ((float[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, double arg)
    {
        int o = Prim.D.ordinal();
        ord[o][ind[o]] = ordinal;
        ((double[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
    protected void set(int ordinal, Object arg)
    {
        int o = Prim.R.ordinal();
        ord[o][ind[o]] = ordinal;
        ((Object[])arr[o])[ind[o]] = arg;
        ind[o]++;
    }
}
