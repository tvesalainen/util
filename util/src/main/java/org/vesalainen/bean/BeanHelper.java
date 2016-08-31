/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;
import org.vesalainen.util.ArrayBasedComparator;
import org.vesalainen.util.ArrayIterator;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.LinkedSet;
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
    public static final char Lim = '.';
    public static final String RegexDel = "\\"+Lim;
    public static final char Add = '+';
    public static final char Rem = '#';
    public static final String Remove = Rem+"";
    public static final char Assign = '=';
    private static final Pattern Index = Pattern.compile("[0-9]+");

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

    private static Object getFieldValue(Object base, Class cls, String property) throws NoSuchFieldException
    {
        try
        {
            return cls.getField(property).get(base);
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
    private static Object getValue(Object arr, int index)
    {
        return Array.get(arr, index);
    }
    private static Object getValue(List list, int index)
    {
        return list.get(index);
    }
    private static Object doFor(Object bean, String property, Class type, IndexFunction<Object,Object> arrayFunc, IndexFunction<List,Object> listFunc, FieldFunction fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        String[] parts = property.split(RegexDel);
        int len = parts.length - 1;
        for (int ii = 0; ii < len; ii++)
        {
            bean = doIt(bean, parts[ii], null, BeanHelper::getValue, BeanHelper::getValue, BeanHelper::getFieldValue, BeanHelper::getMethodValue);
        }
        int idx = property.lastIndexOf(Lim);
        if (idx != -1)
        {
            property = property.substring(idx + 1);
        }
        return doIt(bean, property, type, arrayFunc, listFunc, fieldFunc, methodFunc);
    }
    private static Object doIt(Object bean, String property, Class argType, IndexFunction<Object,Object> arrayFunc, IndexFunction<List,Object> listFunc, FieldFunction fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        if (Index.matcher(property).matches())
        {
            int index = Integer.parseInt(property);
            if (bean.getClass().isArray())
            {
                return arrayFunc.apply(bean, index);
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
                return fieldFunc.apply(bean, type, property);
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
            (Object a, int i)->{Object o = Array.get(a, i);return o!=null?o.getClass().getComponentType():null;},
            (List l, int i)->{return l.get(i).getClass().getGenericSuperclass();},
            (Object o, Class c, String p)->{return getField(c, p).getGenericType();}, 
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
        if (type instanceof Class)
        {
            Class cls = (Class) type;
            if (cls.isArray())
            {
                cls = cls.getComponentType();
            }
            return new Class[] {cls};
        }
        return null;
    }
    /**
     * Returns Declared field either from given class or it's super class
     * @param cls
     * @param fieldname
     * @return
     * @throws NoSuchFieldException 
     */
    public static Field getField(Class cls, String fieldname) throws NoSuchFieldException
    {
        while (true)
        {
            try
            {
                return cls.getDeclaredField(fieldname);
            }
            catch (NoSuchFieldException ex)
            {
                cls = cls.getSuperclass();
                if (Object.class.equals(cls))
                {
                    throw ex;
                }
            }
            catch (SecurityException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
    private static Class getFieldType(Object bean, Class cls, String property) throws NoSuchFieldException
    {
        return getField(cls, property).getType();
    }
    private static Class getMethodType(Object bean, Method method)
    {
        return method.getReturnType();
    }
    private static Class getObjectType(Object arr, int index)
    {
        Object value = Array.get(arr, index);
        if (value != null)
        {
            return value.getClass();
        }
        else
        {
            return null;
        }
    }
    private static Class getObjectType(List list, int index)
    {
        return list.get(index).getClass();
    }
    /**
     * Return property type.
     * @param bean
     * @param property
     * @return
     */
    public static final Class getType(Object bean, String property)
    {
        return (Class) doFor(bean, property, null, BeanHelper::getObjectType, BeanHelper::getObjectType, BeanHelper::getFieldType, BeanHelper::getMethodType);
    }
    private static Object getMethodAnnotation(Object annotationClass, Method method)
    {
        return method.getAnnotation((Class)annotationClass);
    }
    /**
     * Returns property annotation.
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
            (Object a, int i)->{Object o=Array.get(a, i);return o!=null?o.getClass().getAnnotation(annotationClass):null;},
            (List l, int i)->{return l.get(i).getClass().getAnnotation(annotationClass);},
            (Object b, Class c, String p)->{return getField(c, p).getAnnotation(annotationClass);}, 
            (Object b, Method m)->{return m.getAnnotation(annotationClass);});
    }
    
    /**
     * Return true if property exists.
     * @param bean
     * @param property
     * @return 
     */
    public static final boolean hasProperty(Object bean, String property)
    {
        try
        {
            return (boolean) doFor(
                bean, 
                property, 
                null,
                (Object a, int i)->{return true;},
                (List l, int i)->{return true;},
                (Object b, Class c, String p)->{getField(c, p);return true;}, 
                (Object b, Method m)->{return true;});
        }
        catch (BeanHelperException ex)
        {
            return false;
        }
    }
    /**
     * Return propertys annotations
     * @param <T>
     * @param bean
     * @param property
     * @return 
     */
    public static final <T extends Annotation> T[] getAnnotations(Object bean, String property)
    {
        return (T[]) doFor(
            bean, 
            property, 
            null,
            (Object a, int i)->{Object o=Array.get(a, i);return o!=null?o.getClass().getAnnotations():null;},
            (List l, int i)->{return l.get(i).getClass().getAnnotations();},
            (Object b, Class c, String p)->{return getField(c, p).getAnnotations();}, 
            (Object b, Method m)->{return m.getAnnotations();});
    }
    /**
     * Returns AnnotatedElement for property
     * @param bean
     * @param property
     * @return 
     */
    public static final AnnotatedElement getAnnotatedElement(Object bean, String property)
    {
        return (AnnotatedElement) doFor(
            bean, 
            property, 
            null,
            (Object a, int i)->{Object o=Array.get(a, i);return o!=null?o.getClass():null;},
            (List l, int i)->{Object o = l.get(i);return o!=null?o.getClass():null;},
            (Object b, Class c, String p)->{return getField(c, p);}, 
            (Object b, Method m)->{return m;});
    }
    /**
     * Default object factory that calls newInstance method. Checked exceptions
     * are wrapped in IllegalArgumentException.
     * @param <T>
     * @param cls Target class
     * @param hint Hint for factory
     * @return 
     */
    public static final <T> T defaultFactory(Class<T> cls, String hint)
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
    /**
     * Applies bean action by using default factory.
     * 
     * <p>Bean actions are:
     * <p>List item property remove by adding '#' to the end of pattern.
     * <p>E.g. list.3- same as list.remove(3)
     * <p>List item property assign by adding '=' to the end of pattern
     * <p>E.g. list.3=hint same as list.set(3, factory.get(cls, hint))
     * <p>List item creation to the end of the list.
     * <p>E.g. list+ same as add(factory.get(cls, null))
     * <p>E.g. list+hint same as add(factory.get(cls, hint))
     * @param <T>
     * @param base
     * @param fieldname 
     * @return true if pattern was applied
     */
    public static final <T> T applyList(Object base, String fieldname)
    {
        return applyList(base, fieldname, BeanHelper::defaultFactory);
    }
    /**
     * Applies bean action by using given factory
     * <p>Bean actions are:
     * <p>List item property remove by adding '#' to the end of pattern.
     * <p>E.g. list.3- - list.remove(3)
     * <p>List item creation to the end of the list.
     * <p>E.g. list+ - add(factory.get(cls, null))
     * <p>E.g. list+hint - add(factory.get(cls, hint))
     * @param <T>
     * @param bean
     * @param property
     * @param factory 
     * @return true if pattern was applied
     */
    public static final <T> T applyList(Object bean, String property, BiFunction<Class<T>,String,T> factory)
    {
        int addIdx = property.lastIndexOf(Add);
        if (addIdx != -1)
        {
            String hint = property.substring(addIdx+1);
            return addList(bean, property.substring(0, addIdx), hint, factory);
        }
        else
        {
            int assignIdx = property.lastIndexOf(Assign);
            if (assignIdx != -1)
            {
                String hint = property.substring(assignIdx+1);
                return assignList(bean, property.substring(0, assignIdx), hint, factory);
            }
            else
            {
                if (property.endsWith(Remove))
                {
                    return (T)removeList(bean, property.substring(0, property.length()-1));
                }
                else
                {
                    return null;
                }
            }
        }
    }
    /**
     * Return prefix of apply pattern or pattern if not apply-pattern.
     * @param property
     * @return 
     */
    public static final String applyPrefix(String property)
    {
        int addIdx = property.lastIndexOf(Add);
        if (addIdx != -1)
        {
            return property.substring(0, addIdx);
        }
        else
        {
            int assignIdx = property.lastIndexOf(Assign);
            if (assignIdx != -1)
            {
                return property.substring(0, assignIdx);
            }
            else
            {
                if (property.endsWith(Remove))
                {
                    return property.substring(0, property.length()-1);
                }
                else
                {
                    return property;
                }
            }
        }
    }
    /**
     * Return true if pattern is add, assign or remove
     * @param pattern
     * @return 
     */
    public static final boolean isApplyPattern(String pattern)
    {
        return isAdd(pattern) || isAssign(pattern) || isRemove(pattern);
    }
    /**
     * Return true for add action
     * @param property
     * @return 
     */
    public static final boolean isAdd(String property)
    {
        return property.lastIndexOf(Add) != -1;
    }
    /**
     * Return true for assign action
     * @param property
     * @return 
     */
    public static final boolean isAssign(String property)
    {
        return property.lastIndexOf(Assign) != -1;
    }
    /**
     * Return true for remove action
     * @param property
     * @return 
     */
    public static final boolean isRemove(String property)
    {
        return property.lastIndexOf(Remove) != -1;
    }
    /**
     * Removes pattern item from list
     * @param bean
     * @param property 
     */
    public static final Object removeList(Object bean, String property)
    {
        return doFor(
            bean, 
            property, 
            null,
            (Object a, int i)->{throw  new UnsupportedOperationException("not supported");},
            (List l, int i)->{return l.remove(i);},
            (Object b, Class c, String p)->{throw  new UnsupportedOperationException("not supported");},
            (Object b, Method m)->{throw  new UnsupportedOperationException("not supported");}
        );
    }
    /**
     * Adds pattern item to end of list
     * @param <T>
     * @param bean
     * @param property
     * @param value 
     */
    public static final <T> void addList(Object bean, String property, T value)
    {
        addList(bean, property, null, (Class<T> c, String h)->{return value;});
    }
    /**
     * Adds pattern item to end of list using given factory and hint
     * @param <T>
     * @param bean
     * @param property
     * @param hint
     * @param factory 
     */
    public static final <T> T addList(Object bean, String property, String hint, BiFunction<Class<T>,String,T> factory)
    {
        Object fieldValue = getValue(bean, property);
        if (fieldValue instanceof List)
        {
            List list = (List) fieldValue;
            Class[] pt = getParameterTypes(bean, property);
            T value = (T)factory.apply(pt[0], hint);
            if (value != null && !pt[0].isAssignableFrom(value.getClass()))
            {
                throw new IllegalArgumentException(pt[0]+" not assignable from "+value);
            }
            list.add(value);
            return value;
        }
        else
        {
            throw new IllegalArgumentException(fieldValue+" not List");
        }
    }
    /**
     * Assign pattern item a new value using given factory (and hint)
     * @param <T>
     * @param bean
     * @param property
     * @param hint
     * @param factory 
     */
    public static final <T> T assignList(Object bean, String property, String hint, BiFunction<Class<T>,String,T> factory)
    {
        Class[] pt = getParameterTypes(bean, prefix(property));
        Class type = pt != null && pt.length > 0 ? pt[0] : null;
        T value = (T) factory.apply(type, hint);
        doFor(
            bean, 
            property, 
            null,
            (Object a, int i)->{Array.set(a, i, value);return null;},
            (List l, int i)->{l.set(i, value);return null;},
            (Object b, Class c, String p)->{throw  new UnsupportedOperationException("not supported");},
            (Object b, Method m)->{throw  new UnsupportedOperationException("not supported");}
        );
        return value;
    }
    /**
     * Set value
     * @param bean
     * @param property
     * @param v 
     */
    public static final void setValue(Object bean, String property, Object v)
    {
        Object vv = BeanHelper.getValue(bean, property);
        if (v != null && v.getClass().isArray() &&  vv != null && (vv instanceof Collection))
        {   // array assignment to Collection
            Collection col = (Collection) vv;
            Class[] pt = BeanHelper.getParameterTypes(bean, property);
            col.clear();
            int len = Array.getLength(v);
            for (int ii=0;ii<len;ii++)
            {
                col.add(ConvertUtility.convert(pt[0], Array.get(v, ii)));
            }
        }
        else
        {
            if (v != null && v.getClass().isArray())
            {   // array with single menber
                if (Array.getLength(v) == 1)
                {
                    v = Array.get(v, 0);
                }
            }
            Class<?> type = BeanHelper.getType(bean, property);
            if (v != null)
            {
                v = ConvertUtility.convert(type, v);
            }
            Object value = v;
            doFor(
                bean, 
                property, 
                type,
                (Object a, int i)->{Array.set(a, i, value);return null;},
                (List l, int i)->{l.set(i, value);return null;},
                (Object b, Class c, String p)->{try
                {
                    c.getField(p).set(b, value);return null;
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
    }

    /**
     * property -> getProperty
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
     * property -> setField
     *
     * @param property
     * @return
     */
    public static final String setter(String property)
    {
        return "set" + upper(property);
    }

    /**
     * <p>
     * (g/s)etField -> property
 <p>
     * Field -> property
     *
     * @param etter
     * @return
     */
    public static final String property(String etter)
    {
        if (etter.startsWith("get") || etter.startsWith("set"))
        {
            etter = etter.substring(3);
        }
        if (etter.startsWith("is"))
        {
            etter = etter.substring(2);
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
    public static final String getProperty(Method method)
    {
        return property(method.getName());
    }
    private static final int ModifierMask = Modifier.ABSTRACT | Modifier.INTERFACE | Modifier.NATIVE | Modifier.STATIC;
    /**
     * Returns Set of classes patterns.
     * <p>Annotations XmlAccessorOrder and XmlType are checked for correct ordering.
     * @param cls
     * @return
     * @see javax.xml.bind.annotation.XmlAccessorOrder
     * @see javax.xml.bind.annotation.XmlType
     */
    public static final Set<String> getProperties(Class<?> cls)
    {
        if (cls.isPrimitive() || cls.isAnnotation() || cls.isEnum() || String.class.equals(cls))
        {
            return Collections.EMPTY_SET;
        }
        NavigableSet<String> set = setFor(cls);
        Method[] methods = cls.getMethods();
        for (Method method : methods)
        {
            if ((method.getModifiers() & ModifierMask) == 0)
            {
                String name = method.getName();
                if ((name.startsWith("get") || name.startsWith("is"))
                        && method.getParameterCount() == 0)
                {
                    String property = getProperty(method);
                    Class<?> returnType = method.getReturnType();
                    if (
                            returnType.isArray() ||
                            List.class.isAssignableFrom(returnType)
                            )
                    {
                        set.add(property);
                    }
                    else
                    {
                        if (hasMethod(methods, setter(property)))
                        {
                            set.add(property);
                        }
                    }
                }
            }
        }
        for (Field field : cls.getFields())
        {
            if ((field.getModifiers() & ModifierMask) == 0)
            {
                set.add(field.getName());
            }
        }
        return set;
    }
    private static boolean hasMethod(Method[] methods, String setter)
    {
        for (Method method : methods)
        {
            if (setter.equals(method.getName()) && method.getParameterCount() == 1)
            {
                return true;
            }
        }
        return false;
    }
    private static NavigableSet<String> setFor(Class<?> cls)
    {
        XmlType xmlType = cls.getAnnotation(XmlType.class);
        if (xmlType != null)
        {
            String[] propOrder = xmlType.propOrder();
            if (propOrder != null && propOrder.length > 0)
            {
                return new TreeSet<>(new ArrayBasedComparator<>(propOrder));
            }
        }
        XmlAccessorOrder xmlAccessorOrder = cls.getAnnotation(XmlAccessorOrder.class);
        if (xmlAccessorOrder == null)
        {
            xmlAccessorOrder = cls.getPackage().getAnnotation(XmlAccessorOrder.class);
        }
        if (xmlAccessorOrder != null)
        {
            XmlAccessOrder xmlAccessOrder = xmlAccessorOrder.value();
            if (xmlAccessOrder != null && xmlAccessOrder.equals(XmlAccessOrder.ALPHABETICAL))
            {
                return new TreeSet<>();
            }
        }
        return new LinkedSet<>();
    }
    private static boolean separator(int cc)
    {
        switch (cc)
        {
            case Lim:
            case Add:
            case Rem:
            case Assign:
                return true;
            default:
                return false;
        }
    }
    /**
     * Return string before last reparator
     * @param pattern
     * @return 
     */
    public static final String prefix(String pattern)
    {
        int idx = CharSequences.lastIndexOf(pattern, BeanHelper::separator);
        if (idx != -1)
        {
            return pattern.substring(0, idx);
        }
        else
        {
            return "";
        }
    }
    /**
     * Return true if string after last separator is numeric
     * @param pattern
     * @return 
     */
    public static final boolean isListItem(String pattern)
    {
        int idx = pattern.lastIndexOf(Lim);
        if (idx != -1)
        {
            try
            {
                Integer.parseInt(pattern.substring(idx+1));
                return true;
            }
            catch (NumberFormatException ex)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    /**
     * Return string after last '.'/'+'/'#'
     * @param pattern
     * @return 
     */
    public static final String suffix(String pattern)
    {
        int idx = CharSequences.lastIndexOf(pattern, BeanHelper::separator);
        if (idx != -1)
        {
            return pattern.substring(idx+1);
        }
        else
        {
            return pattern;
        }
    }
    /**
     * Return set of objects patterns
     * @param bean
     * @return 
     */
    public static final Set<String> getProperties(Object bean)
    {
        return stream(bean).collect(Collectors.toSet());
    }
    /**
     * Returns targets pattern or null if target not found.
     * @param bean
     * @param target
     * @return 
     */
    public static String getPattern(Object bean, Object target)
    {
        return stream(bean).filter((s)->{return target.equals(getValue(bean, s));}).findFirst().orElse(null);
    }
    /**
     * Return stream of bean patterns
     * @param bean
     * @return 
     */
    public static final Stream<String> stream(Object bean)
    {
        return StreamSupport.stream(spliterator(bean), false);
    }
    /**
     * Return spliterator of bean patterns
     * <p>Note! tryAdvance method is not implemented.
     * @param bean
     * @return 
     */
    public static final Spliterator<String> spliterator(Object bean)
    {
        return new SpliteratorImpl(bean);
    }

    private static class SpliteratorImpl implements Spliterator<String>
    {
        private Deque<Ctx> stack;
        private Object bean;

        public SpliteratorImpl(Object bean)
        {
            this.bean = bean;
        }

        @Override
        public void forEachRemaining(Consumer<? super String> action)
        {
            walk(bean, action);
        }

        private void walk(Object bean, Consumer<? super String> consumer)
        {
            walk("", bean, consumer);
        }

        private void walk(String prefix, Object bean, Consumer<? super String> consumer)
        {
            if (bean != null)
            {
                for (String fld : getProperties(bean.getClass()))
                {
                    String name = prefix + fld;
                    consumer.accept(name);
                    Object value = getValue(bean, fld);
                    if (value != null)
                    {
                        if (value.getClass().isArray())
                        {
                            int len = Array.getLength(value);
                            for (int index = 0;index<len;index++)
                            {
                                Object o = Array.get(value, index);
                                consumer.accept(name + Lim + index);
                                walk(name + Lim + index + Lim, o, consumer);
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
                                    consumer.accept(name + Lim + index);
                                    walk(name + Lim + index + Lim, o, consumer);
                                    index++;
                                }
                            }
                            else
                            {
                                walk(name + Lim, value, consumer);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action)
        {
            if (stack == null)
            {
                stack = new ArrayDeque<>();
                stack.push(new Ctx("", bean));
            }
            while (!stack.isEmpty())
            {
                Ctx c = stack.peek();
                if (c.oit != null && c.oit.hasNext())
                {
                    action.accept(c.name + Lim + c.idx);
                    Object next = c.oit.next();
                    stack.push(new Ctx(c.name+Lim+c.idx+Lim, next));
                    c.idx++;
                    return true;
                }
                if (c.fit == null && c.ob != null)
                {
                    c.fit = getProperties(c.ob.getClass()).iterator();
                }
                if (c.fit != null && c.fit.hasNext())
                {
                    String fld = c.fit.next();
                    c.name = c.prefix + fld;
                    action.accept(c.name);
                    Object value = getValue(c.ob, fld);
                    if (value != null)
                    {
                        if (value.getClass().isArray())
                        {
                            c.oit = new ArrayIterator<>(value);
                            c.idx = 0;
                        }
                        else
                        {
                            if (value instanceof List)
                            {
                                List list = (List) value;
                                c.oit = list.iterator();
                                c.idx = 0;
                            }
                            else
                            {
                                stack.push(new Ctx(c.name + Lim, value));
                            }
                        }
                    }
                    return true;
                }
                stack.pop();
            }
            return false;
        }

        @Override
        public Spliterator<String> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return bean.getClass().getDeclaredFields().length*2;
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
    private static class Ctx
    {
        private String prefix;
        private Object ob;
        private Iterator<String> fit;
        private Iterator<Object> oit;
        private int idx;
        private String name;

        public Ctx(String p, Object o)
        {
            this.prefix = p;
            this.ob = o;
        }

    }
}
