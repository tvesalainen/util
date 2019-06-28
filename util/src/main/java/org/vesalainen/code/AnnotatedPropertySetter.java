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
package org.vesalainen.code;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AnnotatedPropertySetter extends JavaLogging implements PropertySetter, Transactional
{
    private Map<String,MethodHandle> map = new HashMap<>();

    public AnnotatedPropertySetter()
    {
        super(AnnotatedPropertySetter.class);
        try
        {
            Class<? extends AnnotatedPropertySetter> cls = this.getClass();
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            for (Field field : cls.getDeclaredFields())
            {
                Property property = field.getDeclaredAnnotation(Property.class);
                if (property != null)
                {
                    String name = property.value();
                    if (name.isEmpty())
                    {
                        name = field.getName();
                    }
                    MethodHandle mh = lookup.unreflectSetter(field);
                    MethodType mt = MethodType.methodType(void.class, AnnotatedPropertySetter.class, field.getType());
                    MethodHandle setter = mh.asType(mt);
                    map.put(name, setter);
                }
            }
            for (Method method : cls.getDeclaredMethods())
            {
                Property property = method.getDeclaredAnnotation(Property.class);
                if (property != null)
                {
                    String name = property.value();
                    if (name.isEmpty())
                    {
                        name = BeanHelper.getProperty(method);
                    }
                    MethodHandle mh = lookup.unreflect(method);
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1)
                    {
                        throw new IllegalArgumentException(method+" is not setter");
                    }
                    MethodType mt = MethodType.methodType(void.class, AnnotatedPropertySetter.class, parameterTypes[0]);
                    MethodHandle setter = mh.asType(mt);
                    map.put(name, setter);
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Override
    public String[] getPrefixes()
    {
        return map.keySet().toArray(new String[map.size()]);
    }

    @Override
    public void set(String property, boolean arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, char arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, short arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, int arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, long arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, float arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, double arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        MethodHandle mh = map.get(property);
        if (mh != null)
        {
            try
            {
                mh.invoke(this, arg);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, " with %s", property);
            }
        }
        else
        {
            warning("%s has no method handler", property);
        }
    }

}
