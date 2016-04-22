/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.ConvertUtilityException;
import org.vesalainen.util.function.IndexFunction;

/**
 * A helper class for handling beans.
 * 
 * <p>Bean object are accessed by strings. 
 * 
 * <p>Examples:
 * <p>prop - getProp / setProp
 * <p>list.1 - list.get(1)
 * <p>list.0.p1 - list.get(0).getP1()
 * @author tkv
 */
public class BeanHelper
{

    private static final Pattern INDEX = Pattern.compile("[0-9]+");

    private static final Object resolvType(Object bean, Object object, Function<Object,Object> defaultFunc, BiFunction<Object,Field,Object> fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        if (object instanceof Field)
        {
                Field field = (Field) object;
                return fieldFunc.apply(bean, field);
        }
        if (object instanceof Method)
        {
            Method method = (Method) object;
            return methodFunc.apply(bean, method);
        }
        return defaultFunc.apply(object);
    }
    /**
     * Walks through bean object. For every property consumer is called with
     * property access string
     * @param bean
     * @param consumer 
     */
    public static final void walk(Object bean, BiConsumer<String, Object> consumer)
    {
        walk("", bean, consumer);
    }

    private static final void walk(String prefix, Object bean, BiConsumer<String, Object> consumer)
    {
        for (String fld : getFields(bean.getClass()))
        {
            Object value = getValue(bean, fld);
            if (value != null)
            {
                String name = prefix + fld;
                consumer.accept(name, value);
                if (value.getClass().isArray())
                {
                    Object[] arr = (Object[]) value;
                    int index = 0;
                    for (Object o : arr)
                    {
                        consumer.accept(name + "." + index, value);
                        if (check(o))
                        {
                            walk(name + "." + index + ".", o, consumer);
                        }
                        index++;
                    }
                }
                else
                {
                    if (value instanceof List)
                    {
                        int index = 0;
                        List list = (List) value;
                        for (Object o : list)
                        {
                            consumer.accept(name + "." + index, value);
                            if (check(o))
                            {
                                walk(name + "." + index + ".", o, consumer);
                            }
                            index++;
                        }
                    }
                    else
                    {
                        if (check(value))
                        {
                            walk(name + ".", value, consumer);
                        }
                    }
                }
            }
        }
    }

    private static boolean check(Object ob)
    {
        Class<? extends Object> cls = ob.getClass();
        if (cls.isPrimitive() || cls.getName().startsWith("java."))
        {
            return false;
        }
        return true;
    }
    /**
     * Executes consumer for property.
     * @param <T>
     * @param bean
     * @param property
     * @param consumer 
     */
    public static final <T> void doFor(Object bean, String property, Consumer<T> consumer)
    {
        T t = (T) getValue(bean, property);
        consumer.accept(t);
    }
    /**
     * Return propertys value.
     * @param bean
     * @param property
     * @return 
     */
    public static final Object getValue(Object bean, String property)
    {
        return doFor(bean, property, null, BeanHelper::getValue, BeanHelper::getValue, BeanHelper::getFieldValue, BeanHelper::getMethodValue);
    }

