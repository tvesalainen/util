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
package org.vesalainen.vfs.pm.rpm;

import org.vesalainen.vfs.pm.Condition;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RPMDependency
{
    public static final int LESS = 0x02;
    public static final int GREATER = 0x04;
    public static final int EQUAL = 0x08;	 
    public static final int PREREQ = 0x40;	 
    public static final int INTERP = 0x100;	 
    public static final int SCRIPT_PRE = 0x200;
    public static final int SCRIPT_POST = 0x400;	 
    public static final int SCRIPT_PREUN = 0x800;	 
    public static final int SCRIPT_POSTUN = 0x1000;	 
    public static final int RPMLIB = 0x1000000;
    
    public static Condition[] toArray(int flags)
    {
        Condition[] array = new Condition[Integer.bitCount(flags)];
        int index = 0;
        if ((flags & 0x08) != 0)
        {
            array[index++] = Condition.EQUAL;
        }
        if ((flags & 0x02) != 0)
        {
            array[index++] = Condition.LESS;
        }
        if ((flags & 0x04) != 0)
        {
            array[index++] = Condition.GREATER;
        }
        if (index != array.length)
        {
            throw new IllegalArgumentException(flags+" illegal flags");
        }
        return array;
    }
    public static int or(Condition... flags)
    {
        int flag = 0;
        for (Condition f : flags)
        {
            switch (f)
            {
                case EQUAL:
                    flag |= 0x08;
                    break;
                case LESS:
                    flag |= 0x02;
                    break;
                case GREATER:
                    flag |= 0x04;
                    break;
            }
        }
        return flag;
    }
    public static int or(int... flags)
    {
        int flag = 0;
        for (int f : flags)
        {
            check(f);
            flag |= f;
        }
        return flag;
    }
    private static void check(int flag)
    {
        switch (flag)
        {
            case LESS:
            case GREATER:
            case EQUAL:
            case PREREQ:
            case INTERP:
            case SCRIPT_PRE:
            case SCRIPT_POST:
            case SCRIPT_PREUN:
            case SCRIPT_POSTUN:
            case RPMLIB:
                break;
            default:
                throw new UnsupportedOperationException(flag+" is unsupported");
        }
    }
}
