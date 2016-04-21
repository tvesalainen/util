/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.ConvertUtilityException;

/**
 * Base object is always the same bean object. Expressions are relative to base
 * when they don't start with '.'.
 * If for example the base bean is page. Expression 'count' is the same as
 * page.count. 'list[1].toString()' is page.list[1].toString().
 *
 * If expression starts with '.' it is relative to the annotated element. If
 * expression starts with '..' it is relative to the parent of that field.
 * @author tkv
 */
public class BeanHelper
{
    private static final Pattern FIELDEXPR = Pattern.compile("([^\\}\\(\\)]+)");
    private static final Pattern METHODEXPR = Pattern.compile("([^\\}\\(\\)]+)\\(\\)");
    private static final Pattern ARRAYEXPR = Pattern.compile("([^\\}\\(\\)]+)\\[([0-9]+)\\]");

    private static final Pattern FIELD = Pattern.compile("([^\\(\\)\\[\\]]+)");
    private static final Pattern METHOD = Pattern.compile("([^\\(\\)]+)\\(\\)");

    private static final Pattern INDEX = Pattern.compile("[0-9]+");

    private static Object resolvBase(Object base, String expr)
    {
        String[] parts = expr.split("\\.");
        int len = parts.length-1;
        for (int ii=0;ii<len;ii++)
        {
            base = getObject(base, parts[ii]);
        }
        return base;
    }
    private static Object base(Object base, String expr)
    {
        try
        {
            Matcher matcher = METHODEXPR.matcher(expr);
            if (matcher.matches())
            {
                return getObject(base, matcher.group(1));
            }
            matcher = ARRAYEXPR.matcher(expr);
            if (matcher.matches())
            {
                int index = Integer.parseInt(matcher.group(2));
                return getObject(base, matcher.group(1), index);
            }
            return getObject(base, expr);
        }
        catch (SecurityException | IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public static final Object getFieldValue(Object base, String pseudoField, int index) throws BeanHelperException
    {
        return null;
    }
    public static final Object getObject(Object base, String pseudoField, int index) throws BeanHelperException
    {
        if (index == -1)
        {
            return getFieldValue(base, pseudoField);
        }
        else
        {
            Object ob = getFieldValue(base, pseudoField);
            if (ob.getClass().isArray())
            {
                Object[] arr = (Object[]) ob;
                return arr[index];
            }
            if (ob instanceof List)
            {
                List list = (List) ob;
                return list.get(index);
            }
            throw new IllegalArgumentException(ob+" not list type");
        }
    }
    public static final <T> void doFor(Object base, String pseudoField, Consumer<T> consumer)
    {
        T t = (T) getFieldValue(base, pseudoField);
        consumer.accept(t);
    }
    public static final Object getFieldValue(Object base, String pseudoField)
    {
        base = resolvBase(base, pseudoField);
        int idx = pseudoField.lastIndexOf('.');
        if (idx != -1)
        {
            pseudoField = pseudoField.substring(idx+1);
        }
        return getObject(base, pseudoField);
    }
    private static Object getObject(Object base, String fieldname)
    {
        if (INDEX.matcher(fieldname).matches())
        {
            int index = Integer.parseInt(fieldname);
            if (base.getClass().isArray())
            {
                Object[] arr = (Object[]) base;
                return arr[index];
            }
            if (base instanceof List)
            {
                List list = (List) base;
                return list.get(index);
            }
            throw new IllegalArgumentException(base+" not list type");
        }
        Class<? extends Object> type = base.getClass();
        try
        {
            try
            {
                Field field = type.getField(fieldname);
                return field.get(base);
            }
            catch (NoSuchFieldException exx)
            {
                try
                {
                    Method method = type.getMethod(fieldname);
                    return method.invoke(base);
                }
                catch (NoSuchMethodException ex)
                {
                    String methodName = BeanHelper.getter(fieldname);
                    try
                    {
                        Method method = type.getMethod(methodName);
                        return method.invoke(base);
                    }
                    catch (NoSuchMethodException ex2)
                    {
                        methodName = BeanHelper.isser(fieldname);
                        Method method = base.getClass().getMethod(methodName);
                        return method.invoke(base);
                    }
                }
            }
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
        {
            throw new BeanHelperException(fieldname+" not found in "+base.getClass().getName(), ex);
        }
    }
    public static final void setFieldValue(Object base, String pseudoField, Object cv, int index) throws BeanHelperException, ConvertUtilityException
    {
        if (index == -1)
        {
            setFieldValue(base, pseudoField, cv);
        }
        else
        {
            Object value = getFieldValue(base, pseudoField);
            if (value instanceof List)
            {
                List list = (List) value;
                list.set(index, cv);
            }
            else
            {
                if (value.getClass().isArray())
                {
                    Object[] arr = (Object[]) value;
                    arr[index] = cv;
                }
                else
                {
                    throw new BeanHelperException("index for non indexed type", new BeanHelperException(base, pseudoField));
                }
            }
        }
    }
    public static final Class getType(Object base, String pseudoField) throws BeanHelperException
    {
        base = resolvBase(base, pseudoField);
        int idx = pseudoField.lastIndexOf('.');
        if (idx != -1)
        {
            pseudoField = pseudoField.substring(idx+1);
        }
        return type(base, pseudoField);
    }
    public static final Class type(Object base, String pseudoField) throws BeanHelperException
    {
        Class<? extends Object> type = base.getClass();
        try
        {
            Field field = type.getField(pseudoField);
            return field.getType();
        }
        catch (NoSuchFieldException | SecurityException ex)
        {
            Method method = getMethod(base, pseudoField);
            return method.getReturnType();
        }
    }
    public static final <T extends Annotation> T getAnnotation(Object base, String pseudoField, Class<T> annotationClass) throws BeanHelperException
    {
        base = resolvBase(base, pseudoField);
        int idx = pseudoField.lastIndexOf('.');
        if (idx != -1)
        {
            pseudoField = pseudoField.substring(idx+1);
        }
        return annotation(base, pseudoField, annotationClass);
    }
    public static final <T extends Annotation> T annotation(Object base, String fieldname, Class<T> annotationClass) throws BeanHelperException
    {
        if (INDEX.matcher(fieldname).matches())
        {
            int index = Integer.parseInt(fieldname);
            if (base.getClass().isArray())
            {
                Object[] arr = (Object[]) base;
                return arr[index].getClass().getAnnotation(annotationClass);
            }
            if (base instanceof List)
            {
                List list = (List) base;
                return list.get(index).getClass().getAnnotation(annotationClass);
            }
            throw new IllegalArgumentException(base+" not list type");
        }
        Class<? extends Object> type = base.getClass();
        try
        {
            Field field = type.getField(fieldname);
            return field.getAnnotation(annotationClass);
        }
        catch (NoSuchFieldException | SecurityException ex)
        {
            Method method = getMethod(base, fieldname);
            return method.getReturnType().getDeclaredAnnotation(annotationClass);
        }
    }
    public static final void setFieldValue(Object base, String pseudoField, Object value) throws BeanHelperException, ConvertUtilityException
    {
        base = resolvBase(base, pseudoField);
        int idx = pseudoField.lastIndexOf('.');
        if (idx != -1)
        {
            pseudoField = pseudoField.substring(idx+1);
        }
        setValue(base, pseudoField, value);
    }
    private static final void setValue(Object base, String fieldname, Object value) throws BeanHelperException, ConvertUtilityException
    {
        if (INDEX.matcher(fieldname).matches())
        {
            int index = Integer.parseInt(fieldname);
            if (base.getClass().isArray())
            {
                Object[] arr = (Object[]) base;
                arr[index] = value;
                return;
            }
            if (base instanceof List)
            {
                List list = (List) base;
                list.set(index, value);
                return;
            }
            throw new IllegalArgumentException(base+" not list type");
        }
        try
        {
            Class<? extends Object> type = base.getClass();
            try
            {
                Field field = type.getField(fieldname);
                field.set(base, ConvertUtility.convert(field.getType(), value));
            }
            catch (NoSuchFieldException | SecurityException ex)
            {
                String methodName = BeanHelper.setter(fieldname);
                for (Method method : type.getMethods())
                {
                    if (methodName.equals(method.getName()))
                    {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes.length == 1)
                        {
                            method.invoke(base, ConvertUtility.convert(paramTypes[0], value));
                            return;
                        }
                    }
                }
                throw new BeanHelperException("no setter for", base, fieldname);
            }
        }
        catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException ex)
        {
            throw new BeanHelperException(base, fieldname, ex);
        }
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
     * If object == null return's false
     * If object is array or List return's true
     * Otherwise return's false
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
            return new Object[]{null};
        }
        return getObjectArray(object.getClass(), object);
    }
    /**
     * If object == null return's empty array
     * If object is array return's the array
     * If object is List return an array containing list elements
     * Otherwise return's an array containing the object
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
                    return new Object[]{};
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
                        return new Object[]{object};
                    }
                }
            }
        }
    }

    /**
     * field -> getField
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
        return "get"+upper(fieldName);
    }
    public static final String isser(String fieldName)
    {
        return "is"+upper(fieldName);
    }
    /**
     * field -> setField
     * @param field
     * @return
     */
    public static final String setter(String field)
    {
        return "set"+upper(field);
    }
    /**
     * <p>(g/s)etField -> field
     * <p>Field -> field
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
        return str.substring(0, 1).toLowerCase()+str.substring(1);
    }

    private static String upper(String str)
    {
        return str.substring(0, 1).toUpperCase()+str.substring(1);
    }
    /**
     * Returns fieldname for method. getX -&gt; 'x'
     * @param method
     * @return 
     */
    public static final String getField(Method method)
    {
        return field(method.getName());
    }
    /**
     * Returns Set of classes fieldnames.
     * @param cls
     * @return 
     */
    public static final Set<String> getFields(Class<?> cls)
    {
        Set<String> set = new HashSet<>();
        for (Method method : cls.getDeclaredMethods())
        {
            if (
                    method.getName().startsWith("get") &&
                    method.getParameterCount() == 0 &&
                    !Void.class.equals(method.getReturnType())
                    )
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
