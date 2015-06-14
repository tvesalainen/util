/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
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
            if (boolean.class.equals(clazz))
            {
                Boolean b = (Boolean) object;
                return b;
            }
            if (byte.class.equals(clazz))
            {
                Byte b = (Byte) object;
                return b;
            }
            if (char.class.equals(clazz))
            {
                Character c = (Character) object;
                return c;
            }
            if (short.class.equals(clazz))
            {
                Short s = (Short) object;
                return s;
            }
            if (int.class.equals(clazz))
            {
                Integer i = (Integer) object;
                return i;
            }
            if (long.class.equals(clazz))
            {
                Long l = (Long) object;
                return l;
            }
            if (float.class.equals(clazz))
            {
                Float f  = (Float) object;
                return f;
            }
            if (double.class.equals(clazz))
            {
                Double d  = (Double) object;
                return d;
            }
            throw new IllegalArgumentException("Unknown primitive type'" + clazz);
        }
        return object;
    }

    public static <T> void convert(T[] target, Object[] object) throws ConvertUtilityException
    {
        T[] result = (T[]) convert(target.getClass(), object);
        for (int ii=0;ii<result.length;ii++)
        {
            target[ii] = result[ii];
        }

    }
    public static <T> T convert(Class<T> expectedReturnType, Object object) throws ConvertUtilityException
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
                if (int[].class.equals(clazz))
                {
                    int[] arr = (int[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (long[].class.equals(clazz))
                {
                    long[] arr = (long[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (short[].class.equals(clazz))
                {
                    short[] arr = (short[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (float[].class.equals(clazz))
                {
                    float[] arr = (float[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (double[].class.equals(clazz))
                {
                    double[] arr = (double[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (char[].class.equals(clazz))
                {
                    char[] arr = (char[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (byte[].class.equals(clazz))
                {
                    byte[] arr = (byte[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                if (boolean[].class.equals(clazz))
                {
                    boolean[] arr = (boolean[]) object;
                    Object[] oa = new Object[arr.length];
                    for (int ii=0;ii<arr.length;ii++)
                    {
                        oa[ii] = convert(expectedReturnType, arr[ii]);
                    }
                    return (T) oa;
                }
                /*
                Object[] arr = (Object[]) object;
                Object[] oa = new Object[arr.length];
                for (int ii=0;ii<arr.length;ii++)
                {
                    oa[ii] = convert(expectedReturnType.getComponentType(), arr[ii]);
                }
                return (T) Arrays.copyOf(oa, oa.length, expectedReturnType);
                        */
            }
            if (expectedReturnType.isAssignableFrom(clazz))
            {
                return (T) object;
            }
            if (object instanceof Convertable)
            {
                Convertable cc = (Convertable) object;
                TypeVariable<?>[] tvl = cc.getClass().getTypeParameters();
                if (expectedReturnType.isAssignableFrom((Class<?>) tvl[1].getGenericDeclaration()))
                {
                    return (T) cc.convertTo();
                }
            }
            if (expectedReturnType.equals(String.class))
            {
                return (T) object.toString();
            }
            if (expectedReturnType.isPrimitive())
            {
                if (boolean.class.equals(expectedReturnType) && object instanceof Boolean)
                {
                    Boolean b = (Boolean) object;
                    return (T) b;
                }
                if (byte.class.equals(expectedReturnType) && object instanceof Byte)
                {
                    Byte b = (Byte) object;
                    return (T) b;
                }
                if (char.class.equals(expectedReturnType) && object instanceof Character)
                {
                    Character c = (Character) object;
                    return (T) c;
                }
                if (short.class.equals(expectedReturnType) && object instanceof Short)
                {
                    Short s = (Short) object;
                    return (T) s;
                }
                if (int.class.equals(expectedReturnType) && object instanceof Integer)
                {
                    Integer i = (Integer) object;
                    return (T) i;
                }
                if (long.class.equals(expectedReturnType) && object instanceof Long)
                {
                    Long l = (Long) object;
                    return (T) l;
                }
                if (float.class.equals(expectedReturnType) && object instanceof Float)
                {
                    Float f = (Float) object;
                    return (T) f;
                }
                if (double.class.equals(expectedReturnType) && object instanceof Double)
                {
                    Double d = (Double) object;
                    return (T) d;
                }
            }
            if (object instanceof String)
            {
                String string = (String) object;
                if (expectedReturnType.isPrimitive())
                {
                    if (boolean.class.equals(expectedReturnType))
                    {
                        return (T) Boolean.valueOf(string);
                    }
                    if (byte.class.equals(expectedReturnType))
                    {
                        return (T) Byte.valueOf(string);
                    }
                    if (char.class.equals(expectedReturnType))
                    {
                        if (string.length() != 1)
                        {
                            throw new IllegalArgumentException("Cannot convert '" + string + "' to char");
                        }
                        Character cc = string.charAt(0);
                        return (T)cc;
                    }
                    if (short.class.equals(expectedReturnType))
                    {
                        return (T) Short.valueOf(string);
                    }
                    if (int.class.equals(expectedReturnType))
                    {
                        return (T) Integer.valueOf(string);
                    }
                    if (long.class.equals(expectedReturnType))
                    {
                        return (T) Long.valueOf(string);
                    }
                    if (float.class.equals(expectedReturnType))
                    {
                        return (T) Float.valueOf(string);
                    }
                    if (double.class.equals(expectedReturnType))
                    {
                        return (T) Double.valueOf(string);
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType+"'");
                }
                if (expectedReturnType.isEnum())
                {
                    return (T) Enum.valueOf((Class<Enum>) expectedReturnType, string);
                }
                try
                {
                    // try to find valueOf method
                    Method method = expectedReturnType.getDeclaredMethod("parse", clazz);
                    return (T) method.invoke(null, object);
                }
                catch (NoSuchMethodException ex)
                {
                }
            }
            if (object instanceof Number)
            {
                Number number = (Number) object;
                if (expectedReturnType.isPrimitive())
                {
                    if (Byte.class.equals(expectedReturnType))
                    {
                        return (T) (Byte)number.byteValue();
                    }
                    if (Short.class.equals(expectedReturnType))
                    {
                        return (T) (Short)number.shortValue();
                    }
                    if (Integer.class.equals(expectedReturnType))
                    {
                        return (T) (Integer)number.intValue();
                    }
                    if (Long.class.equals(expectedReturnType))
                    {
                        return (T) (Long)number.longValue();
                    }
                    if (Float.class.equals(expectedReturnType))
                    {
                        return (T) (Float)number.floatValue();
                    }
                    if (Double.class.equals(expectedReturnType))
                    {
                        return (T) (Double)number.doubleValue();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType+"'");
                }
            }
            if (object instanceof Calendar)
            {
                Calendar calendar = (Calendar) object;
                if (expectedReturnType.isPrimitive())
                {
                    if (long.class.equals(expectedReturnType))
                    {
                        return (T) (Long)calendar.getTimeInMillis();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType+"'");
                }
                if (expectedReturnType.equals(Date.class))
                {
                    return (T)calendar.getTime();
                }
            }
            if (object instanceof Date)
            {
                Date date = (Date) object;
                if (expectedReturnType.isPrimitive())
                {
                    if (long.class.equals(expectedReturnType))
                    {
                        return (T)(Long)date.getTime();
                    }
                    throw new IllegalArgumentException("Unknown primitive type '" + expectedReturnType+"'");
                }
                if (expectedReturnType.equals(Calendar.class))
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    return (T)calendar;
                }
            }
            try
            {
                // try to find valueOf method
                Method method = expectedReturnType.getDeclaredMethod("valueOf", clazz);
                return (T)method.invoke(null, object);
            }
            catch (NoSuchMethodException ex)
            {
            }
            try
            {
                // try to find expectedReturnType constructor taking single object argument
                Constructor cons = expectedReturnType.getConstructor(clazz);
                return (T)cons.newInstance(object);
            }
            catch (NoSuchMethodException ex)
            {
            }
            throw new IllegalArgumentException("Cannot convert "+clazz+" to " + expectedReturnType);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException ex1)
        {
            throw new ConvertUtilityException(expectedReturnType, object, ex1);
        }
    }
}
