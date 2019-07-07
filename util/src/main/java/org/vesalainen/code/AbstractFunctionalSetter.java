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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IdentityArraySet;
import org.vesalainen.util.IdentityHashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractFunctionalSetter implements Transactional
{
    public static final void noOp(boolean arg){};
    public static final void noOp(byte arg){};
    public static final void noOp(char arg){};
    public static final void noOp(short arg){};
    public static final void noOp(int arg){};
    public static final void noOp(long arg){};
    public static final void noOp(float arg){};
    public static final void noOp(double arg){};
    public static final <T> void noOp(T arg){};

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

    protected int VERSION;
    protected List<String> TRANSACTION_PROPERTIES = new ArrayList<>();
    protected Set<Transactional> TRANSACTION_TARGETS = new IdentityArraySet<>();
    private MethodHandle transactionAdder;
    private Map<String,MethodHandle> handleSetters = new HashMap<>();
    private Map<String,MethodHandle> savers = new HashMap<>();
    private Map<String,MethodHandle> arrayGetters = new HashMap<>();
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
            Field versionField = AbstractFunctionalSetter.class.getDeclaredField("VERSION");
            MethodHandle vfg = lookup.unreflectGetter(versionField);
            MethodHandle versionFieldGetter = vfg.bindTo(this);
            
            MethodHandle addObject = lookup.findVirtual(Collection.class, "add", MethodType.methodType(boolean.class, Object.class));
            MethodHandle addString = addObject.asType(MethodType.methodType(void.class, List.class, String.class));
            transactionAdder = addString.bindTo(TRANSACTION_PROPERTIES);  // List.add(String) void

            for (Field field : cls.getDeclaredFields())
            {
                String property = field.getName();
                if (!"VERSION".equals(property))
                {
                    Class<?> type = field.getType();
                    if (type.isArray())
                    {
                        types.put(property, type.getComponentType());
                        Object value = Array.newInstance(type.getComponentType(), 2);
                        field.set(this, value);
                        MethodHandle aes = MethodHandles.arrayElementSetter(type);
                        MethodHandle bound = aes.bindTo(value);
                        savers.put(property, MethodHandles.foldArguments(bound, versionFieldGetter));
                        MethodHandle aeg = MethodHandles.arrayElementGetter(type);
                        MethodHandle arrayGetter = aeg.bindTo(value);
                        arrayGetters.put(property, arrayGetter);
                    }
                    else
                    {
                        property = property.substring(0, property.length()-6);
                        MethodHandle mhs = lookup.unreflectSetter(field);
                        MethodHandle setter = mhs.bindTo(this);
                        handleSetters.put(property, setter);
                    }
                }
            }
            for (String property : handleSetters.keySet())
            {
                assignMethod(property, null);
            }
        }
        catch (Throwable ex)
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
                try
                {
                    MethodHandle mh;
                    if (type.isPrimitive())
                    {
                        mh = MethodHandles.lookup().findVirtual(setter.getClass(), "set", MethodType.methodType(void.class, String.class, type));
                    }
                    else
                    {
                        mh = MethodHandles.lookup().findVirtual(setter.getClass(), "set", MethodType.methodType(void.class, String.class, Object.class));
                        mh = mh.asType(MethodType.methodType(void.class, setter.getClass(), String.class, type));
                    }
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
            List<MethodHandle> list = setters.get(property);
            MethodHandle setter = null;
            if (list != null)
            {
                for (MethodHandle mh : list)
                {
                    if (setter == null)
                    {
                        setter = mh;
                    }
                    else
                    {
                        setter = MethodHandles.foldArguments(setter, mh);
                    }
                }
            }
            assignMethod(property, setter);
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

    private void assignMethod(String property, MethodHandle setter) throws Throwable
    {
        MethodHandle methodSetter = handleSetters.get(property);
        if (setter == null)
        {
            Class<?> type = types.get(property);
            if (type.isPrimitive())
            {
                setter = MethodHandles.lookup().findStatic(AbstractFunctionalSetter.class, "noOp", MethodType.methodType(void.class, type));
            }
            else
            {
                setter = MethodHandles.lookup().findStatic(AbstractFunctionalSetter.class, "noOp", MethodType.methodType(void.class, Object.class));
                setter = setter.asType(MethodType.methodType(void.class, type));
            }
        }
        MethodHandle saver = savers.get(property);
        MethodHandle folded = MethodHandles.foldArguments(setter, transactionAdder.bindTo(property));
        MethodHandle folded2 = MethodHandles.foldArguments(folded, saver);
        methodSetter.invokeExact(folded2);
    }
    
}
