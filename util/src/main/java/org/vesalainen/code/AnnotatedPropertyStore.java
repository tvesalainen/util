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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.vesalainen.bean.BeanHelper;
import org.vesalainen.code.getter.BooleanGetter;
import org.vesalainen.code.getter.ByteGetter;
import org.vesalainen.code.getter.CharGetter;
import org.vesalainen.code.getter.DoubleGetter;
import org.vesalainen.code.getter.IntGetter;
import org.vesalainen.code.getter.LongGetter;
import org.vesalainen.code.getter.ObjectGetter;
import org.vesalainen.code.getter.ShortGetter;
import org.vesalainen.code.setter.BooleanSetter;
import org.vesalainen.code.setter.BooleanSingleSetter;
import org.vesalainen.code.setter.ByteSetter;
import org.vesalainen.code.setter.ByteSingleSetter;
import org.vesalainen.code.setter.CharSetter;
import org.vesalainen.code.setter.CharSingleSetter;
import org.vesalainen.code.setter.DoubleSetter;
import org.vesalainen.code.setter.DoubleSingleSetter;
import org.vesalainen.code.setter.FloatSetter;
import org.vesalainen.code.setter.FloatSingleSetter;
import org.vesalainen.code.setter.IntSetter;
import org.vesalainen.code.setter.IntSingleSetter;
import org.vesalainen.code.setter.LongSetter;
import org.vesalainen.code.setter.LongSingleSetter;
import org.vesalainen.code.setter.ObjectSetter;
import org.vesalainen.code.setter.ObjectSingleSetter;
import org.vesalainen.code.setter.ShortSetter;
import org.vesalainen.code.setter.ShortSingleSetter;
import org.vesalainen.lang.Bytes;
import org.vesalainen.net.Nets;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.JavaLogging;

