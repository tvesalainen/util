/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.vesalainen.util.ConvertUtility;

/**
 * An utility to access objects field using getX(), isX() and setX() methods
 * @author tkv
 * @param <T> Base class type
 * @param <D> Field type
 */
public abstract class AbstractBeanField<T,D> implements BeanField<D>
{
    private Class<?> type;
    private Method getter;
    private Method setter;
    /**
     * Creates an AbstractBeanField
     * @param obj Base object
     * @param fieldname Fieldname
     */
    public AbstractBeanField(T obj, String fieldname)
    {
        this((Class<? extends T>)obj.getClass(), fieldname);
    }
    /**
     * Creates an AbstractBeanField
     * @param cls Base clas
     * @param fieldname Fieldname
     */
    public AbstractBeanField(Class<? extends T> cls, String fieldname)
    {
        try
        {
            try
            {
                getter = cls.getMethod(BeanHelper.getter(fieldname));
            }
            catch (NoSuchMethodException ex)
            {
                try
                {
                    getter = cls.getMethod(BeanHelper.isser(fieldname));
                }
                catch (NoSuchMethodException ex1)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
            type = getter.getReturnType();
            setter = cls.getMethod(BeanHelper.setter(fieldname), type);
        }
        catch (SecurityException | NoSuchMethodException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        
    }
    /**
     * Set value using type conversions
     * @param value 
     * @see org.vesalainen.util.ConvertUtility#convert(java.lang.Class, java.lang.Object) 
     */
    @Override
    public void set(Object value)
    {
        try
        {
            setter.invoke(getBase(), ConvertUtility.convert(type, value));
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Get value.
     * @return 
     */
    @Override
    public D get()
    {
        try
        {
            return (D) getter.invoke(getBase());
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Returns base class
     * @return 
     */
    protected abstract T getBase();
}
