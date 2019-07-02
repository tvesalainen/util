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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AnnotatedPropertyStore extends JavaLogging implements PropertyGetter, PropertySetter, Transactional
{
    private static final Map<Class<? extends AnnotatedPropertyStore>,Inner> INNERS = new WeakHashMap<>();
    
    private Map<String,MethodHandle> setters;
    private Map<String,MethodHandle> getters;
    private Map<String,MethodHandle> copiers;
    private String[] properties;

    public AnnotatedPropertyStore(AnnotatedPropertyStore aps)
    {
        this();
        copyFrom(aps);
    }
    public AnnotatedPropertyStore()
    {
        super(AnnotatedPropertyStore.class);
        Class<? extends AnnotatedPropertyStore> cls = this.getClass();
        Inner inner = INNERS.get(cls);
        if (inner != null)
        {
            inner.populate(this);
        }
        else
        {
            Set<Prop> props = new HashSet<>();
            setters = new HashMap<>();
            getters = new HashMap<>();
            copiers = new HashMap<>();
            try
            {
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
                        props.add(new Prop(property, name));
                        MethodHandle mhg = lookup.unreflectGetter(field);
                        MethodType mtg = MethodType.methodType(field.getType(), AnnotatedPropertyStore.class);
                        MethodHandle getter = mhg.asType(mtg);
                        getters.put(name, getter);
                        MethodHandle mhs = lookup.unreflectSetter(field);
                        MethodType mts = MethodType.methodType(void.class, AnnotatedPropertyStore.class, field.getType());
                        MethodHandle setter = mhs.asType(mts);
                        setters.put(name, setter);
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
                        props.add(new Prop(property, name));
                        if (BeanHelper.isGetter(method))
                        {
                            MethodHandle mh = lookup.unreflect(method);
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            if (parameterTypes.length != 0)
                            {
                                throw new IllegalArgumentException(method+" is not getter");
                            }
                            MethodType mt = MethodType.methodType(method.getReturnType(), AnnotatedPropertyStore.class);
                            MethodHandle getter = mh.asType(mt);
                            getters.put(name, getter);
                        }
                        else
                        {
                            if (BeanHelper.isSetter(method))
                            {
                                MethodHandle mh = lookup.unreflect(method);
                                Class<?>[] parameterTypes = method.getParameterTypes();
                                if (parameterTypes.length != 1)
                                {
                                    throw new IllegalArgumentException(method+" is not setter");
                                }
                                MethodType mt = MethodType.methodType(void.class, AnnotatedPropertyStore.class, parameterTypes[0]);
                                MethodHandle setter = mh.asType(mt);
                                setters.put(name, setter);
                            }
                            else
                            {
                                throw new IllegalArgumentException(method+" has @Property but is not either setter or getter");
                            }
                        }
                    }
                }
                List<String> propertiesList = props.stream().sorted().map((p)->p.name).collect(Collectors.toList());
                properties = propertiesList.toArray(new String[propertiesList.size()]);
                for (String property : properties)
                {
                    MethodHandle getter = getters.get(property);
                    MethodHandle setter = setters.get(property);
                    if (getter != null && setter != null)
                    {
                        MethodHandle copier = MethodHandles.collectArguments(setter, 1, getter);
                        copiers.put(property, copier);
                    }
                }
                INNERS.put(cls, new Inner(this));
            }
            catch (IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    @Override
    public String[] getPrefixes()
    {
        return properties;
    }

    public final void copyFrom(AnnotatedPropertyStore oth)
    {
        if (!this.getClass().isAssignableFrom(oth.getClass()))
        {
            throw new IllegalArgumentException("can't copy from "+oth);
        }
        for (String property : properties)
        {
            MethodHandle copier = copiers.get(property);
            if (copier == null)
            {
                throw new IllegalArgumentException(property+" missing getter and/or setter");
            }
            try
            {
                copier.invokeExact(this, oth);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
    }
    @Override
    public boolean getBoolean(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (boolean) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public byte getByte(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (byte) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public char getChar(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (char) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public short getShort(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (short) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public int getInt(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (int) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public long getLong(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (long) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public float getFloat(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (float) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public double getDouble(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return (double) mh.invokeExact(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public Object getObject(String property)
    {
        MethodHandle mh = getters.get(property);
        if (mh != null)
        {
            try
            {
                return mh.invoke(this);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, boolean arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, byte arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, char arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, short arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, int arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, long arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, float arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, double arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invokeExact(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }

    @Override
    public void set(String property, Object arg)
    {
        MethodHandle mh = setters.get(property);
        if (mh != null)
        {
            try
            {
                mh.invoke(this, arg);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+property, ex);
            }
        }
        else
        {
            throw new IllegalArgumentException(property+" has no method handler");
        }
    }
    private static class Inner
    {
        private Map<String,MethodHandle> setters;
        private Map<String,MethodHandle> getters;
        private Map<String,MethodHandle> copiers;
        private String[] properties;

        public Inner(AnnotatedPropertyStore aps)
        {
            this.setters = aps.setters;
            this.getters = aps.getters;
            this.copiers = aps.copiers;
            this.properties = aps.properties;
        }
        
        private void populate(AnnotatedPropertyStore aps)
        {
            aps.getters = this.getters;
            aps.setters = this.setters;
            aps.copiers = this.copiers;
            aps.properties = this.properties;
        }
    }
    private static class Prop implements Comparable<Prop>
    {
        private String name;
        private int ordinal;

        public Prop(Property property, String name)
        {
            this.name = name;
            ordinal = property.ordinal();
        }
        
        @Override
        public int compareTo(Prop o)
        {
            return ordinal- o.ordinal;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Prop other = (Prop) obj;
            if (!Objects.equals(this.name, other.name))
            {
                return false;
            }
            return true;
        }
        
    }
}
