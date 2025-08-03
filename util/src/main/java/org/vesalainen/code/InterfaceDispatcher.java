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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.code.setter.Setter;
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
 * <p>If observer implements Transactional frame setObject methods with transaction method.
 start() setx(x) setY(y) commit(). You can call rollback() instead of commit()
 which will restore properties to values after last commit.
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
    private boolean transaction;
    private final Map<String,Ctx> ctxMap = new HashMap<>();
    private final MapList<Object,Setter> undoSetters = new IdentityHashMapList<>();
    private final MapList<String,Transactional> transactionalProperties = new HashMapList<>();
    private final ReentrantLock lock = new ReentrantLock();
    
    public InterfaceDispatcher()
    {
        super(InterfaceDispatcher.class);
    }
    protected void init()
    {
        Class<? extends InterfaceDispatcher> cls = this.getClass();
        //Lookup lookup = MethodHandles.lookup();
        try
        {
            for (Field field : cls.getDeclaredFields())
            {
                String property = field.getName();
                Class<?> type = field.getType();
                if (type.isArray())
                {
                    Ctx c = new Ctx();
                    ctxMap.put(property, c);
                    c.type = type.getComponentType();
                    // initial setter
                    Field initialSetterField = cls.getDeclaredField(property+"InitSetter");
                    initialSetterField.setAccessible(true);
                    c.initialSetter = (Setter) initialSetterField.get(this);
                    // setter
                    c.setter = cls.getDeclaredField(property+"Setter");
                    c.setter.setAccessible(true);
                    // isModified
                    Field isModifiedField = cls.getDeclaredField(property+"IsModified");
                    isModifiedField.setAccessible(true);
                    c.isModified = (BooleanSupplier) isModifiedField.get(this);
                    c.version = cls.getDeclaredField(property+"Version");
                    c.version.setAccessible(true);
                    c.array = cls.getDeclaredField(property);
                    c.array.setAccessible(true);
                }
            }
        }
        catch (Throwable ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private boolean isModified(String property)
    {
        try
        {
            Ctx c = ctxMap.get(property);
            return (boolean) c.isModified.getAsBoolean();
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public void addObserver(PropertySetter setter)
    {
        addObserver(setter, true);
    }
    public void addObserver(PropertySetter setter, boolean reportMissingProperties)
    {
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
                Ctx c = ctxMap.get(property);
                if (c == null)
                {
                    if (reportMissingProperties)
                    {
                        throw new IllegalArgumentException(property+" not found");
                    }
                }
                else
                {
                    Class<?> type = c.type;
                    if (transactional != null)
                    {
                        transactionalProperties.add(property, transactional);
                    }
                    Setter s = setter.getSetter(property, type);
                    c.setters.add(s);
                    undoSetters.add(setter, s);
                    compile(property);
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
            List<Setter> list = undoSetters.get(setter);
            if (!list.isEmpty())
            {
                for (String property : setter.getProperties())
                {
                    Ctx c = ctxMap.get(property);
                    if (transactional != null)
                    {
                        transactionalProperties.removeItem(property, transactional);
                    }
                    List<Setter> mhs = c.setters;
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
            Ctx c = ctxMap.get(property);
            List<Setter> list = c.setters;
            Setter setter = c.initialSetter;
            if (list != null)
            {
                for (Setter mh : list)
                {
                    setter = setter.andThen(mh);
                }
            }
            if (transactionalProperties.containsKey(property))
            {
                setter = setter.andThen(()->TRANSACTION_PROPERTIES.add(property));
            }
            c.setter.set(this, setter);
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
                Ctx c = ctxMap.get(property);
                int ver = c.version.getInt(this);
                ver = ver == 0 ? 1 : 0;
                c.version.setInt(this, ver);
                Object array = c.array.get(this);
                Object committedValue = Array.get(array, ver);
                for (Setter setter : c.setters)
                {
                    setter.setObject(committedValue);  // rollback
                }
            }
            catch (Throwable ex)
            {
                throw new RuntimeException("rollback restoring "+property, ex);
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
                throw new RuntimeException("\"rollback "+reason, ex);
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
                t.commit(reason, unmodifiableTransactionProperties, this::isModified);
            }
            catch (Throwable ex)
            {
                log(SEVERE, ex, "commit(%s)", reason);
            }
        });
    }

    private static class Ctx
    {

        private Class<?> type;
        private Setter initialSetter;
        private List<Setter> setters = new ArrayList<>();
        private BooleanSupplier isModified;
        private Field version;
        private Field array;
        private Field setter;
        
    }
}
