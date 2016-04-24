/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
    private static final char Del = '.';
    private static final String RegexDel = "\\.";
    private static final char Plus = '+';
    private static final char Minus = '-';
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
    private static Object getValue(Object[] arr, int index)
    {
        return arr[index];
    }
    private static Object getValue(List list, int index)
    {
        return list.get(index);
    }
    private static Object doFor(Object bean, String property, Class type, IndexFunction<Object[],Object> arrayFunc, IndexFunction<List,Object> listFunc, FieldFunction fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        String[] parts = property.split(RegexDel);
        int len = parts.length - 1;
        for (int ii = 0; ii < len; ii++)
        {
            bean = doIt(bean, parts[ii], null, BeanHelper::getValue, BeanHelper::getValue, BeanHelper::getFieldValue, BeanHelper::getMethodValue);
        }
        int idx = property.lastIndexOf(Del);
        if (idx != -1)
        {
            property = property.substring(idx + 1);
        }
        return doIt(bean, property, type, arrayFunc, listFunc, fieldFunc, methodFunc);
    }
    private static Object doIt(Object bean, String property, Class argType, IndexFunction<Object[],Object> arrayFunc, IndexFunction<List,Object> listFunc, FieldFunction fieldFunc, BiFunction<Object,Method,Object> methodFunc)
    {
        if (Index.matcher(property).matches())
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
            (Object[] a, int i)->{return a[i].getClass().getGenericSuperclass();},
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
            (Object b, Class c, String p)->{return getField(c, p).getAnnotation(annotationClass);}, 
            (Object b, Method m)->{return m.getAnnotation(annotationClass);});
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
            (Object[] a, int i)->{return a[i].getClass().getAnnotations();},
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
            (Object[] a, int i)->{return a[i].getClass();},
            (List l, int i)->{return l.get(i).getClass();},
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
     * <p>List item property remove by adding '-' to the end of pattern.
     * <p>E.g. list.3- - list.remove(3)
     * <p>List item creation to the end of the list.
     * <p>E.g. list+ - add(factory.get(cls, null))
     * <p>E.g. list+hint - add(factory.get(cls, hint))
     * @param <T>
     * @param base
     * @param fieldname 
     */
    public static final <T> void applyList(Object base, String fieldname)
    {
        applyList(base, fieldname, BeanHelper::defaultFactory);
    }
    /**
     * Applies bean action by using given factory
     * <p>Bean actions are:
     * <p>List item property remove by adding '-' to the end of pattern.
     * <p>E.g. list.3- - list.remove(3)
     * <p>List item creation to the end of the list.
     * <p>E.g. list+ - add(factory.get(cls, null))
     * <p>E.g. list+hint - add(factory.get(cls, hint))
     * @param <T>
     * @param bean
     * @param property
     * @param factory 
     */
    public static final <T> void applyList(Object bean, String property, BiFunction<Class<T>,String,T> factory)
    {
        int plusIdx = property.lastIndexOf('+');
        if (plusIdx != -1)
        {
            String hint = property.substring(0, plusIdx+1);
            addList(bean, property.substring(0, plusIdx), hint, factory);
        }
        else
        {
            if (property.endsWith("-"))
            {
                removeList(bean, property.substring(0, property.length()-1));
            }
            else
            {
                throw new IllegalArgumentException("nothing to apply");
            }
        }
    }
    /**
     * Removes pattern item from list
     * @param bean
     * @param property 
     */
    public static final void removeList(Object bean, String property)
    {
        doFor(
            bean, 
            property, 
            null,
            (Object[] a, int i)->{throw  new UnsupportedOperationException("not supported");},
            (List l, int i)->{l.remove(i);return null;},
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
     * Adds pattern item to end of list giving factory hint and factory
     * @param <T>
     * @param bean
     * @param property
     * @param hint
     * @param factory 
     */
    public static final <T> void addList(Object bean, String property, String hint, BiFunction<Class<T>,String,T> factory)
    {
        Object fieldValue = getValue(bean, property);
        if (fieldValue instanceof List)
        {
            List list = (List) fieldValue;
            Class[] pt = getParameterTypes(bean, property);
            Object value = factory.apply(pt[0], hint);
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
    /**
     * Set value
     * @param bean
     * @param property
     * @param v 
     */
    public static final void setValue(Object bean, String property, Object v)
    {
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
            (Object[] a, int i)->{a[i]=value;return null;},
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
     *
     * @param cls
     * @return
     */
    public static final Set<String> getProperties(Class<?> cls)
    {
        Set<String> set = new LinkedSet<>();
        for (Method method : cls.getMethods())
        {
            if ((method.getModifiers() & ModifierMask) == 0)
            {
                String name = method.getName();
                if ((name.startsWith("get") || name.startsWith("is"))
                        && method.getParameterCount() == 0)
                {
                    String property = getProperty(method);
                    Class<?> returnType = method.getReturnType();
                    if (!List.class.isAssignableFrom(returnType))
                    {
                        try
                        {
                            cls.getMethod(setter(property), returnType);
                        }
                        catch (NoSuchMethodException ex)
                        {
                            continue;
                        }
                    }
                    set.add(property);
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
    /**
     * Return string before last '.'
     * @param pattern
     * @return 
     */
    public static final String prefix(String pattern)
    {
        int idx = pattern.lastIndexOf(Del);
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
     * Return true if string after last '.' is numeric
     * @param pattern
     * @return 
     */
    public static final boolean isListItem(String pattern)
    {
        int idx = pattern.lastIndexOf(Del);
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
     * Return string after last '.'
     * @param pattern
     * @return 
     */
    public static final String suffix(String pattern)
    {
        int idx = pattern.lastIndexOf(Del);
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
            for (String fld : getProperties(bean.getClass()))
            {
                String name = prefix + fld;
                consumer.accept(name);
                Object value = getValue(bean, fld);
                if (value != null)
                {
                    if (value.getClass().isArray())
                    {
                        Object[] arr = (Object[]) value;
                        int index = 0;
                        for (Object o : arr)
                        {
                            consumer.accept(name + Del + index);
                            walk(name + Del + index + Del, o, consumer);
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
                                consumer.accept(name + Del + index);
                                walk(name + Del + index + Del, o, consumer);
                                index++;
                            }
                        }
                        else
                        {
                            walk(name + Del, value, consumer);
                        }
                    }
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action)
        {
            throw new UnsupportedOperationException("Not supported yet.");
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
}
