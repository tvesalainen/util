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
package org.vesalainen.lang.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class MethodHelp
{
    public static Method getAssignableMethod(Class<?> cls, String name, Class<?>... types) throws NoSuchMethodException
    {
        List<Method> list = new ArrayList<>();
        for (Method method : cls.getMethods())
        {
            if (method.getName().equals(name))
            {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (types.length == parameterTypes.length)
                {
                    boolean ok = true;
                    for (int ii=0;ii<types.length;ii++)
                    {
                        if (!parameterTypes[ii].isAssignableFrom(types[ii]))
                        {
                            ok = false;
                            break;
                        }
                    }
                    if (ok)
                    {
                        list.add(method);
                    }
                }
            }
        }
        switch (list.size())
        {
            case 0:
                throw new NoSuchMethodException();
            default:
                list.sort(new Comp());
            case 1:
                return list.get(0);
        }
    }
    private static class Comp implements Comparator<Method>
    {

        @Override
        public int compare(Method o1, Method o2)
        {
            Class<?>[] p1 = o1.getParameterTypes();
            Class<?>[] p2 = o2.getParameterTypes();
            for (int ii=0;ii<p1.length;ii++)
            {
                if (!p1[ii].equals(p2[ii]))
                {
                    if (p1[ii].isAssignableFrom(p2[ii]))
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
            }
            throw new IllegalArgumentException("should not came here!");
        }
        
    }
}
