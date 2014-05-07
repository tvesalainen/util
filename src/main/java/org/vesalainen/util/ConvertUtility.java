/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author tkv
 */
public class ConvertUtility
{
    /**
     * Convert a primitive object to its Class object
     * @param object
     * @return
     */
    public static Object convertPrimitive(Object object)
    {
        if (object == null)
        {
            return object;
        }
        Class<?> clazz = object.getClass();
        if (clazz.isPrimitive())
        {
            if ("boolean".equals(clazz.getName()))
            {
                Boolean b = (Boolean) object;
                return b;
            }
            if ("byte".equals(clazz.getName()))
            {
                Byte b = (Byte) object;
                return b;
            }
            if ("char".equals(clazz.getName()))
            {
                Character c = (Character) object;
                return c;
            }
            if ("short".equals(clazz.getName()))
            {
                Short s = (Short) object;
                return s;
            }
            if ("int".equals(clazz.getName()))
            {
                Integer i = (Integer) object;
                return i;
            }
            if ("long".equals(clazz.getName()))
            {
                Long l = (Long) object;
                return l;
            }
            if ("float".equals(clazz.getName()))
            {
                Float f  = (Float) object;
                return f;
            }
            if ("double".equals(clazz.getName()))
            {
                Double d  = (Double) object;
                return d;
            }
            throw new IllegalArgumentException("Unknown primitive type'" + clazz.getName());
        }
        return object;
    }

