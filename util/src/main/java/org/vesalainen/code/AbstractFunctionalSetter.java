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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IdentityHashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractFunctionalSetter implements Transactional
{
    public static final MethodHandle NO_OP;
    public static final void noOp(){};
    static
    {
        try
        {
            NO_OP = MethodHandles.lookup().findStatic(AbstractFunctionalSetter.class, "noOp", MethodType.methodType(void.class));
        }
        catch (NoSuchMethodException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static <T extends AbstractFunctionalSetter> T newInstance(Class<T> base)
    {
        try
        {
            Class<?> cls = Class.forName(base.getCanonicalName()+"Impl");
            Constructor<?> constructor = cls.getConstructor();
            return (T) constructor.newInstance();
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private Map<String,MethodHandle> handleSetters = new HashMap<>();
    private MapList<String,MethodHandle> setters = new HashMapList<>();
    private MapList<Object,MethodHandle> undoSetters = new IdentityHashMapList<>();
    private Map<String,Class<?>> types = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    
    public AbstractFunctionalSetter()
    {
        Class<? extends AbstractFunctionalSetter> cls = this.getClass();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try
        {
            for (Field field : cls.getDeclaredFields())
            {
                String name = field.getName();
                Class<?> type = field.getType();
                if (type.isArray())
                {
                    types.put(name, type.getComponentType());
                }
                else
                {
                    name = name.substring(0, name.length()-6);
                    MethodHandle mhs = lookup.unreflectSetter(field);
                    MethodHandle setter = mhs.bindTo(this);
                    handleSetters.put(name, setter);
                }
            }
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    public void addObserver(PropertySetter setter)
    {
        lock.lock();
        try
        {
            for (String property : setter.getPrefixes())
            {
                Class<?> type = types.get(property);
                if (type == null)
                {
                    throw new IllegalArgumentException(property+" not found");
                }
                if (type.isPrimitive())
                {
                    try
                    {
                        MethodHandle mh = MethodHandles.lookup().findVirtual(setter.getClass(), "set", MethodType.methodType(void.class, String.class, type));
                        MethodHandle bound = MethodHandles.insertArguments(mh, 0, setter, property);
                        setters.add(property, bound);
                        undoSetters.add(setter, bound);
                        compile(property);
                    }
                    catch (NoSuchMethodException | IllegalAccessException ex)
                    {
                        throw new IllegalArgumentException(ex);
                    }
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    public void removeObserver(PropertySetter setter)
    {
        lock.lock();
        try
        {
            List<MethodHandle> list = undoSetters.get(setter);
            if (!list.isEmpty())
            {
                for (String property : setter.getPrefixes())
                {
                    List<MethodHandle> mhs = setters.get(property);
                    if (mhs != null && mhs.removeAll(list))
                    {
                        compile(property);
                    }
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void compile(String property)
    {
        try
        {
            MethodHandle methodSetter = handleSetters.get(property);
            List<MethodHandle> list = setters.get(property);
            MethodHandle setter = NO_OP;
            if (list != null)
            {
                for (MethodHandle mh : list)
                {
                    if (setter == NO_OP)
                    {
                        setter = mh;
                    }
                    else
                    {
                        setter = MethodHandles.foldArguments(setter, mh);
                    }
                }
            }
            methodSetter.invokeExact(setter);
        }
        catch (Throwable ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void start(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rollback(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commit(String reason)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