    private static Object getFieldValue(Object base, Field field)
    {
        try
        {
            return field.get(base);
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private static Object getMethodValue(Object base, Method method)
    {
        try
        {
            return method.invoke(base);
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private static Object getValue(Object[] arr, int index)
    {
        return arr[index];
    }
    private static Object getValue(List list, int index)
    {
        return list.get(index);
    }
    private static Object doFor(Object bean, String property, Class type, IndexFunction<Object[],Object> arrayFunc, IndexFunction<List,Object> listFunc, BiFunction<Object,Field,Object> fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        String[] parts = property.split("\\.");
        int len = parts.length - 1;
        for (int ii = 0; ii < len; ii++)
        {
            bean = doIt(bean, parts[ii], null, BeanHelper::getValue, BeanHelper::getValue, BeanHelper::getFieldValue, BeanHelper::getMethodValue);
        }
        int idx = property.lastIndexOf('.');
        if (idx != -1)
        {
            property = property.substring(idx + 1);
        }
        return doIt(bean, property, type, arrayFunc, listFunc, fieldFunc, methodFunc);
    }
    private static Object doIt(Object bean, String property, Class argType, IndexFunction<Object[],Object> arrayFunc, IndexFunction<List,Object> listFunc, BiFunction<Object,Field,Object> fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        if (INDEX.matcher(property).matches())
        {
            int index = Integer.parseInt(property);
            if (bean.getClass().isArray())
            {
                Object[] arr = (Object[]) bean;
                return arrayFunc.apply(arr, index);
            }
            if (bean instanceof List)
            {
                List list = (List) bean;
                return listFunc.apply(list, index);
            }
            throw new IllegalArgumentException(bean + " not list type");
        }
        Class<? extends Object> type = bean.getClass();
        try
        {
            try
            {
                return fieldFunc.apply(bean, type.getField(property));
            }
            catch (NoSuchFieldException exx)
            {
                if (argType != null)
                {
                    try
                    {
                        return methodFunc.apply(bean, type.getMethod(property, argType));
                    }
                    catch (NoSuchMethodException ex)
                    {
                        String methodName = BeanHelper.setter(property);
                        return methodFunc.apply(bean, type.getMethod(methodName, argType));
                    }
                }
                else
                {
                    try
                    {
                        return methodFunc.apply(bean, type.getMethod(property));
                    }
                    catch (NoSuchMethodException ex)
                    {
                        String methodName = BeanHelper.getter(property);
                        try
                        {
                            return methodFunc.apply(bean, type.getMethod(methodName));
                        }
                        catch (NoSuchMethodException ex2)
                        {
                            methodName = BeanHelper.isser(property);
                            return methodFunc.apply(bean, type.getMethod(methodName));
                        }
                    }
                }
            }
        }
        catch (IllegalArgumentException | NoSuchMethodException ex)
        {
            throw new BeanHelperException(property + " not found in " + bean.getClass().getName(), ex);
        }
    }
    /**
     * Returns actual parameter types for property
     * @param bean
     * @param property
     * @return
     */
    public static final Class[] getParameterTypes(Object bean, String property)
    {
        Type type = (Type) doFor(
            bean, 
            property, 
            null,
            (Object[] a, int i)->{return a[i].getClass().getGenericSuperclass();},
            (List l, int i)->{return l.get(i).getClass().getGenericSuperclass();},
            (Object o, Field f)->{return f.getGenericType();}, 
            (Object o, Method m)->{return m.getGenericReturnType();});
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] ata = pt.getActualTypeArguments();
            if (ata.length > 0)
            {
                Class[] ca = new Class[ata.length];
                for (int ii=0;ii<ca.length;ii++)
                {
                    ca[ii] = (Class) ata[ii];
                }
                return ca;
            }
        }
        return null;
    }

    private static Class getFieldType(Object bean, Field field)
    {
        return field.getType();
    }
    private static Class getMethodType(Object bean, Method method)
    {
        return method.getReturnType();
    }
    private static Class getObjectType(Object[] arr, int index)
    {
        return arr[index].getClass();
    }
    private static Class getObjectType(List list, int index)
    {
        return list.get(index).getClass();
    }
    /**
     * Return propertys type.
     * @param bean
     * @param property
     * @return
     */
    public static final Class getType(Object bean, String property)
    {
        return (Class) doFor(bean, property, null, BeanHelper::getObjectType, BeanHelper::getObjectType, BeanHelper::getFieldType, BeanHelper::getMethodType);
    }
    private static Object getFieldAnnotation(Object annotationClass, Field field)
    {
        return field.getAnnotation((Class)annotationClass);
    }
    private static Object getMethodAnnotation(Object annotationClass, Method method)
    {
        return method.getAnnotation((Class)annotationClass);
    }
    /**
     * Returns propertys annotation.
     * @param <T>
     * @param bean
     * @param property
     * @param annotationClass
     * @return 
     */
    public static final <T extends Annotation> T getAnnotation(Object bean, String property, Class<T> annotationClass)
    {
        return (T) doFor(
            bean, 
            property, 
            null,
            (Object[] a, int i)->{return a[i].getClass().getAnnotation(annotationClass);},
            (List l, int i)->{return l.get(i).getClass().getAnnotation(annotationClass);},
            BeanHelper::getFieldAnnotation, 
            BeanHelper::getMethodAnnotation);
    }
    static final <T> T defaultFactory(Class<T> cls)
    {
        try
        {
            return cls.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static final <T> void applyList(Object base, String fieldname)
    {
        applyList(base, fieldname, BeanHelper::defaultFactory);
    }
    public static final <T> void applyList(Object base, String fieldname, Function<Class<T>,T> factory)
    {
        if (fieldname.endsWith("+"))
        {
            addList(base, fieldname.substring(0, fieldname.length()-1), factory);
        }
        else
        {
            if (fieldname.endsWith("-"))
            {
                removeList(base, fieldname.substring(0, fieldname.length()-1));
            }
            else
            {
                throw new IllegalArgumentException("nothing to apply");
            }
        }
    }
    public static final void removeList(Object base, String fieldname)
    {
        doFor(
            base, 
            fieldname, 
            null,
            (Object[] a, int i)->{throw  new UnsupportedOperationException("not supported");},
            (List l, int i)->{l.remove(i);return null;},
            (Object b, Field f)->{throw  new UnsupportedOperationException("not supported");},
            (Object b, Method m)->{throw  new UnsupportedOperationException("not supported");}
        );
    }
    public static final <T> void addList(Object base, String fieldname, T value)
    {
        addList(base, fieldname, (Class<T> c)->{return value;});
    }
    public static final <T> void addList(Object base, String fieldname, Function<Class<T>,T> factory)
    {
        Object fieldValue = getValue(base, fieldname);
        if (fieldValue instanceof List)
        {
            List list = (List) fieldValue;
            Class[] pt = getParameterTypes(base, fieldname);
            Object value = factory.apply(pt[0]);
            if (value != null && !pt[0].isAssignableFrom(value.getClass()))
            {
                throw new IllegalArgumentException(pt[0]+" not assignable from "+value);
            }
            list.add(value);
        }
        else
        {
            throw new IllegalArgumentException(fieldValue+" not List");
        }
    }
    public static final void setFieldValue(Object base, String fieldname, Object v)
    {
        Class<?> type = BeanHelper.getType(base, fieldname);
        if (v != null)
        {
            v = ConvertUtility.convert(type, v);
        }
        Object value = v;
        doFor(
            base, 
            fieldname, 
            type,
            (Object[] a, int i)->{a[i]=value;return null;},
            (List l, int i)->{l.set(i, value);return null;},
            (Object b, Field f)->{try
            {
                f.set(b, value);return null;
            }
            catch (IllegalArgumentException | IllegalAccessException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            }, 
            (Object b, Method m)->{try
            {
                m.invoke(b, value);return null;
            }
            catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            });
    }

    public static final Object invoke(Object base, String expression) throws BeanHelperException
    {
        try
        {
            Method method = (Method) getMethod(base, expression);
            return method.invoke(base);
        }
        catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new BeanHelperException(base, expression, ex);
        }
    }

    public static final Object invoke(Object base, Method method) throws BeanHelperException
    {
        try
        {
            return method.invoke(base);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new BeanHelperException(base, method, ex);
        }
    }

    /**
     * Returns AnnotatedElement for expression. Example: model.getList().size()
     *
     * @param base
     * @param pseudoField
     * @return
     * @throws IllegalArgumentException
     */
    public static final Method getMethod(Object base, String pseudoField) throws BeanHelperException
    {
        try
        {
            try
            {
                return base.getClass().getMethod(pseudoField);
            }
            catch (NoSuchMethodException ex)
            {
                try
                {
                    String name = getter(pseudoField);
                    return base.getClass().getMethod(name);
                }
                catch (NoSuchMethodException ex2)
                {
                    String name = isser(pseudoField);
                    return base.getClass().getMethod(name);
                }
            }
        }
        catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex)
        {
            throw new BeanHelperException(base, pseudoField, ex);
        }
    }

    public static final Field getField(Class clazz, String name) throws BeanHelperException
    {
        Class sc = clazz;
        NoSuchFieldException ex = null;
        while (sc != null)
        {
            try
            {
                return sc.getDeclaredField(name);
            }
            catch (NoSuchFieldException ex1)
            {
                ex = ex1;
                sc = sc.getSuperclass();
            }
        }
        throw new BeanHelperException(clazz, name, ex);
    }

    /**
     * If object == null return's false If object is array or List return's true
     * Otherwise return's false
     *
     * @param object
     * @return
     */
    public static final boolean isArrayType(Object object)
    {
        if (object == null)
        {
            return false;
        }
        if (object.getClass().isArray())
        {
            return true;
        }
        else
        {
            if (object instanceof List)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * @param object
     * @return
     * @throws BeanHelperException
     */
    public static final Collection getObjectCollection(Object object) throws BeanHelperException
    {
        if (object == null)
        {
            return new HashSet();
        }
        if (object instanceof Collection)
        {
            return (Collection) object;
        }
        if (object.getClass().isArray())
        {
            Set set = new HashSet();
            Object[] arr = (Object[]) object;
            Collections.addAll(set, arr);
            return set;
        }
        Set set = new HashSet();
        set.add(object);
        return set;
    }

    public static final Object[] castArray(Object[] arr, Object[] object)
    {
        assert arr.length == object.length;
        System.arraycopy(object, 0, arr, 0, arr.length);
        return arr;
    }

    public static final Object[] getObjectArray(Object object) throws BeanHelperException, ConvertUtilityException
    {
        if (object == null)
        {
            return new Object[]
            {
                null
            };
        }
        return getObjectArray(object.getClass(), object);
    }

    /**
     * If object == null return's empty array If object is array return's the
     * array If object is List return an array containing list elements
     * Otherwise return's an array containing the object
     *
     * @param type
     * @param object
     * @return
     */
    public static final Object[] getObjectArray(Class type, Object object) throws BeanHelperException, ConvertUtilityException
    {
        if (type.isArray())
        {
            return (Object[]) ConvertUtility.convert(Object.class, object);
        }
        else
        {
            if (type.isEnum())
            {
                try
                {
                    Method values = type.getMethod("values");
                    return (Object[]) values.invoke(null);
                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex)
                {
                    throw new BeanHelperException("Enum", ex);
                }
            }
            else
            {
                if (object == null)
                {
                    return new Object[]
                    {
                    };
                }
                else
                {
                    if (object instanceof List)
                    {
                        List list = (List) object;
                        return list.toArray();
                    }
                    else
                    {
                        return new Object[]
                        {
                            object
                        };
                    }
                }
            }
        }
    }

    /**
     * field -> getField
     *
     * @param field
     * @param fieldName
     * @return
     */
    public static final String getter(Field field, String fieldName)
    {
        Class<?> type = field.getType();
        if (type.isPrimitive() && "boolean".equals(type.getName()))
        {
            return isser(fieldName);
        }
        else
        {
            return getter(fieldName);
        }
    }

    public static final String getter(String fieldName)
    {
        return "get" + upper(fieldName);
    }

    public static final String isser(String fieldName)
    {
        return "is" + upper(fieldName);
    }

    /**
     * field -> setField
     *
     * @param field
     * @return
     */
    public static final String setter(String field)
    {
        return "set" + upper(field);
    }

    /**
     * <p>
     * (g/s)etField -> field
     * <p>
     * Field -> field
     *
     * @param etter
     * @return
     */
    public static final String field(String etter)
    {
        if (etter.startsWith("get") || etter.startsWith("set"))
        {
            etter = etter.substring(3);
        }
        return lower(etter);
    }

    private static String lower(String str)
    {
        if (str.length() > 0)
        {
            return str.substring(0, 1).toLowerCase() + str.substring(1);
        }
        else
        {
            return str;
        }
    }

    private static String upper(String str)
    {
        if (str.length() > 0)
        {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        else
        {
            return str;
        }
    }

    /**
     * Returns fieldname for method. getX -&gt; 'x'
     *
     * @param method
     * @return
     */
    public static final String getField(Method method)
    {
        return field(method.getName());
    }

    /**
     * Returns Set of classes fieldnames.
     *
     * @param cls
     * @return
     */
    public static final Set<String> getFields(Class<?> cls)
    {
        Set<String> set = new HashSet<>();
        for (Method method : cls.getDeclaredMethods())
        {
            if (method.getName().startsWith("get")
                    && method.getParameterCount() == 0
                    && !Void.class.equals(method.getReturnType()))
            {
                set.add(getField(method));
            }
        }
        for (Field field : cls.getFields())
        {
            set.add(field.getName());
        }
        return set;
    }
}
