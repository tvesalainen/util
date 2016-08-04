/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author tkv
 */
public class BeanHelperException extends RuntimeException
{

    /**
     * Creates a new instance of <code>BeanHelperException</code> without detail message.
     */
    public BeanHelperException()
    {
    }

    public BeanHelperException(Field field, Throwable thr)
    {
        super(" field="+field.getName(), thr);
    }

    public BeanHelperException(Object object, Field field)
    {
        super("base="+object.getClass().getName()+" field="+field.getName());
    }

    public BeanHelperException(Object object, Field field, Throwable thr)
    {
        super("base="+object.getClass().getName()+" field="+field.getName(), thr);
    }

    public BeanHelperException(String msg, Object object, String pseudoField)
    {
        super(msg+": base="+object.getClass().getName()+" field="+pseudoField);
    }

    public BeanHelperException(String msg, Object object, String pseudoField, Throwable thr)
    {
        super(msg+": base="+object.getClass().getName()+" field="+pseudoField, thr);
    }

    public BeanHelperException(Object object, String pseudoField)
    {
        super("base="+object.getClass().getName()+" field="+pseudoField);
    }

    public BeanHelperException(Object object, String pseudoField, Throwable thr)
    {
        super("base="+object.getClass().getName()+" field="+pseudoField, thr);
    }

    public BeanHelperException(Object object, Method method, Throwable thr)
    {
        super("base="+object.getClass().getName()+" method="+method.getName(), thr);
    }

    public BeanHelperException(Class clazz, String name, Throwable thr)
    {
        super("class="+clazz.getName()+" name="+name, thr);
    }

    /**
     * Constructs an instance of <code>BeanHelperException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BeanHelperException(String msg)
    {
        super(msg);
    }
    /**
     * Constructs an instance of <code>BeanHelperException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BeanHelperException(String msg, Throwable thr)
    {
        super(msg, thr);
    }

    BeanHelperException(Method method, Throwable thr)
    {
        super(" method="+method.getName(), thr);
    }

}