    public static void convert(Object[] target, Object[] object) throws ConvertUtilityException
    {
        Object[] result = (Object[]) convert(target.getClass(), object);
        for (int ii=0;ii<result.length;ii++)
        {
            target[ii] = result[ii];
        }

    }
    public static Object convert(Class<?> expectedReturnType, Object object) throws ConvertUtilityException
    {
        try
        {
            // simple cases
            if (object == null)
            {
                return null;
            }
            Class clazz = object.getClass();
            if (clazz.isArray())
            {
                if ("[I".equals(clazz.getName()))
                {
                    int[] arr = (int[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[J".equals(clazz.getName()))
                {
                    long[] arr = (long[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[S".equals(clazz.getName()))
                {
                    short[] arr = (short[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[F".equals(clazz.getName()))
                {
                    float[] arr = (float[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[D".equals(clazz.getName()))
                {
                    double[] arr = (double[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[C".equals(clazz.getName()))
                {
                    char[] arr = (char[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[B".equals(clazz.getName()))
                {
                    byte[] arr = (byte[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                if ("[Z".equals(clazz.getName()))
                {
                    boolean[] arr = (boolean[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return oa;
                }
                Object[] arr = (Object[]) object;
                Object[] oa = new Object[arr.length];
                for (int ii=0;ii<arr.length;ii++)
                {
                    oa[ii] = convert(expectedReturnType, arr[ii]);
                }
                return oa;
            }
            if (expectedReturnType.isAssignableFrom(clazz))
            {
                return object;
            }
            if (object instanceof Convertable)
            {
                Convertable cc = (Convertable) object;
                TypeVariable<?>[] tvl = cc.getClass().getTypeParameters();
                if (expectedReturnType.isAssignableFrom((Class<?>) tvl[1].getGenericDeclaration()))
                {
                    return cc.convertTo();
                }
            }
            if (expectedReturnType.equals(String.class))
            {
                return object.toString();
            }
            if (expectedReturnType.isPrimitive())
            {
                if ("boolean".equals(expectedReturnType.getName()) && object instanceof Boolean)
                {
                    Boolean b = (Boolean) object;
                    return b.booleanValue();
                }
                if ("byte".equals(expectedReturnType.getName()) && object instanceof Byte)
                {
                    Byte b = (Byte) object;
                    return b.byteValue();
                }
                if ("char".equals(expectedReturnType.getName()) && object instanceof Character)
                {
                    Character c = (Character) object;
                    return c.charValue();
                }
                if ("short".equals(expectedReturnType.getName()) && object instanceof Short)
                {
                    Short s = (Short) object;
                    return s.shortValue();
                }
                if ("int".equals(expectedReturnType.getName()) && object instanceof Integer)
                {
                    Integer i = (Integer) object;
                    return i.intValue();
                }
                if ("long".equals(expectedReturnType.getName()) && object instanceof Long)
                {
                    Long l = (Long) object;
                    return l.longValue();
                }
                if ("float".equals(expectedReturnType.getName()) && object instanceof Float)
                {
                    Float f = (Float) object;
                    return f.floatValue();
                }
                if ("double".equals(expectedReturnType.getName()) && object instanceof Double)
                {
                    Double d = (Double) object;
                    return d.doubleValue();
                }
            }
            if (object instanceof String)
            {
                String string = (String) object;
                if (expectedReturnType.isPrimitive())
                {
                    if ("boolean".equals(expectedReturnType.getName()))
                    {
                        return Boolean.parseBoolean(string);
                    }
                    if ("byte".equals(expectedReturnType.getName()))
                    {
                        return Byte.parseByte(string);
                    }
                    if ("char".equals(expectedReturnType.getName()))
                    {
                        if (string.length() != 1)
                        {
                            throw new IllegalArgumentException("Cannot convert '" + string + "' to char");
                        }
                        return string.charAt(0);
                    }
                    if ("short".equals(expectedReturnType.getName()))
                    {
                        return Short.parseShort(string);
                    }
                    if ("int".equals(expectedReturnType.getName()))
                    {
                        return Integer.parseInt(string);
                    }
                    if ("long".equals(expectedReturnType.getName()))
                    {
                        return Long.parseLong(string);
                    }
                    if ("float".equals(expectedReturnType.getName()))
                    {
                        return Float.parseFloat(string);
                    }
                    if ("double".equals(expectedReturnType.getName()))
                    {
                        return Double.parseDouble(string);
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType.getName()+"'");
                }
            }
            if (object instanceof Number)
            {
                Number number = (Number) object;
                if (expectedReturnType.isPrimitive())
                {
                    if ("byte".equals(expectedReturnType.getName()))
                    {
                        return number.byteValue();
                    }
                    if ("short".equals(expectedReturnType.getName()))
                    {
                        return number.shortValue();
                    }
                    if ("int".equals(expectedReturnType.getName()))
                    {
                        return number.intValue();
                    }
                    if ("long".equals(expectedReturnType.getName()))
                    {
                        return number.longValue();
                    }
                    if ("float".equals(expectedReturnType.getName()))
                    {
                        return number.floatValue();
                    }
                    if ("double".equals(expectedReturnType.getName()))
                    {
                        return number.doubleValue();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType.getName()+"'");
                }
            }
            if (object instanceof Calendar)
            {
                Calendar calendar = (Calendar) object;
                if (expectedReturnType.isPrimitive())
                {
                    if ("long".equals(expectedReturnType.getName()))
                    {
                        return calendar.getTimeInMillis();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType.getName()+"'");
                }
                if (expectedReturnType.equals(Date.class))
                {
                    return calendar.getTime();
                }
            }
            if (object instanceof Date)
            {
                Date date = (Date) object;
                if (expectedReturnType.isPrimitive())
                {
                    if ("long".equals(expectedReturnType.getName()))
                    {
                        return date.getTime();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType.getName()+"'");
                }
                if (expectedReturnType.equals(Calendar.class))
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    return calendar;
                }
            }
            try
            {
                // try to find valueOf method
                Method method = expectedReturnType.getDeclaredMethod("valueOf", clazz);
                return method.invoke(null, object);
            }
            catch (NoSuchMethodException ex)
            {
                try
                {
                    // try to find expectedReturnType constructor taking single object argument
                    Constructor cons = expectedReturnType.getConstructor(clazz);
                    return cons.newInstance(object);
                }
                catch (NoSuchMethodException exx)
                {
                    throw new IllegalArgumentException("Cannot convert "+clazz.getName()+" to " + expectedReturnType.getName(), exx);
                }
            }
        }
        catch (InstantiationException ex1)
        {
            throw new ConvertUtilityException(expectedReturnType, object, ex1);
        }
        catch (IllegalAccessException ex1)
        {
            throw new ConvertUtilityException(expectedReturnType, object, ex1);
        }
        catch (InvocationTargetException ex1)
        {
            throw new ConvertUtilityException(expectedReturnType, object, ex1);
        }
    }
}
