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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.lang.Bytes;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * AnnotatedPropertyStore is a PropertyGetter/Setter implementation which 
 * automates getting and or setting properties which are either accessed by
 * fields or methods. Fields and methods must be annotated with @Property and
 * they must have public or packet private access.
 * 
 * <p>Loading and storing to file implements a subset of properties features.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPropertyStore extends JavaLogging implements PropertyGetter, PropertySetter, Transactional
{
    private static final String PREFIX = "#AnnotatedPropertyStore:";
    private static final Map<Class<? extends AnnotatedPropertyStore>,Inner> INNERS = new WeakHashMap<>();
    
    private Map<String,Class<?>> types;
    private Map<String,MethodHandle> setters;
    private Map<String,MethodHandle> getters;
    private Map<String,MethodHandle> copiers;
    private String[] properties;
    private Map<String, MethodHandle> unmodifiableSetters;

    public AnnotatedPropertyStore(AnnotatedPropertyStore aps)
    {
        this((Lookup)null);
        copyFrom(aps);
    }
    public AnnotatedPropertyStore(Lookup lookup, Path path) throws IOException
    {
        this(lookup, path, true);
    }
    public AnnotatedPropertyStore(Lookup lookup, Path path, boolean reportMissingProperties) throws IOException
    {
        this(lookup);
        load(path, reportMissingProperties);
    }    
    public AnnotatedPropertyStore(Lookup lookup)
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
            types = new HashMap<>();
            setters = new HashMap<>();
            getters = new HashMap<>();
            copiers = new HashMap<>();
            try
            {
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
                        types.put(name, field.getType());
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
                            types.put(name, method.getReturnType());
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
                                types.put(name, parameterTypes[0]);
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
        unmodifiableSetters = Collections.unmodifiableMap(setters);
    }
    @Override
    public String[] getProperties()
    {
        return properties;
    }
    /**
     * Returns unmodifiable map of unbound setters.
     * @return 
     */
    public Map<String,MethodHandle> getSetters()
    {
        return unmodifiableSetters;
    }
    /**
     * Copies listed properties
     * @param from
     * @param properties 
     */
    public final void copyFrom(AnnotatedPropertyStore from, Collection<String> properties)
    {
        copyFrom(from, properties, true);
    }
    public final void copyFrom(AnnotatedPropertyStore from, Collection<String> properties, boolean reportMissingProperties)
    {
        if (!this.getClass().isAssignableFrom(from.getClass()))
        {
            throw new IllegalArgumentException("can't copy from "+from);
        }
        properties.forEach((property) ->
        {
            MethodHandle copier = copiers.get(property);
            if (copier != null)
            {
                try
                {
                    copier.invokeExact(this, from);
                }
                catch (Throwable ex)
                {
                    throw new IllegalArgumentException("with "+property, ex);
                }
            }
            else
            {
                if (reportMissingProperties)
                {
                    throw new IllegalArgumentException("can't copy "+property);
                }
            }
        });
    }
    /**
     * Copies all properties.
     * @param from 
     */
    public final void copyFrom(AnnotatedPropertyStore from)
    {
        if (!this.getClass().isAssignableFrom(from.getClass()))
        {
            throw new IllegalArgumentException("can't copy from "+from);
        }
        copiers.entrySet().forEach((entry) ->
        {
            MethodHandle copier = entry.getValue();
            try
            {
                copier.invokeExact(this, from);
            }
            catch (Throwable ex)
            {
                throw new IllegalArgumentException("with "+entry.getKey(), ex);
            }
        });
    }
    public static <T extends AnnotatedPropertyStore> T getInstance(Path path) throws IOException
    {
        return getInstance(path, true);
    }
    public static <T extends AnnotatedPropertyStore> T getInstance(Path path, boolean reportMissingProperties) throws IOException
    {
        try (BufferedReader br = Files.newBufferedReader(path))
        {
            String line = br.readLine();
            if (line.startsWith(PREFIX))
            {
                String className = line.substring(PREFIX.length());
                try
                {
                    Class<T> cls = (Class<T>) Class.forName(className);
                    Constructor<T> constructor = cls.getConstructor();
                    T aps = constructor.newInstance();
                    aps.load(br, reportMissingProperties);
                    return aps;
                }
                catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
            else
            {
                throw new IllegalArgumentException(line+" wrong start of file");
            }
        }
    }
    public final void load(Path path) throws IOException
    {
        load(path, true);
    }
    public final void load(Path path, boolean reportMissingProperties) throws IOException
    {
        try (BufferedReader br = Files.newBufferedReader(path))
        {
            load(br, reportMissingProperties);
        }
    }
    public final void load(BufferedReader br, boolean reportMissingProperties) throws IOException
    {
        String line = br.readLine();
        checkClass(line);
        while (line != null)
        {
            line = line.trim();
            if (!line.startsWith("#"))
            {
                String[] split = line.split("=", 2);
                String property = split[0];
                String value = null;
                if (split.length == 2)
                {
                    value = unescape(split[1].trim());
                }
                Class<?> type = types.get(property);
                if (type != null)
                {
                    Object newValue = ConvertUtility.convert(type, value);
                    set(property, newValue);
                }
                else
                {
                    if (reportMissingProperties)
                    {
                        throw new IllegalArgumentException(property+" missing");
                    }
                }
            }
            line = br.readLine();
        }
    }
    public void store(Path path) throws IOException
    {
        try (BufferedWriter bw = Files.newBufferedWriter(path, WRITE, CREATE))
        {
            store(bw);
        }
    }
    public void store(Appendable out) throws IOException
    {
        out.append(PREFIX);
        out.append(this.getClass().getName());
        out.append('\n');
        for (String property : properties)
        {
            out.append(property);
            Object value = getObject(property);
            if (value != null)
            {
                out.append('=');
                out.append(escape(value.toString()));
            }
            out.append('\n');
        }
    }
    /**
     * Hash code is calculated by using all property values
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        for (String property : properties)
        {
            hash += Objects.hashCode(getObject(property));
        }
        return hash;
    }
    /**
     * Returns SHA-1 digest which is updated with properties content.
     * @return
     * @throws NoSuchAlgorithmException 
     * @see org.vesalainen.lang.Bytes
     */
    public byte[] getSha1() throws NoSuchAlgorithmException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        update(sha1);
        return sha1.digest();
    }
    /**
     * Updates digest with properties content
     * @param digest 
     * @see org.vesalainen.lang.Bytes
     */
    public void update(MessageDigest digest)
    {
        for (String property : properties)
        {
            Class<?> type = types.get(property);
            if (type == null)
            {
                throw new IllegalArgumentException(property+" don't exist");
            }
            switch (type.getSimpleName())
            {
                case "boolean":
                    Bytes.set(getBoolean(property), digest::update);
                    break;
                case "byte":
                    Bytes.set(getByte(property), digest::update);
                    break;
                case "char":
                    Bytes.set(getChar(property), digest::update);
                    break;
                case "short":
                    Bytes.set(getShort(property), digest::update);
                    break;
                case "int":
                    Bytes.set(getInt(property), digest::update);
                    break;
                case "long":
                    Bytes.set(getLong(property), digest::update);
                    break;
                case "float":
                    Bytes.set(getFloat(property), digest::update);
                    break;
                case "double":
                    Bytes.set(getDouble(property), digest::update);
                    break;
                default:
                    Bytes.set(getObject(property), digest::update);
                    break;
            }
        }
    }
    /**
     * Returns true if objects have same class and all properties are equal.
     * @param aps
     * @return 
     */
    
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AnnotatedPropertyStore))
        {
            return false;
        }
        AnnotatedPropertyStore aps = (AnnotatedPropertyStore) obj;
        if (this == aps)
        {
            return true;
        }
        if (aps == null)
        {
            return false;
        }
        if (getClass() != aps.getClass())
        {
            return false;
        }
        for (String property : properties)
        {
            if (!Objects.equals(getObject(property), aps.getObject(property)))
            {
                return false;
            }
        }
        return true;
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

    private String escape(String str)
    {
        return str
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                ;
    }
    private String unescape(String str)
    {
        return str
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                ;
    }

    private void checkClass(String line)
    {
        if (line.startsWith(PREFIX))
        {
            if (!this.getClass().getName().equals(line.substring(PREFIX.length())))
            {
                throw new IllegalArgumentException(line+" wrong class");
            }
        }
    }

    @Override
    public void start(String reason)
    {
    }

    @Override
    public void rollback(String reason)
    {
    }

    @Override
    public void commit(String reason)
    {
    }
    private static class Inner
    {
        private Map<String,Class<?>> types;
        private Map<String,MethodHandle> setters;
        private Map<String,MethodHandle> getters;
        private Map<String,MethodHandle> copiers;
        private String[] properties;

        public Inner(AnnotatedPropertyStore aps)
        {
            this.types = aps.types;
            this.setters = aps.setters;
            this.getters = aps.getters;
            this.copiers = aps.copiers;
            this.properties = aps.properties;
        }
        
        private void populate(AnnotatedPropertyStore aps)
        {
            aps.types = this.types;
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