/**
 * AnnotatedPropertyStore is a PropertyGetter/Setter implementation which 
 * automates getting and or setting properties which are either accessed by
 * fields or methods. Fields and methods must be annotated with @Property.
 * 
 * <p>Loading and storing to file implements a subset of properties features.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPropertyStore extends JavaLogging implements PropertyGetter, PropertySetter, Transactional
{
    private static final String PREFIX = "#AnnotatedPropertyStore:";
    private static final Map<Class<? extends AnnotatedPropertyStore>,Inner> INNERS = new WeakHashMap<>();
    
    private final Lookup lookup;
    private Map<String,C> cMap;
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
    public AnnotatedPropertyStore(Lookup lookup, URL url) throws IOException, URISyntaxException
    {
        this(lookup, url, true);
    }
    public AnnotatedPropertyStore(Lookup lookup, URL url, boolean reportMissingProperties) throws IOException, URISyntaxException
    {
        this(lookup);
        load(url, reportMissingProperties);
    }    
    public AnnotatedPropertyStore(Lookup lookup)
    {
        super(AnnotatedPropertyStore.class);
        this.lookup = lookup;
        Class<? extends AnnotatedPropertyStore> cls = this.getClass();
        Inner inner = INNERS.get(cls);
        if (inner != null)
        {
            inner.populate(this);
        }
        else
        {
            Set<Prop> props = new HashSet<>();
            cMap = new HashMap<>();
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
                        C c = new C();
                        cMap.put(name, c);
                        for (String alias : property.aliases())
                        {
                            if (cMap.put(alias, c) != null)
                            {
                                throw new IllegalArgumentException(alias+" is not unique alias");
                            }
                        }
                        props.add(new Prop(property, name));
                        c.type = field.getType();
                        MethodHandle mhg = lookup.unreflectGetter(field);
                        MethodType mtg = MethodType.methodType(field.getType(), AnnotatedPropertyStore.class);
                        MethodHandle getter = mhg.asType(mtg);
                        c.getter = getter;
                        MethodHandle mhs = lookup.unreflectSetter(field);
                        MethodType mts = MethodType.methodType(void.class, AnnotatedPropertyStore.class, field.getType());
                        MethodHandle setter = mhs.asType(mts);
                        c.setter = setter;
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
                        C c = cMap.get(name);
                        if (c == null)
                        {
                            c = new C();
                            cMap.put(name, c);
                            for (String alias : property.aliases())
                            {
                                if (cMap.put(alias, c) != null)
                                {
                                    throw new IllegalArgumentException(alias+" is not unique alias");
                                }
                            }
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
                            c.getter = getter;
                            c.type = method.getReturnType();
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
                                c.setter = setter;
                                c.type = parameterTypes[0];
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
                    C c = cMap.get(property);
                    if (c.getter != null && c.setter != null)
                    {
                        MethodHandle copier = MethodHandles.collectArguments(c.setter, 1, c.getter);
                        c.copier = copier;
                    }
                }
                INNERS.put(cls, new Inner(this));
            }
            catch (IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        Map<String,MethodHandle> us = new HashMap<>();
        cMap.forEach((p, c)->{if (c.setter != null) us.put(p, c.setter);});
        unmodifiableSetters = Collections.unmodifiableMap(us);
    }
    @Override
    public BooleanGetter getBooleanGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(BooleanGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }

    @Override
    public ByteGetter getByteGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(ByteGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    
    @Override
    public CharGetter getCharGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(CharGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    
    @Override
    public ShortGetter getShortGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(ShortGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    
    @Override
    public IntGetter getIntGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(IntGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public DoubleGetter getDoubleGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(DoubleGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public LongGetter getLongGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(LongGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public <T> ObjectGetter<T> getObjectGetter(String property)
    {
        C c = c(property);
        if (c.getter != null)
        {
            return MethodHandleProxies.asInterfaceInstance(ObjectGetter.class, c.getter.bindTo(this));
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public BooleanSetter getBooleanSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            BooleanSingleSetter s = MethodHandleProxies.asInterfaceInstance(BooleanSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public ByteSetter getByteSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            ByteSingleSetter s = MethodHandleProxies.asInterfaceInstance(ByteSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public CharSetter getCharSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            CharSingleSetter s = MethodHandleProxies.asInterfaceInstance(CharSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public ShortSetter getShortSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            ShortSingleSetter s = MethodHandleProxies.asInterfaceInstance(ShortSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public IntSetter getIntSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            IntSingleSetter s = MethodHandleProxies.asInterfaceInstance(IntSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public LongSetter getLongSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            LongSingleSetter s = MethodHandleProxies.asInterfaceInstance(LongSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public FloatSetter getFloatSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            FloatSingleSetter s = MethodHandleProxies.asInterfaceInstance(FloatSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public DoubleSetter getDoubleSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            DoubleSingleSetter s = MethodHandleProxies.asInterfaceInstance(DoubleSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
    }
    @Override
    public <T> ObjectSetter<T> getObjectSetter(String property)
    {
        C c = c(property);
        if (c.setter != null)
        {
            ObjectSingleSetter s = MethodHandleProxies.asInterfaceInstance(ObjectSingleSetter.class, c.setter.bindTo(this));
            return (v)->s.set(v);
        }
        else
        {
            throw new IllegalArgumentException(property+" doesn't have a getter");
        }
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
     * Returns true is property exists.
     * @param property
     * @return 
     */
    public boolean hasProperty(String property)
    {
        return cMap.containsKey(property);
    }
    /**
     * Returns type of property
     * @param property
     * @return 
     */
    public Class<?> getType(String property)
    {
        return c(property).type;
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
            C c = null;
            try
            {
                c = c(property);
            }
            catch (IllegalArgumentException ex)
            {
                if (reportMissingProperties)
                {
                    throw ex;
                }
                return;
            }
            MethodHandle copier = c.copier;
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
        cMap.entrySet().forEach((entry) ->
        {
            MethodHandle copier = entry.getValue().copier;
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
    public final void load(URL url, boolean reportMissingProperties) throws IOException, URISyntaxException
    {
        load(Nets.createReader(url), reportMissingProperties);
    }
    public final void load(BufferedReader br, boolean reportMissingProperties) throws IOException
    {
        String line = br.readLine();
        if (line != null)
        {
            checkClass(line);
        }
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
                C c = cMap.get(property);
                if (c != null)
                {
                    Object newValue = ConvertUtility.convert(c.type, value);
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
    public void store(URL url) throws IOException
    {
        store(Nets.getPath(url));
    }
    public void store(Path path) throws IOException
    {
        try (BufferedWriter bw = Files.newBufferedWriter(path, WRITE, CREATE, TRUNCATE_EXISTING))
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
     * @see org.vesalainen.lang.Bytes
     */
    public byte[] getSha1()
    {
        MessageDigest sha1;
        try
        {
            sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IllegalArgumentException(ex);
        }
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
            C c = c(property);
            switch (c.type.getSimpleName())
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
    public final boolean getBoolean(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (boolean) c.getter.invokeExact(this);
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
    public final byte getByte(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (byte) c.getter.invokeExact(this);
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
    public final char getChar(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (char) c.getter.invokeExact(this);
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
    public final short getShort(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (short) c.getter.invokeExact(this);
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
    public final int getInt(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (int) c.getter.invokeExact(this);
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
    public final long getLong(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (long) c.getter.invokeExact(this);
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
    public final float getFloat(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (float) c.getter.invokeExact(this);
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
    public final double getDouble(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (double) c.getter.invokeExact(this);
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
    public final <T> T getObject(String property)
    {
        C c = cMap.get(property);
        if (c != null && c.getter != null)
        {
            try
            {
                return (T) c.getter.invoke(this);
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
    public final void set(String property, boolean arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, byte arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, char arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, short arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, int arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, long arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, float arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, double arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invokeExact(this, arg);
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
    public final void set(String property, Object arg)
    {
        C c = cMap.get(property);
        if (c != null && c.setter != null)
        {
            try
            {
                c.setter.invoke(this, arg);
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

    protected C c(String property)
    {
        C c = cMap.get(property);
        if (c == null)
        {
            throw new IllegalArgumentException(property+" not found");
        }
        return c;
    }
    private static class Inner
    {
        private Map<String,C> cMap;
        private String[] properties;

        public Inner(AnnotatedPropertyStore aps)
        {
            this.cMap = aps.cMap;
            this.properties = aps.properties;
        }
        
        private void populate(AnnotatedPropertyStore aps)
        {
            aps.cMap = this.cMap;
            aps.properties = this.properties;
        }
    }
    protected static class C
    {

        protected Class<?> type;
        protected MethodHandle getter;
        protected MethodHandle setter;
        protected MethodHandle copier;

        public MethodHandle getGetter()
        {
            return getter;
        }

        public MethodHandle getSetter()
        {
            return setter;
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
