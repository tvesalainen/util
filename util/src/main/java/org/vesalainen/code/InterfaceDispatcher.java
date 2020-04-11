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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IdentityArraySet;
import org.vesalainen.util.IdentityHashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * InterfaceDispatcher implements fast dispatching of properties to
 * AnnotatedPropertyStore or PropertySetter. If those targets implement
 * Transactional the transactions are handled. Rollback will restore property
 * values to last committed values.
 * 
 * <p>Usage: @InterfaceDispatcherAnnotation public class MyClass extends InterfaceDispatcher implements MyInterface
 * <p>Compiler will generate MyClassImpl which you can get from MyClass.getInstance(MyClass.class).
 * Calling any of those interface methods will have no effect before you
 * add observer.
 * <p>If observer implements Transactional frame set methods with transaction method.
 * start() setx(x) setY(y) commit(). You can call rollback() instead of commit()
 * which will restore properties to values after last commit.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class InterfaceDispatcher extends JavaLogging implements Transactional
{
    private static void noOp(boolean arg){};
    private static void noOp(byte arg){};
    private static void noOp(char arg){};
    private static void noOp(short arg){};
    private static void noOp(int arg){};
    private static void noOp(long arg){};
    private static void noOp(float arg){};
    private static void noOp(double arg){};
    private static <T> void noOp(T arg){};
    /**
     * Convenience method to get access to generated class. 
     * @param <T>
     * @param base
     * @return 
     */
    public static <T extends InterfaceDispatcher> T newInstance(Class<T> base)
    {
        if (base.getDeclaredAnnotation(InterfaceDispatcherAnnotation.class) == null)
        {
            throw new IllegalArgumentException("no @InterfaceDispatcherAnnotation present");
        }
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

    protected final List<String> TRANSACTION_PROPERTIES = new ArrayList<>();
    private final List<String> unmodifiableTransactionProperties = Collections.unmodifiableList(TRANSACTION_PROPERTIES);
    private final Set<Transactional> transactionTargets = new IdentityArraySet<>();
    private final MethodHandle transactionAdder;
    private boolean transaction;
    private final Map<String,MethodHandle> handleSetters = new HashMap<>();
    private final Map<String,MethodHandle> savers = new HashMap<>();
    private final Map<String,MethodHandle> arrayGetters = new HashMap<>();
    private final MapList<String,MethodHandle> setters = new HashMapList<>();
    private final MapList<Object,MethodHandle> undoSetters = new IdentityHashMapList<>();
    private final Map<String,Class<?>> types = new HashMap<>();
    private final MapList<String,Transactional> transactionalProperties = new HashMapList<>();
    private final Map<String,MethodHandle> versionSetters = new HashMap<>();
    private final Map<String,MethodHandle> versionGetters = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final MethodHandle setVersion;
    
    public InterfaceDispatcher(Lookup lookup)
    {
        super(InterfaceDispatcher.class);
        Class<? extends InterfaceDispatcher> cls = this.getClass();
        //Lookup lookup = MethodHandles.lookup();
        try
        {
            setVersion = lookup.findStatic(InterfaceDispatcher.class, "version", MethodType.methodType(int.class, int.class));

            MethodHandle addObject = lookup.findVirtual(Collection.class, "add", MethodType.methodType(boolean.class, Object.class));
            MethodHandle addString = addObject.asType(MethodType.methodType(void.class, List.class, String.class));
            transactionAdder = addString.bindTo(TRANSACTION_PROPERTIES);  // List.add(String) void

            for (Field field : cls.getDeclaredFields())
            {
                String property = field.getName();
                Class<?> type = field.getType();
                if (type.isArray())
                {
                    // handle
                    Field handleField = cls.getDeclaredField(property+"Handle");
                    MethodHandle mhhs = lookup.unreflectSetter(handleField);
                    MethodHandle handleSetter = mhhs.bindTo(this);
                    handleSetters.put(property, handleSetter);
                    // version
                    Field versionField = cls.getDeclaredField(property+"Version");
                    MethodHandle vfg = lookup.unreflectGetter(versionField);
                    MethodHandle versionGetter = vfg.bindTo(this);
                    versionGetters.put(property, versionGetter);
                    // version toggle
                    MethodHandle mhvs = lookup.unreflectSetter(versionField);
                    MethodHandle mhvsb = mhvs.bindTo(this);
                    MethodHandle mh1 = MethodHandles.foldArguments(setVersion, versionGetter);
                    MethodHandle versionSetter = MethodHandles.foldArguments(mhvsb, mh1);
                    versionSetters.put(property, versionSetter);
                    // array init
                    types.put(property, type.getComponentType());
                    Object value = Array.newInstance(type.getComponentType(), 2);
                    MethodHandle mhas = lookup.unreflectSetter(field);
                    mhas.invoke(this, value);
                    // array setter
                    MethodHandle aes = MethodHandles.arrayElementSetter(type);
                    MethodHandle arraySetter = aes.bindTo(value);
                    MethodHandle saver = MethodHandles.foldArguments(arraySetter, versionGetter);
                    savers.put(property, saver);
                    // array getter
                    MethodHandle aeg = MethodHandles.arrayElementGetter(type);
                    MethodHandle arrayGetter = aeg.bindTo(value);
                    arrayGetters.put(property, arrayGetter);
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
    protected static int version(int v)
    {
        return v == 0 ? 1 : 0;
    }
    public void addObserver(AnnotatedPropertyStore aps)
    {
        addObserver(aps, true);
    }
    public void addObserver(AnnotatedPropertyStore aps, boolean reportMissingProperties)
    {
        lock.lock();
        try
        {
            Transactional transactional = null;
            if (aps instanceof Transactional)
            {
                transactional = (Transactional) aps;
            }
            Map<String, MethodHandle> map = aps.getSetters();
            for (String property : map.keySet())
            {
                Class<?> type = types.get(property);
                if (type == null)
                {
                    if (reportMissingProperties)
                    {
                        throw new IllegalArgumentException(property+" not found");
                    }
                }
                else
                {
                    if (transactional != null)
                    {
                        transactionalProperties.add(property, transactional);
                    }
                    MethodHandle mh = map.get(property);
                    MethodHandle bound = mh.bindTo(aps);
                    setters.add(property, bound);
                    undoSetters.add(aps, bound);
                    compile(property);
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    public void addObserver(PropertySetter setter)
    {
        addObserver(setter, true);
    }
    public void addObserver(PropertySetter setter, boolean reportMissingProperties)
    {
        if (setter instanceof AnnotatedPropertyStore)
        {
            AnnotatedPropertyStore aps = (AnnotatedPropertyStore) setter;
            addObserver(aps);
        }
        lock.lock();
        try
        {
            Transactional transactional = null;
            if (setter instanceof Transactional)
            {
                transactional = (Transactional) setter;
            }
            for (String property : setter.getProperties())
            {
                Class<?> type = types.get(property);
                if (type == null)
                {
                    if (reportMissingProperties)
                    {
                        throw new IllegalArgumentException(property+" not found");
                    }
                }
                else
                {
                    if (transactional != null)
                    {
                        transactionalProperties.add(property, transactional);
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
        }
        finally
        {
            lock.unlock();
        }
    }
    public void removeObserver(PropertySetter setter)
    {
        Transactional transactional = null;
        if (setter instanceof Transactional)
        {
            transactional = (Transactional) setter;
        }
        lock.lock();
        try
        {
            List<MethodHandle> list = undoSetters.get(setter);
            if (!list.isEmpty())
            {
                for (String property : setter.getProperties())
                {
                    if (transactional != null)
                    {
                        transactionalProperties.removeItem(property, transactional);
                    }
                    List<MethodHandle> mhs = setters.get(property);
                    if (mhs != null && mhs.removeAll(list))
                    {
                        compile(property);
                    }
                }
                undoSetters.remove(setter);
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    /**
     * Return true if no observers
     * @return 
     */
    public boolean hasObservers()
    {
        return !undoSetters.isEmpty();
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
            throw new IllegalArgumentException(property, ex);
        }
    }

    @Override
    public void start(String reason)
    {
        if (transaction)
        {
            throw new IllegalStateException("transaction not ended");
        }
        transactionTargets.forEach((Transactional t)->
        {
            try
            {
                t.start(reason);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "start(%s)", reason);
            }
        });
        transaction = true;
        TRANSACTION_PROPERTIES.clear();
        transactionTargets.clear();
        //VERSION = VERSION == 0 ? 1 : 0;
    }

    @Override
    public void rollback(String reason)
    {
        if (!transaction)
        {
            throw new IllegalStateException("transaction not started: "+reason);
        }
        transaction = false;
        TRANSACTION_PROPERTIES.forEach((String property)->
        {
            try
            {
                MethodHandle versionSetter = versionSetters.get(property);
                versionSetter.invoke(); // toggle version
                MethodHandle versionGetter = versionGetters.get(property);
                Object version = versionGetter.invoke();    // get version
                MethodHandle ag = arrayGetters.get(property);
                Object committedValue = ag.invoke(version); // get last committed value
                for (MethodHandle setter : setters.get(property))
                {
                    setter.invoke(committedValue);  // rollback
                }
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "rollback restoring %s", property);
            }
            List<Transactional> list = transactionalProperties.get(property);
            if (list != null)
            {
                transactionTargets.addAll(list);
            }
        });
        transactionTargets.forEach((Transactional t)->
        {
            try
            {
                t.rollback(reason);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "rollback(%s)", reason);
            }
        });
    }

    @Override
    public void commit(String reason)
    {
        if (!transaction)
        {
            throw new IllegalStateException("transaction not started");
        }
        transaction = false;
        TRANSACTION_PROPERTIES.forEach((property)->
        {
            List<Transactional> list = transactionalProperties.get(property);
            if (list != null)
            {
                transactionTargets.addAll(list);
            }
        });
        transactionTargets.forEach((Transactional t)->
        {
            try
            {
                t.commit(reason, unmodifiableTransactionProperties);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "commit(%s)", reason);
            }
        });
    }

    private void assignMethod(String property, MethodHandle setter) throws Throwable
    {
        MethodHandle methodSetter = handleSetters.get(property);
        if (setter == null)
        {
            Class<?> type = types.get(property);
            if (type.isPrimitive())
            {
                setter = MethodHandles.lookup().findStatic(InterfaceDispatcher.class, "noOp", MethodType.methodType(void.class, type));
            }
            else
            {
                setter = MethodHandles.lookup().findStatic(InterfaceDispatcher.class, "noOp", MethodType.methodType(void.class, Object.class));
                setter = setter.asType(MethodType.methodType(void.class, type));
            }
        }
        if (transactionalProperties.containsKey(property))
        {
            setter = MethodHandles.foldArguments(setter, transactionAdder.bindTo(property));
            MethodHandle saver = savers.get(property);
            setter = MethodHandles.foldArguments(setter, saver);
            MethodHandle versionSetter = versionSetters.get(property);
            setter = MethodHandles.foldArguments(setter, versionSetter);
        }
        methodSetter.invokeExact(setter);
    }

}
