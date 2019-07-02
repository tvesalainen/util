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

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * A utility that helps convert java type to another.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
            Class clazz = openBox(object.getClass());
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
                if (byte.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Byte.valueOf(b.byteValue());
                }
                if (char.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Character.valueOf((char) b.shortValue());
                }
                if (short.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Short.valueOf(b.shortValue());
                }
                if (int.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Integer.valueOf(b.intValue());
                }
                if (long.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Long.valueOf(b.longValue());
                }
                if (float.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Float.valueOf(b.floatValue());
                }
                if (double.class.equals(expectedReturnType) && object instanceof Number)
                {
                    Number b = (Number) object;
                    return (T) Double.valueOf(b.doubleValue());
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
                if (Character.class.equals(expectedReturnType))
                {
                    if (string.length() != 1)
                    {
                        throw new IllegalArgumentException("Cannot convert '" + string + "' to char");
                    }
                    Character cc = string.charAt(0);
                    return (T)cc;
                }
                if (expectedReturnType.isEnum())
                {
                    return (T) Enum.valueOf((Class<Enum>) expectedReturnType, string);
                }
                if (Color.class.isAssignableFrom(expectedReturnType))
                {
                    if (string.startsWith("#"))
                    {
                        int ci = Integer.parseInt(string.substring(1), 16);
                        return (T) new Color(ci);
                    }
                }
                if (ZonedDateTime.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) ZonedDateTime.parse(string);
                }
                if (Duration.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) Duration.parse(string);
                }
                if (LocalDate.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) LocalDate.parse(string);
                }
                if (LocalDateTime.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) LocalDateTime.parse(string);
                }
                if (LocalTime.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) LocalTime.parse(string);
                }
                if (Period.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) Period.parse(string);
                }
                if (Year.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) Year.parse(string);
                }
                if (YearMonth.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) YearMonth.parse(string);
                }
                if (Path.class.isAssignableFrom(expectedReturnType))
                {
                    return (T) Paths.get(string);
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
            if (object instanceof Color)
            {
                if (expectedReturnType.equals(Color.class))
                {
                    Color color = (Color) object;
                    return (T) String.format("#%06x", color.getRGB() & 0xffffff);
                }
            }
            if (object instanceof Duration)
            {
                Duration d = (Duration) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.toString();
                }
            }
            if (object instanceof LocalDate)
            {
                LocalDate d = (LocalDate) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }
            if (object instanceof ZonedDateTime)
            {
                ZonedDateTime d = (ZonedDateTime) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.format(DateTimeFormatter.ISO_DATE_TIME);
                }
            }
            if (object instanceof LocalDateTime)
            {
                LocalDateTime d = (LocalDateTime) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }
            if (object instanceof LocalTime)
            {
                LocalTime d = (LocalTime) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.format(DateTimeFormatter.ISO_LOCAL_TIME);
                }
            }
            if (object instanceof Year)
            {
                Year d = (Year) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.toString();
                }
            }
            if (object instanceof YearMonth)
            {
                YearMonth d = (YearMonth) object;
                if (expectedReturnType.equals(String.class))
                {
                    return (T) d.toString();
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
    /**
     * Returns primitive type for boxed type
     * @param cls
     * @return 
     */
    public static final Class<?> openBox(Class<?> cls)
    {
        switch (cls.getName())
        {
            case "java.lang.Boolean":
                return boolean.class;
            case "java.lang.Byte":
                return byte.class;
            case "java.lang.Character":
                return char.class;
            case "java.lang.Short":
                return short.class;
            case "java.lang.Integer":
                return int.class;
            case "java.lang.Long":
                return long.class;
            case "java.lang.Float":
                return float.class;
            case "java.lang.Double":
                return double.class;
            default:
                return cls;
        }
    }
}
